# ADR-005: 이벤트 소싱 패턴 적용

## 상태
채택됨 (2025-11-29)

## 컨텍스트 (Context)

PayFlow는 금융 거래를 다루는 시스템으로, 다음과 같은 요구사항이 있습니다:

### 비즈니스 요구사항
1. **감사 추적 (Audit Trail)**: 모든 결제 상태 변경 이력 보존
2. **규제 준수**: 금융 거래 이력 7년 보관 의무
3. **분쟁 해결**: 과거 특정 시점의 상태 재구성 필요
4. **디버깅**: 장애 발생 시 이벤트 재생으로 원인 파악

### 기존 방식의 문제점
**CRUD 방식**:
```java
// 현재 상태만 저장
payment.setStatus(PaymentStatus.APPROVED);
paymentRepository.save(payment);
// 이전 상태는 사라짐 (PENDING → APPROVED 과정 손실)
```

**문제점**:
- 상태 변경 이력 손실
- 누가, 언제, 왜 변경했는지 알 수 없음
- 과거 시점 재구성 불가능
- 감사 로그 별도 구현 필요

## 결정 (Decision)

**결제 도메인에 이벤트 소싱 패턴을 적용**합니다.

### 이벤트 소싱 구조

#### 1. Event Store (이벤트 저장소)
```java
@Entity
@Table(name = "payment_event_store")
public class PaymentEvent {
    @Id
    @GeneratedValue
    private Long id;
    
    private String paymentId;      // 집합 루트 ID
    private String eventType;      // PaymentCreated, PaymentApproved, ...
    private String eventData;      // JSON 형태의 이벤트 데이터
    private Long sequence;         // 이벤트 순서
    private LocalDateTime occurredAt;
    private String userId;         // 누가
    private String reason;         // 왜
}
```

#### 2. 이벤트 발행
```java
@Service
public class PaymentService {
    
    public void approvePayment(Long paymentId, String userId) {
        Payment payment = paymentRepository.findById(paymentId);
        
        // 상태 변경
        payment.approve();
        paymentRepository.save(payment);
        
        // 이벤트 저장
        PaymentEvent event = new PaymentEvent(
            paymentId.toString(),
            "PaymentApproved",
            toJson(payment),
            getNextSequence(paymentId),
            LocalDateTime.now(),
            userId,
            "사용자 승인"
        );
        eventStore.save(event);
        
        // Kafka 발행
        kafkaTemplate.send("PaymentApproved", 
            new PaymentApprovedEvent(payment));
    }
}
```

#### 3. 상태 재구성 (Event Replay)
```java
@Service
public class PaymentEventSourcingService {
    
    public Payment reconstructPayment(String paymentId, Long sequence) {
        // 특정 시퀀스까지의 이벤트 조회
        List<PaymentEvent> events = eventStore
            .findByPaymentIdAndSequenceLessThanEqual(paymentId, sequence);
        
        // 초기 상태
        Payment payment = new Payment();
        
        // 이벤트 재생
        for (PaymentEvent event : events) {
            applyEvent(payment, event);
        }
        
        return payment;
    }
    
    private void applyEvent(Payment payment, PaymentEvent event) {
        switch (event.getEventType()) {
            case "PaymentCreated":
                payment.setStatus(PaymentStatus.PENDING);
                break;
            case "PaymentApproved":
                payment.setStatus(PaymentStatus.APPROVED);
                break;
            case "PaymentCancelled":
                payment.setStatus(PaymentStatus.CANCELLED);
                break;
        }
    }
}
```

## 대안 (Alternatives)

### 1. 감사 로그 테이블
**장점**:
- 구현 단순
- 기존 CRUD 방식 유지

**단점**:
- 상태 재구성 불가능
- 로그와 실제 데이터 불일치 위험
- 별도 테이블 관리 필요
- **거부 이유**: 과거 시점 재구성 불가능

### 2. 데이터베이스 트리거
**장점**:
- 자동 이력 기록
- 애플리케이션 코드 변경 최소

**단점**:
- 데이터베이스 종속적
- 성능 저하
- 디버깅 어려움
- **거부 이유**: 비즈니스 로직이 DB에 숨겨짐

### 3. CDC (Change Data Capture)
**장점**:
- 애플리케이션 코드 변경 없음
- 모든 변경 자동 캡처

**단점**:
- 인프라 복잡도 증가
- 비즈니스 의미 손실
- **거부 이유**: 비즈니스 이벤트와 DB 변경은 다름

## 결과 (Consequences)

