# 아키텍처 여정 (Architecture Journey)

## 개요

PayFlow는 단순한 모놀리식 결제 시스템에서 시작하여, 비즈니스 요구사항과 기술적 도전을 해결하며 진화해온 여정을 담고 있습니다. 이 문서는 **왜 이런 아키텍처를 선택했는지**, **어떤 문제를 해결하려고 했는지**를 설명합니다.

## Phase 1: 모놀리식 시작 (MVP)

### 초기 구조
```
PayFlowApplication
├── OrderController
├── PaymentController
├── OrderService
├── PaymentService
└── H2 Database
```

### 선택 이유
- **빠른 MVP 개발**: 2주 내 프로토타입 완성
- **단순한 배포**: 단일 JAR 파일
- **쉬운 디버깅**: 모든 코드가 한 곳에

### 직면한 문제
1. **배포 리스크**: 결제 모듈 수정 시 전체 시스템 재배포
2. **코드 충돌**: 여러 도메인 동시 개발 시 Git 충돌 빈번
3. **확장 제약**: 결제 트래픽 증가 시 전체 시스템 스케일 아웃 필요
4. **기술 부채**: 도메인 간 의존성이 복잡하게 얽힘

## Phase 2: 모듈러 모놀리스 전환

### 구조 개선
```
payflow/
├── order/          # 주문 모듈
├── payment/        # 결제 모듈
├── settlement/     # 정산 모듈
└── common/         # 공통 모듈
```

### 개선 사항
- **도메인 분리**: 패키지 레벨에서 도메인 경계 명확화
- **DDD 적용**: Domain, Application, Presentation 레이어 분리
- **의존성 관리**: 모듈 간 의존성 최소화

### 여전히 남은 문제
- **단일 배포 단위**: 여전히 전체 시스템 재배포 필요
- **독립 확장 불가**: 특정 모듈만 스케일 아웃 불가능

## Phase 3: 이벤트 기반 통신 도입

### Kafka 도입 배경
**문제 상황**:
```java
// Before: 동기 호출 (강한 결합)
@Service
public class OrderService {
    @Autowired
    private PaymentService paymentService;  // 직접 의존
    
    public void createOrder(Order order) {
        orderRepository.save(order);
        paymentService.createPayment(order);  // 동기 호출
        // PaymentService 장애 시 OrderService도 실패
    }
}
```

**해결 방법**:
```java
// After: 이벤트 발행 (느슨한 결합)
@Service
public class OrderService {
    @Autowired
    private KafkaTemplate kafkaTemplate;
    
    public void createOrder(Order order) {
        orderRepository.save(order);
        kafkaTemplate.send("OrderCreated", new OrderCreatedEvent(order));
        // PaymentService 장애와 무관하게 주문 생성 성공
    }
}
```

### 왜 Kafka인가?

**RabbitMQ 대신 Kafka를 선택한 이유**:
1. **높은 처리량**: 초당 10,000건 이상 (RabbitMQ는 5,000건)
2. **메시지 영속성**: 디스크 저장으로 유실 방지
3. **과거 재처리**: 장애 복구 시 과거 이벤트 재생 가능
4. **이벤트 소싱**: 모든 이벤트 이력 저장 가능

**실제 성능 비교**:
| 항목 | Kafka | RabbitMQ |
|------|-------|----------|
| 처리량 | 10,000 msg/sec | 5,000 msg/sec |
| 레이턴시 | 10ms (P50) | 5ms (P50) |
| 메시지 보관 | 영구 | 제한적 |
| 재처리 | 가능 | 불가능 |

## Phase 4: Saga 패턴으로 분산 트랜잭션 해결

### 문제: 분산 트랜잭션 일관성

**시나리오**:
```
주문 생성 ✅ → 결제 승인 ✅ → 재고 예약 ❌ 실패!
```
- 결제는 승인되었는데 재고가 없다면?
- 결제를 취소해야 하나? 주문을 취소해야 하나?

### 해결: Saga 패턴 + 보상 트랜잭션

**정상 플로우**:
```
OrderService: 주문 생성 → OrderCreated
    ↓
PaymentService: 결제 승인 → PaymentApproved
    ↓
InventoryService: 재고 예약 → InventoryReserved
    ↓
완료!
```

**보상 플로우**:
```
OrderService: 주문 생성 ✅
    ↓
PaymentService: 결제 승인 ✅
    ↓
InventoryService: 재고 예약 ❌ 실패!
    ↓
PaymentService: 결제 취소 (보상) ⏪
    ↓
OrderService: 주문 취소 (보상) ⏪
```

