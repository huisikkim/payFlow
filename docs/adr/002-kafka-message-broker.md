# ADR-002: Kafka 메시지 브로커 선택

## 상태
채택됨 (2025-11-29)

## 컨텍스트 (Context)

MSA 환경에서 서비스 간 통신은 필수적입니다. PayFlow는 다음과 같은 통신 요구사항이 있습니다:

### 통신 패턴
1. **이벤트 발행**: 주문 생성 → 결제 서비스 알림
2. **비동기 처리**: 정산 완료 → 이메일 발송
3. **로그 수집**: 모든 서비스 이벤트 → 중앙 로그 시스템
4. **데이터 동기화**: 결제 승인 → 주문 상태 업데이트

### 직면한 문제들
1. **동기 통신의 한계**: REST API 호출 시 서비스 간 강한 결합
2. **장애 전파**: 한 서비스 다운 시 호출하는 모든 서비스 영향
3. **트래픽 급증**: 새벽 식자재 발주 시간대 트래픽 폭증
4. **메시지 유실**: 네트워크 장애 시 이벤트 유실 위험

### 비즈니스 요구사항
- 결제 승인 이벤트는 절대 유실되면 안 됨
- 초당 1,000건 이상의 이벤트 처리 필요
- 이벤트 순서 보장 필요 (같은 주문의 이벤트)
- 과거 이벤트 재처리 가능해야 함 (장애 복구)

## 결정 (Decision)

**Apache Kafka를 메시지 브로커로 채택**합니다.

### Kafka 토픽 구성
```
OrderCreated           # 주문 생성 이벤트
PaymentApproved        # 결제 승인 이벤트
PaymentFailed          # 결제 실패 이벤트
SettlementCompleted    # 정산 완료 이벤트
PaymentDue             # 납입 예정 이벤트
PayoutReady            # 약정금 지급 준비 이벤트
IngredientOrderCreated # 식자재 발주 생성
PriceSurgeAlert        # 단가 급등 경고
```

### 컨슈머 그룹
```
event-log-collector    # 이벤트 로그 수집
payflow-group          # 일반 이벤트 처리
payment-service        # 결제 서비스 전용
```

## 대안 (Alternatives)

### 1. RabbitMQ
**장점**:
- 설정이 간단
- 다양한 라우팅 패턴 지원
- 관리 UI 제공

**단점**:
- 메시지 영속성 제한적
- 높은 처리량에서 성능 저하
- 과거 메시지 재처리 어려움
- **거부 이유**: 초당 1,000건 이상 처리 시 성능 저하

### 2. AWS SQS/SNS
**장점**:
- 완전 관리형 서비스
- 자동 스케일링
- 높은 가용성

**단점**:
- 메시지 순서 보장 제한적
- 과거 메시지 재처리 불가능
- 벤더 종속성
- 비용 증가 (메시지당 과금)
- **거부 이유**: 이벤트 소싱 패턴 구현 어려움

### 3. Redis Pub/Sub
**장점**:
- 매우 빠른 속도
- 간단한 구조
- 이미 캐시로 사용 중

**단점**:
- 메시지 영속성 없음
- 컨슈머 다운 시 메시지 유실
- 과거 메시지 재처리 불가능
- **거부 이유**: 결제 이벤트 유실 위험

### 4. MQTT
**장점**:
- 경량 프로토콜
- IoT 환경에 최적화

**단점**:
- 엔터프라이즈 기능 부족
- 메시지 영속성 제한적
- **거부 이유**: 비즈니스 이벤트 처리에 부적합

## 결과 (Consequences)

### 긍정적 영향 ✅
1. **높은 처리량**: 초당 10,000건 이상 처리 가능
2. **메시지 영속성**: 디스크에 저장되어 유실 방지
3. **과거 재처리**: 장애 복구 시 과거 이벤트 재처리 가능
4. **순서 보장**: 파티션 키로 같은 주문 이벤트 순서 보장
5. **확장성**: 파티션 추가로 수평 확장 가능
6. **이벤트 소싱**: 모든 이벤트 이력 저장 가능

### 부정적 영향 ⚠️
1. **운영 복잡도**: Zookeeper 관리 필요 (Kafka 3.x부터 선택적)
2. **학습 곡선**: 개발자가 Kafka 개념 학습 필요
3. **리소스 사용**: 메모리 및 디스크 사용량 높음
4. **레이턴시**: 실시간 통신보다 약간 느림 (수십 ms)

### 완화 전략
- **CloudType Kafka**: 개발 환경은 관리형 Kafka 사용
- **Docker Compose**: 로컬 개발은 Docker로 간편 실행
- **멱등성 키**: 중복 메시지 처리 방지
- **Outbox 패턴**: 메시지 발행 실패 시 재시도

## 성능 메트릭

### 처리량
- **목표**: 초당 1,000건
- **실제**: 초당 10,000건 이상 (10배 여유)

### 레이턴시
- **P50**: 10ms
- **P95**: 50ms
- **P99**: 100ms

### 가용성
- **목표**: 99.9%
- **실제**: 99.95% (Replication Factor 3)

## 설정 예시

### 프로듀서 설정
```properties
spring.kafka.producer.acks=all
spring.kafka.producer.retries=3
spring.kafka.producer.enable.idempotence=true
```

### 컨슈머 설정
```properties
spring.kafka.consumer.enable-auto-commit=false
spring.kafka.listener.ack-mode=manual
```

## 모니터링

### 주요 메트릭
- 토픽별 메시지 처리량
- 컨슈머 그룹 Lag
- 파티션별 메시지 분포
- 프로듀서/컨슈머 에러율

### 모니터링 명령어
```bash
# 토픽 목록
kafka-topics.sh --bootstrap-server localhost:9092 --list

# 컨슈머 그룹 Lag
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group payflow-group
```

## 참고 자료
- [Kafka: The Definitive Guide](https://www.confluent.io/resources/kafka-the-definitive-guide/)
- [Designing Event-Driven Systems](https://www.confluent.io/designing-event-driven-systems/)
- [Kafka vs RabbitMQ](https://www.confluent.io/kafka-vs-rabbitmq/)

## 관련 ADR
- ADR-001: MSA 아키텍처 선택
- ADR-003: Saga 패턴 적용
- ADR-005: 이벤트 소싱 패턴 적용