### 긍정적 영향 ✅
1. **완전한 감사 추적**: 모든 상태 변경 이력 보존
2. **시간 여행**: 과거 특정 시점의 상태 재구성 가능
3. **디버깅 용이**: 이벤트 재생으로 장애 원인 파악
4. **규제 준수**: 금융 거래 이력 영구 보관
5. **이벤트 기반 분석**: 비즈니스 인사이트 도출

### 부정적 영향 ⚠️
1. **저장 공간**: 모든 이벤트 저장으로 디스크 사용량 증가
2. **복잡도**: 이벤트 재생 로직 구현 필요
3. **성능**: 이벤트 저장 오버헤드
4. **스키마 진화**: 이벤트 구조 변경 시 호환성 관리 필요

### 완화 전략
1. **스냅샷**: 주기적으로 현재 상태 저장
   ```java
   // 100개 이벤트마다 스냅샷 생성
   if (sequence % 100 == 0) {
       snapshotRepository.save(new PaymentSnapshot(payment));
   }
   ```

2. **이벤트 압축**: 오래된 이벤트 압축 저장
   ```java
   @Scheduled(cron = "0 0 2 * * *")  // 매일 새벽 2시
   public void compressOldEvents() {
       // 1년 이상 된 이벤트 압축
   }
   ```

3. **이벤트 버전 관리**: 스키마 변경 대응
   ```java
   public class PaymentEvent {
       private String eventType;
       private Integer version;  // 이벤트 버전
       private String eventData;
   }
   ```

## 실제 적용 사례

### 1. 결제 이벤트 소싱
**저장되는 이벤트**:
- `PaymentCreated`: 결제 생성
- `PaymentApproving`: 결제 승인 중
- `PaymentApproved`: 결제 승인 완료
- `PaymentFailed`: 결제 실패
- `PaymentCancelling`: 결제 취소 중
- `PaymentCancelled`: 결제 취소 완료

**통계**:
- 평균 이벤트 수: 결제당 3-5개
- 저장 공간: 이벤트당 약 1KB
- 재구성 시간: 100개 이벤트 기준 50ms

### 2. 에스크로 이벤트 소싱
**저장되는 이벤트**:
- `EscrowCreated`: 거래 생성
- `DepositConfirmed`: 입금 확인
- `VehicleDelivered`: 차량 인도
- `VehicleVerified`: 차량 검증
- `OwnershipTransferred`: 명의 이전
- `EscrowCompleted`: 거래 완료

**통계**:
- 평균 이벤트 수: 거래당 6-10개
- 저장 공간: 이벤트당 약 2KB
- 재구성 시간: 10개 이벤트 기준 30ms

## API 엔드포인트

### 이벤트 히스토리 조회
```bash
# 결제 이벤트 히스토리
GET /api/logs/payments/{paymentId}/history

# 특정 시점 상태 재구성
GET /api/logs/payments/{paymentId}/state?sequence=3

# 에스크로 이벤트 히스토리
GET /api/escrow/{transactionId}/events

# 특정 시점 상태 재구성
GET /api/escrow/{transactionId}/events/{sequence}
```

### 응답 예시
```json
{
  "paymentId": "PAY-001",
  "events": [
    {
      "sequence": 1,
      "eventType": "PaymentCreated",
      "occurredAt": "2025-11-28T10:00:00",
      "userId": "user001",
      "data": {
        "orderId": "ORD-001",
        "amount": 50000,
        "status": "PENDING"
      }
    },
    {
      "sequence": 2,
      "eventType": "PaymentApproved",
      "occurredAt": "2025-11-28T10:01:00",
      "userId": "user001",
      "data": {
        "status": "APPROVED",
        "approvedAt": "2025-11-28T10:01:00"
      }
    }
  ]
}
```

## 모니터링

### 주요 메트릭
- 이벤트 저장 속도 (events/sec)
- 이벤트 저장소 크기 (GB)
- 이벤트 재생 시간 (ms)
- 스냅샷 생성 빈도

### 대시보드
```
http://localhost:8080/logs/dashboard
```
- 실시간 이벤트 스트림
- 이벤트 타입별 통계
- 서비스별 이벤트 발행 현황

## 참고 자료
- [Event Sourcing - Martin Fowler](https://martinfowler.com/eaaDev/EventSourcing.html)
- [Versioning in an Event Sourced System - Greg Young](https://leanpub.com/esversioning)
- [Event Store Documentation](https://www.eventstore.com/event-sourcing)

## 관련 ADR
- ADR-002: Kafka 메시지 브로커 선택
- ADR-003: Saga 패턴 적용
- ADR-004: DDD 전술적 패턴 적용