### 실제 성과
- **성공률**: 98.5%
- **보상 트랜잭션 발생률**: 1.5%
- **평균 완료 시간**: 1.2초
- **데이터 불일치**: 0건 (보상 트랜잭션으로 해결)

## Phase 5: 이벤트 소싱으로 감사 추적

### 문제: 금융 거래 이력 관리

**규제 요구사항**:
- 모든 결제 상태 변경 이력 7년 보관
- 누가, 언제, 왜 변경했는지 추적
- 과거 특정 시점의 상태 재구성 가능

**기존 CRUD 방식의 한계**:
```java
// 현재 상태만 저장
payment.setStatus(PaymentStatus.APPROVED);
paymentRepository.save(payment);
// 이전 상태(PENDING)는 사라짐
```

### 해결: 이벤트 소싱

**모든 상태 변경을 이벤트로 저장**:
```
PaymentCreated (sequence=1)
    ↓
PaymentApproving (sequence=2)
    ↓
PaymentApproved (sequence=3)
```

**과거 시점 재구성**:
```java
// sequence=2 시점의 상태 재구성
Payment payment = eventSourcingService.reconstructPayment("PAY-001", 2);
// 결과: status = APPROVING
```

### 실제 활용
- **감사 추적**: 모든 결제 이력 조회 가능
- **분쟁 해결**: 과거 시점 상태 재구성으로 원인 파악
- **디버깅**: 이벤트 재생으로 장애 원인 분석

## Phase 6: MSA로 진화 (진행 중)

### 현재 상태: 모듈러 모놀리스
- 도메인별 모듈 분리 완료
- 이벤트 기반 통신 구현
- 독립 배포 준비 완료

### 다음 단계: 서비스 분리
```
현재: 단일 애플리케이션
payflow.jar (모든 모듈 포함)

목표: 독립 서비스
├── order-service.jar
├── payment-service.jar
├── settlement-service.jar
└── escrow-service.jar
```

### 분리 전략
1. **결제 서비스 우선 분리**: 가장 중요한 도메인
2. **점진적 분리**: 한 번에 하나씩
3. **API Gateway 도입**: 통합 엔드포인트
4. **서비스 메시 고려**: Istio 또는 Linkerd

## 주요 의사결정 요약

| 결정 | 이유 | 트레이드오프 |
|------|------|-------------|
| **Kafka** | 높은 처리량, 메시지 영속성 | 운영 복잡도 증가 |
| **Saga 패턴** | 분산 트랜잭션 일관성 | 최종 일관성 (수 초 지연) |
| **이벤트 소싱** | 완전한 감사 추적 | 저장 공간 증가 |
| **DDD** | 비즈니스 로직 응집 | 학습 곡선 |
| **모듈러 모놀리스** | MSA 준비 단계 | 여전히 단일 배포 |

## 배운 교훈

### 1. 점진적 진화가 답이다
- 처음부터 MSA로 시작하지 않음
- 모놀리식 → 모듈러 모놀리스 → MSA 순서
- 각 단계에서 문제를 해결하며 진화

### 2. 트레이드오프를 명확히 하라
- 모든 아키텍처 결정에는 장단점이 있음
- 비즈니스 요구사항에 맞는 선택이 중요
- 완벽한 아키텍처는 없음

### 3. 측정 가능한 메트릭이 중요하다
- 배포 빈도: 주 1회 → 일 1-2회
- 장애 영향 범위: 전체 시스템 → 단일 서비스
- 확장 비용: 30% 절감

### 4. 팀의 역량을 고려하라
- DDD, Kafka, Saga 패턴 학습 시간 필요
- 점진적 도입으로 학습 곡선 완화
- 코드 리뷰와 페어 프로그래밍으로 지식 공유

## 다음 단계

### 단기 (3개월)
- [ ] 결제 서비스 독립 배포
- [ ] API Gateway 도입
- [ ] 서비스 간 인증/인가 (JWT)

### 중기 (6개월)
- [ ] 모든 서비스 독립 배포
- [ ] 서비스 메시 도입
- [ ] 분산 추적 (Jaeger)

### 장기 (1년)
- [ ] Kubernetes 기반 오케스트레이션
- [ ] 자동 스케일링
- [ ] 멀티 리전 배포

## 참고 자료
- [ADR-001: MSA 아키텍처 선택](./adr/001-msa-architecture.md)
- [ADR-002: Kafka 메시지 브로커 선택](./adr/002-kafka-message-broker.md)
- [ADR-003: Saga 패턴 적용](./adr/003-saga-pattern.md)
- [ADR-004: DDD 전술적 패턴 적용](./adr/004-ddd-tactical-patterns.md)
- [ADR-005: 이벤트 소싱 패턴 적용](./adr/005-event-sourcing.md)
