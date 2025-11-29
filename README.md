# PayFlow - MSA + EDA + DDD 기반 결제 시스템

> 토스페이먼츠를 연동한 실전형 결제 시스템 MVP입니다.

## 📖 포트폴리오 문서

**채용 담당자님께**: 이 프로젝트의 **아키텍처 의사결정 과정**과 **기술적 도전 해결 방법**이 궁금하시다면:

👉 **[포트폴리오 README 보기](./README_PORTFOLIO.md)** 👈

### 주요 내용
- 🏗️ **아키텍처 여정**: 모놀리식 → 모듈러 모놀리스 → MSA 진화 과정
- 🔑 **주요 의사결정 (ADR)**: 왜 MSA? 왜 Kafka? 왜 Saga 패턴?
- 💡 **기술적 도전과 해결**: 분산 트랜잭션, 이벤트 유실, 분산 추적 등
- 📊 **성능 & 안정성**: TPS 10,000+, 가용성 99.95%, 데이터 불일치 0건

### 문서 구조
```
docs/
├── ARCHITECTURE_JOURNEY.md      # 아키텍처 진화 과정
├── TECHNICAL_CHALLENGES.md      # 기술적 도전과 해결
└── adr/                          # Architecture Decision Records
    ├── 001-msa-architecture.md
    ├── 002-kafka-message-broker.md
    ├── 003-saga-pattern.md
    ├── 004-ddd-tactical-patterns.md
    └── 005-event-sourcing.md
```

---

### 1. Kafka 환경 설정

PayFlow는 **로컬 개발**과 **개발 서버** 두 가지 카프카 환경을 지원합니다.

#### 로컬 환경 (Docker Kafka)

**Docker Compose 사용:**
```bash
docker-compose up -d
```

**또는 개별 실행:**
```bash
# macOS (Homebrew)
brew install kafka
brew services start zookeeper
brew services start kafka

# Docker
docker run -d --name zookeeper -p 2181:2181 zookeeper:3.7
docker run -d --name kafka -p 9092:9092 \
  -e KAFKA_ZOOKEEPER_CONNECT=host.docker.internal:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  confluentinc/cp-kafka:latest
```

**로컬 환경으로 실행:**
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

#### 개발 서버 (CloudType Kafka)

**설정 정보:**
- 호스트: `svc.sel5.cloudtype.app:30851`
- 환경: 개발/테스트용
- 설정 파일: `src/main/resources/application-dev.properties`

**개발 서버로 실행:**
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
# 또는 환경변수 없이 실행 (기본값: dev)
./gradlew bootRun
```

#### Kafka 토픽 구성

현재 프로젝트에서 사용하는 토픽:
- `OrderCreated` - 주문 생성 이벤트
- `PaymentApproved` - 결제 승인 이벤트
- `PaymentFailed` - 결제 실패 이벤트
- `SettlementCompleted` - 정산 완료 이벤트
- `PaymentDue` - 납입 예정 이벤트
- `PayoutReady` - 약정금 지급 준비 이벤트

#### Kafka 설정 상태 (개발 서버)

**파티션 및 복제:**
- 파티션 수: 1개 (개발/테스트 환경)
- Replication Factor: 1 (단일 브로커)
- ⚠️ 프로덕션 환경에서는 파티션 수 증가 및 복제본 2~3 이상 권장

**컨슈머 그룹:**
- `event-log-collector` - 이벤트 로그 수집
- `payflow-group` - 일반 이벤트 처리
- `payment-service` - 결제 서비스 전용

**모니터링 명령어 (CloudType 터미널):**
```bash
# 토픽 목록 확인
/opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list

# 토픽 상세 정보
/opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe

# 컨슈머 그룹 목록
/opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list

# 컨슈머 그룹 상세 정보
/opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group event-log-collector

# 메시지 확인
/opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic PaymentApproved --from-beginning
```

### 2. 토스페이먼츠 테스트 키 발급

1. [토스페이먼츠 개발자센터](https://developers.tosspayments.com/) 접속
2. 회원가입 후 로그인
3. 내 개발 정보 > API 키 발급
4. `application.properties`에 키 입력:
```properties
toss.payments.secret-key=test_sk_YOUR_SECRET_KEY
toss.payments.client-key=test_ck_YOUR_CLIENT_KEY
```

## 실행 방법

### 1. 프로젝트 빌드
```bash
./gradlew clean build
```

### 2. 애플리케이션 실행
```bash
./gradlew bootRun
```

### 3. 브라우저에서 접속
```
http://localhost:8080
```

## 주요 기능 흐름

### 결제 프로세스
```
1. 사용자가 결제 요청
   ↓
2. Order Service: 주문 생성 (PENDING)
   ↓
3. Kafka: OrderCreated 이벤트 발행
   ↓
4. Payment Service: 이벤트 수신 → 결제 정보 생성
   ↓
5. 토스페이먼츠 결제창 호출
   ↓
6. 사용자 결제 완료
   ↓
7. Payment Service: 토스 API로 결제 승인
   ↓
8. Order Service: 주문 상태 변경 (CONFIRMED)
```

## API 엔드포인트

### Authentication (인증)
- `POST /api/auth/signup` - 회원가입
- `POST /api/auth/login` - 로그인 (JWT 토큰 발급)

### User (사용자)
- `GET /api/user/profile` - 사용자 프로필 조회 (인증 필요)
- `GET /api/admin/dashboard` - 관리자 대시보드 (ADMIN 권한 필요)

### Order Service (인증 필요)
- `POST /api/orders` - 주문 생성
- `GET /api/orders/{orderId}` - 주문 조회

### Payment Service (인증 필요)
- `POST /api/payments/confirm` - 결제 승인
- `GET /api/payments/{orderId}` - 결제 조회
- `GET /api/stages/payments/my` - 내 결제 내역
- `GET /api/stages/payouts/my` - 내 약정금 내역
- `POST /api/stages/payouts/{id}/complete` - 약정금 지급 완료 처리
- `POST /api/stages/{id}/settlement` - 정산 생성
- `GET /api/stages/{id}/settlement` - 정산 조회
- `GET /api/stages/{id}/settlement/my` - 내 정산 내역
- `GET /api/stages/settlement/my` - 내 전체 정산 내역

### Chatbot Service (챗봇)
- `POST /api/chatbot/chat` - 채팅 메시지 전송
- `GET /api/chatbot/conversations/{id}/history` - 대화 히스토리 조회
- `POST /api/chatbot/conversations/{id}/close` - 대화 종료
- `GET /api/chatbot/health` - 챗봇 서비스 상태 확인

### Web UI
- `GET /` - 결제 페이지
- `GET /success` - 결제 성공 페이지
- `GET /fail` - 결제 실패 페이지
- `GET /stages/{id}/settlement` - 정산 대시보드 페이지
- `GET /chatbot` - 챗봇 페이지

## 데이터베이스

H2 Console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:payflowdb`
- Username: `sa`
- Password: (비워두기)

### Kafka 테스트

**로컬에서 카프카 연결 테스트:**
```bash
# 테스트 이벤트 발행
curl -X POST "http://localhost:8080/api/test/kafka?eventType=PaymentApproved"

# 응답 예시
이벤트 발행 완료: PaymentApproved (eventId: xxx-xxx-xxx)
```

**Spring Boot 로그에서 확인:**
```
📨 이벤트 발행: topic=PaymentApproved, eventId=...
Event collected: PaymentApproved from payment
```

## Spring Security 인증/인가

### 기본 사용자 계정
애플리케이션 시작 시 자동으로 생성됩니다:

**일반 사용자:**
- Username: `user`
- Password: `password`
- Role: `ROLE_USER`

**관리자:**
- Username: `admin`
- Password: `admin`
- Role: `ROLE_USER`, `ROLE_ADMIN`

### JWT 인증 사용법

#### 1. 회원가입
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "password123",
    "email": "newuser@example.com"
  }'
```

#### 2. 로그인 (JWT 토큰 발급)
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "password"
  }'
```

응답:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "username": "user"
}
```

#### 3. 인증이 필요한 API 호출
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "productName": "테스트 상품",
    "amount": 10000
  }'
```

### 권한 체계
- **ROLE_USER**: 일반 사용자 권한 (주문, 결제 API 접근)
- **ROLE_ADMIN**: 관리자 권한 (모든 API 접근 + 관리자 전용 API)

### 보안 테스트
```bash
./test-security-api.sh
```

이 스크립트는 다음을 테스트합니다:
- 회원가입
- 로그인 (일반 사용자 / 관리자)
- 인증된 API 호출
- 권한 기반 접근 제어
- 인증 실패 시나리오

## 🤖 Career Mentor - 시니어 개발자의 챗봇

### 주요 특징
- ✅ **버튼 기반 Q&A**: 주요 질문을 버튼으로 제공하여 쉽게 선택
- ✅ **실전 경험 공유**: 실무 경험을 바탕으로 한 구체적인 조언
- ✅ **다양한 주제**: 이직 전략, 아키텍처, 면접, 연봉 협상 등
- ✅ **DDD 패턴**: Domain, Application, Presentation 레이어 분리
- ✅ **EDA 적용**: Kafka를 통한 이벤트 발행
- ✅ **대화 컨텍스트 관리**: 사용자의 대화 상태 추적

### 질문 가능한 주제

#### 💼 자기소개
- 개발자의 경력과 강점
- 주요 프로젝트 경험
- 기술적 전문성

#### 🏗️ 아키텍처 경험
- MSA (Microservices Architecture)
- EDA (Event-Driven Architecture)
- DDD (Domain-Driven Design)
- Saga Pattern

#### 🔧 레거시 현대화
- 모놀리스 → 모듈러 모놀리스 → MSA 전환
- 점진적 리팩토링 전략
- 이벤트 기반 통신 도입
- 기술 부채 관리

#### 💻 기술 스택
- Backend: Java, Spring Boot, PHP, Node.js
- Architecture: MSA, EDA, DDD
- Message: Kafka, RabbitMQ, MQTT
- Database: MySQL, PostgreSQL, MongoDB, Redis
- Blockchain: Solidity, Web3j
- DevOps: AWS, Docker, Jenkins, GitHub Actions


### 챗봇 사용하기

**웹 UI:**
```
http://localhost:8080/chatbot
```
또는 메인 페이지 우측 하단의 💬 버튼 클릭!

**API 테스트:**
```bash
./test-chatbot-api.sh
```

## Saga 패턴 (보상 트랜잭션)

PayFlow는 분산 트랜잭션 관리를 위한 **Saga 패턴**을 구현.

### 주요 기능
- ✅ 주문 → 결제 → 재고 예약의 순차적 트랜잭션
- ✅ 실패 시 자동 보상 트랜잭션 (Rollback)
- ✅ Saga 상태 추적 및 모니터링
- ✅ 에러 처리 및 로깅

### Saga 테스트
```bash
./test-saga.sh
```

## 📊 로그 수집·분석 시스템

PayFlow는  로그 수집 및 분석 시스템 을 구현합니다.

### 주요 기능

#### 1. 실시간 이벤트 로그 수집
- ✅ 모든 비즈니스 이벤트를 Kafka 토픽으로 전송
- ✅ 주문, 결제, 정산 서비스의 이벤트를 JSON 형식으로 발행
- ✅ Kafka Consumer를 통한 중앙 집중식 로그 집계

#### 2. Correlation ID 기반 분산 추적
- ✅ 하나의 요청이 여러 서비스를 거치는 과정을 추적
- ✅ HTTP 헤더 `X-Correlation-ID`를 통한 자동 추적
- ✅ 전체 이벤트 체인 조회 가능

#### 3. 이벤트 소싱 패턴
- ✅ 결제 도메인에 이벤트 소싱 적용
- ✅ 결제 상태 변경 이력을 순차적으로 저장
  - `PENDING` → `APPROVING` → `APPROVED`
  - `PENDING` → `APPROVING` → `FAILED`
- ✅ 특정 시점의 결제 상태 재구성 가능
- ✅ Event Store를 별도 테이블로 구성

#### 4. 로그 분석 대시보드
- ✅ 실시간 메트릭 모니터링
  - 시간대별 이벤트 건수
  - 이벤트 타입별 통계
  - 서비스별 성공률
  - 평균 처리 시간
- ✅ 실시간 이벤트 스트림 (최근 이벤트 조회)
- ✅ 시각화된 차트 및 그래프

### API 엔드포인트

```bash
# 대시보드 메트릭 조회
GET /api/logs/dashboard/metrics?hours=24

# 실시간 이벤트 스트림
GET /api/logs/events/recent?limit=50

# Correlation ID로 이벤트 체인 추적
GET /api/logs/events/trace/{correlationId}

# 결제 이벤트 히스토리 (이벤트 소싱)
GET /api/logs/payments/{paymentId}/history

# 특정 시점의 결제 상태 재구성
GET /api/logs/payments/{paymentId}/state?sequence=3

# 사용자별 이벤트 조회
GET /api/logs/events/user/{userId}
```

### 대시보드 접속

```
http://localhost:8080/logs/dashboard
```

### 로그 시스템 테스트

```bash
./test-logging-api.sh
```

### 아키텍처

```
[Order Service] ──┐
                  │
[Payment Service] ├──> Kafka Topics ──> Event Log Consumer ──> H2 Database
                  │                                              (event_logs)
[Stage Service] ──┘                                              (payment_event_store)
                                                                        │
                                                                        ▼
                                                                 Log Analytics API
                                                                        │
                                                                        ▼
                                                                  Dashboard UI
```

### 기술 스택
- **이벤트 수집**: Kafka (비동기 메시징)
- **로그 저장소**: H2 Database (중앙 집중식)
- **이벤트 소싱**: JPA Event Store
- **분산 추적**: Correlation ID (MDC)
- **분석**: Spring Data JPA Aggregation
- **시각화**: Thymeleaf + Vanilla JS

### 포인트

1. **EDA (Event-Driven Architecture)**
   - Kafka를 활용한 비동기 이벤트 처리
   - 서비스 간 느슨한 결합

2. **분산 시스템 추적**
   - Correlation ID를 통한 분산 트랜잭션 추적
   - MSA 환경에서의 디버깅 능력

3. **이벤트 소싱 패턴**
   - 상태 변경 이력 관리
   - 시간 여행 (Time Travel) 가능
   - 감사(Audit) 로그 자동 생성

4. **데이터 분석**
   - 실시간 메트릭 집계
   - 성능 모니터링
   - 비즈니스 인사이트 도출

5. **운영 효율성**
   - 중앙 집중식 로그 관리
   - 장애 추적 및 디버깅
   - SLA 모니터링

## 🚛 에스크로 결제 시스템

PayFlow는 **중고차 거래를 위한 안전한 에스크로 시스템**을 구현합니다.

### 주요 특징

#### 1. 토스 페이먼츠 통합
- ✅ **카드 결제**: 실시간 카드 결제 및 즉시 입금 처리
- ✅ **가상계좌 입금**: 가상계좌 발급 및 입금 대기
- ✅ **웹훅 자동 처리**: 입금 완료 시 자동으로 거래 진행
- ✅ 토스 결제 위젯을 통한 안전한 결제
- ✅ 테스트 환경에서 전체 플로우 검증 가능

#### 2. 완전한 거래 생명주기 관리
```
1. INITIATED           → 거래 생성
2. DEPOSITED          → 입금 완료 (토스 결제)
3. DELIVERED          → 차량 인도
4. VERIFIED           → 차량 검증
5. OWNERSHIP_TRANSFERRED → 명의 이전
6. COMPLETED          → 정산 완료 (판매자 지급)
```

#### 3. 안전장치
- ✅ **에스크로 보관**: 입금된 금액은 모든 조건 충족 시까지 보관
- ✅ **단계별 검증**: 각 단계마다 필수 조건 확인
- ✅ **분쟁 처리**: 문제 발생 시 분쟁 제기 및 해결
- ✅ **자동 환불**: 거래 취소 시 구매자에게 자동 환불

#### 4. DDD 패턴 적용
```
domain/
  ├── EscrowTransaction    # 에스크로 거래 집합 루트
  ├── Deposit             # 입금 엔티티
  ├── Verification        # 검증 엔티티
  ├── Settlement          # 정산 엔티티
  └── Dispute             # 분쟁 엔티티

application/
  ├── EscrowService           # 거래 관리
  ├── EscrowPaymentService    # 토스 결제 통합
  ├── DepositService          # 입금 처리
  ├── VerificationService     # 검증 처리
  ├── SettlementService       # 정산 처리
  └── DisputeService          # 분쟁 처리
```

#### 5. 이벤트 소싱 & EDA
- ✅ 모든 상태 변경을 이벤트로 기록
- ✅ Kafka를 통한 이벤트 발행
- ✅ 이벤트 히스토리 조회 가능
- ✅ 특정 시점의 거래 상태 재구성

### 에스크로 API 엔드포인트

#### 거래 관리
```bash
# 거래 생성
POST /api/escrow

# 거래 조회
GET /api/escrow/{transactionId}

# 구매자별 거래 목록
GET /api/escrow/buyer/{buyerId}

# 판매자별 거래 목록
GET /api/escrow/seller/{sellerId}

# 상태별 거래 목록
GET /api/escrow/status/{status}

# 거래 취소
DELETE /api/escrow/{transactionId}?reason=취소사유
```

#### 입금 처리

**카드 결제:**
```bash
# 토스 결제 페이지 (카드)
GET /escrow/{transactionId}/payment

# 결제 승인 및 입금 처리
POST /api/escrow/{transactionId}/payment/confirm

# 입금 내역 조회 (API)
GET /api/escrow/{transactionId}/deposits

# 입금 내역 조회 (웹 UI)
GET /escrow/{transactionId}/deposits
```

**가상계좌 입금:**
```bash
# 가상계좌 발급 페이지
GET /escrow/{transactionId}/payment/virtual-account

# 가상계좌 발급 API
POST /api/escrow/{transactionId}/payment/virtual-account/issue

# 가상계좌 입금 완료 (웹훅)
POST /api/escrow/webhook/virtual-account

# 가상계좌 내역 조회 (API)
GET /api/escrow/{transactionId}/virtual-accounts

# 가상계좌 내역 조회 (웹 UI)
GET /escrow/{transactionId}/virtual-accounts

# 웹훅 테스트 페이지 (개발용)
GET /escrow/{transactionId}/webhook-test
```

#### 차량 인도 & 검증
```bash
# 차량 인도 확인
POST /api/escrow/{transactionId}/delivery

# 차량 검증
POST /api/escrow/{transactionId}/verification

# 명의 이전 확인
POST /api/escrow/{transactionId}/ownership-transfer

# 검증 내역 조회
GET /api/escrow/{transactionId}/verifications
```

#### 정산 & 분쟁
```bash
# 정산 시작
POST /api/escrow/{transactionId}/settlement/start

# 정산 완료
POST /api/escrow/{transactionId}/settlement/complete

# 정산 조회 (API)
GET /api/escrow/{transactionId}/settlement

# 정산 조회 (웹 UI)
GET /escrow/{transactionId}/settlement

# 분쟁 제기
POST /api/escrow/{transactionId}/dispute

# 분쟁 해결
POST /api/escrow/disputes/{disputeId}/resolve

# 분쟁 목록
GET /api/escrow/{transactionId}/disputes
```

#### 이벤트 소싱
```bash
# 이벤트 히스토리
GET /api/escrow/{transactionId}/events

# 특정 시점 상태 재구성
GET /api/escrow/{transactionId}/events/{sequence}
```

### 웹 UI

```bash
# 에스크로 거래 목록
http://localhost:8080/escrow

# 거래 생성
http://localhost:8080/escrow/create

# 거래 상세
http://localhost:8080/escrow/{transactionId}

# 카드 입금 결제 페이지
http://localhost:8080/escrow/{transactionId}/payment

# 가상계좌 입금 페이지
http://localhost:8080/escrow/{transactionId}/payment/virtual-account

# 입금 내역 페이지
http://localhost:8080/escrow/{transactionId}/deposits

# 가상계좌 내역 페이지
http://localhost:8080/escrow/{transactionId}/virtual-accounts

# 검증 내역 페이지
http://localhost:8080/escrow/{transactionId}/verifications

# 정산 내역 페이지
http://localhost:8080/escrow/{transactionId}/settlement

# 이벤트 히스토리 페이지
http://localhost:8080/escrow/{transactionId}/events

# 웹훅 테스트 페이지 (개발용)
http://localhost:8080/escrow/{transactionId}/webhook-test
```

### 가상계좌 입금 시스템

#### 주요 기능
- ✅ **가상계좌 발급**: 토스페이먼츠 API를 통한 가상계좌 생성
- ✅ **입금 대기**: 발급된 계좌로 입금 시까지 대기
- ✅ **웹훅 자동 처리**: 입금 완료 시 토스페이먼츠가 웹훅 호출
- ✅ **자동 입금 처리**: 웹훅 수신 시 자동으로 에스크로 입금 처리
- ✅ **입금 기한 관리**: 24시간 입금 기한 설정
- ✅ **취소 처리**: 기한 만료 또는 사용자 취소 시 자동 처리

#### 가상계좌 프로세스
```
1. 사용자가 가상계좌 발급 요청
   ↓
2. 토스페이먼츠 API 호출 → 가상계좌 생성
   (예: 국민은행 12345678901234)
   ↓
3. 가상계좌 정보 DB 저장 (상태: WAITING_FOR_DEPOSIT)
   ↓
4. 사용자가 은행 앱/ATM에서 입금
   ↓
5. 토스페이먼츠가 입금 확인
   ↓
6. 토스페이먼츠가 웹훅 호출 ⭐
   POST /api/escrow/webhook/virtual-account
   {
     "status": "DONE",
     "orderId": "ESCROW-xxx",
     "virtualAccount": {
       "customerName": "홍길동"
     }
   }
   ↓
7. 웹훅 핸들러가 자동 실행
   - 가상계좌 상태 → DONE
   - 에스크로 입금 처리 (Deposit 생성)
   - 에스크로 거래 상태 → DEPOSITED
   ↓
8. 사용자가 프로세스 실행 페이지에서 다음 단계 진행
```

#### 토스페이먼츠 웹훅 설정

**1. 개발자센터 설정:**
```
1. https://developers.tosspayments.com/ 로그인
2. 내 개발 정보 > 웹훅 메뉴
3. 웹훅 URL 추가:
   https://your-domain.com/api/escrow/webhook/virtual-account
4. 이벤트 선택:
   ☑ 결제 상태 변경 (PAYMENT_STATUS_CHANGED)
5. 저장
```

**2. 로컬 테스트 (ngrok 사용):**
```bash
# ngrok 설치 및 실행
brew install ngrok  # Mac
ngrok http 8080

# 생성된 URL을 토스페이먼츠에 등록
# 예: https://abc123.ngrok-free.app/api/escrow/webhook/virtual-account
```

**3. 웹훅 테스트 (개발용):**
```bash
# 브라우저에서 GET 요청 (자동 입금 처리)
https://your-domain.com/api/escrow/webhook/virtual-account

# 또는 웹 UI에서 테스트
http://localhost:8080/escrow/{transactionId}/webhook-test
```

#### 가상계좌 상태
- `WAITING_FOR_DEPOSIT` - 입금 대기 중
- `DONE` - 입금 완료
- `CANCELED` - 취소됨
- `EXPIRED` - 기한 만료

### 에스크로 테스트

#### 전체 플로우 테스트
```bash
# 1. 거래 생성
curl -X POST http://localhost:8080/api/escrow \
  -H "Content-Type: application/json" \
  -d '{
    "buyer": {
      "userId": "buyer001",
      "name": "홍길동",
      "email": "buyer@example.com",
      "phone": "010-1234-5678"
    },
    "seller": {
      "userId": "seller001",
      "name": "김판매",
      "email": "seller@example.com",
      "phone": "010-8765-4321"
    },
    "vehicle": {
      "vin": "KMHXX00XXXX000001",
      "manufacturer": "현대",
      "model": "그랜저",
      "year": 2023,
      "registrationNumber": "12가3456"
    },
    "amount": 50000000,
    "feeRate": 0.03
  }'

# 2-A. 카드 입금 (즉시 처리)
# http://localhost:8080/escrow/{transactionId}/payment
# 토스 테스트 카드: 4330123412341234

# 2-B. 가상계좌 입금 (발급 후 입금 대기)
# http://localhost:8080/escrow/{transactionId}/payment/virtual-account
# 가상계좌 발급 후 입금하면 웹훅으로 자동 처리

# 3. 차량 인도
curl -X POST http://localhost:8080/api/escrow/{transactionId}/delivery \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "{transactionId}",
    "confirmedBy": "seller001",
    "deliveredAt": "2025-11-13T12:00:00",
    "deliveryLocation": "서울시 강남구",
    "deliveryNotes": "차량 인도 완료"
  }'

# 4. 차량 검증
curl -X POST http://localhost:8080/api/escrow/{transactionId}/verification \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "{transactionId}",
    "type": "VEHICLE_CONDITION",
    "result": "PASSED",
    "verifiedBy": "inspector001",
    "notes": "차량 상태 양호",
    "documentId": "DOC-001"
  }'

# 5. 명의 이전
curl -X POST http://localhost:8080/api/escrow/{transactionId}/ownership-transfer \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "{transactionId}",
    "verifiedBy": "inspector001",
    "documentId": "TRANSFER-DOC-001",
    "notes": "명의 이전 완료",
    "newOwnerId": "buyer001",
    "transferDate": "2025-11-13",
    "registrationOffice": "서울시청"
  }'

# 6. 정산 시작 및 완료
curl -X POST http://localhost:8080/api/escrow/{transactionId}/settlement/start
curl -X POST "http://localhost:8080/api/escrow/{transactionId}/settlement/complete?paymentMethod=BANK_TRANSFER&paymentReference=SETTLE-001"
```

#### 자동화된 테스트 스크립트
```bash
./test-escrow-api.sh
```

### 에스크로 이벤트

시스템에서 발행되는 에스크로 관련 이벤트:

- `EscrowCreated` - 거래 생성
- `DepositConfirmed` - 입금 확인
- `VehicleDelivered` - 차량 인도
- `VehicleVerified` - 차량 검증
- `VerificationFailed` - 검증 실패
- `OwnershipTransferred` - 명의 이전
- `EscrowCompleted` - 거래 완료
- `EscrowCancelled` - 거래 취소
- `SettlementFailed` - 정산 실패
- `DisputeRaised` - 분쟁 제기
- `DisputeResolved` - 분쟁 해결

### 이벤트 대시보드에서 확인

```
http://localhost:8080/logs/dashboard
```

에스크로 이벤트가 실시간으로 표시되며, 서비스별 성공률과 처리 시간을 모니터링할 수 있습니다.

### 정리

1. **실제 결제 시스템 통합**
   - 토스 페이먼츠 API 연동
   - 결제 승인 후 자동 입금 처리
   - 실제 테스트 환경에서 검증 가능

2. **비즈니스 로직**
   - 6단계 거래 생명주기
   - 각 단계별 상태 전이 규칙
   - 비즈니스 제약조건 검증

3. **안전한 거래 보장**
   - 에스크로 패턴 구현
   - 조건부 정산 처리
   - 분쟁 처리 메커니즘

4. **이벤트 소싱**
   - 모든 상태 변경 이력 저장
   - 감사 로그 자동 생성
   - 시간 여행 가능

5. **MSA & EDA 적용**
   - 도메인 주도 설계
   - 이벤트 기반 통신
   - 느슨한 결합

## 👤
 근태관리 시스템 (HR Management)

**인사관리** 기본형 근태관리 기능을 추가했습니다.

### 주요 기능

#### 1. 출퇴근 관리
- ✅ **출근 체크**: 버튼 클릭으로 간편한 출근 처리
- ✅ **퇴근 체크**: 출근 후 퇴근 처리 가능
- ✅ **실시간 근태 현황**: 오늘의 출퇴근 시간 표시
- ✅ **월별 근태 기록**: 이번 달 전체 근태 내역 조회
- ✅ **근무 시간 계산**: 출퇴근 시간 기반 자동 계산

#### 2. 휴가 관리
- ✅ **휴가 신청**: 연차, 병가, 개인 사유 등 다양한 휴가 타입
- ✅ **휴가 승인/반려**: 관리자의 휴가 승인 프로세스
- ✅ **잔여 연차 조회**: 실시간 잔여 연차 확인
- ✅ **휴가 내역 관리**: 신청 내역 및 상태 추적
- ✅ **휴가 취소**: 승인 전 휴가 신청 취소 가능

### 휴가 종류
- **연차 (ANNUAL)**: 기본 연차 휴가
- **병가 (SICK)**: 질병으로 인한 휴가
- **개인 사유 (PERSONAL)**: 개인적인 사유
- **출산 휴가 (MATERNITY)**: 출산 관련 휴가
- **육아 휴가 (PATERNITY)**: 육아 관련 휴가
- **무급 휴가 (UNPAID)**: 무급 휴가

### DDD 패턴 적용

```
hr/
├── domain/
│   ├── Attendance.java           # 근태 엔티티
│   ├── AttendanceStatus.java     # 근태 상태 (출근/퇴근)
│   ├── AttendanceRepository.java
│   ├── Leave.java                # 휴가 엔티티
│   ├── LeaveStatus.java          # 휴가 상태 (대기/승인/반려)
│   ├── LeaveType.java            # 휴가 종류
│   └── LeaveRepository.java
├── application/
│   ├── AttendanceService.java    # 근태 비즈니스 로직
│   └── LeaveService.java         # 휴가 비즈니스 로직
└── presentation/
    ├── AttendanceController.java # 근태 API
    ├── LeaveController.java      # 휴가 API
    ├── HrWebController.java      # 웹 페이지 컨트롤러
    └── dto/
        ├── AttendanceResponse.java
        ├── LeaveRequest.java
        └── LeaveResponse.java
```

### API 엔드포인트

#### 근태 관리
```bash
# 출근
POST /api/hr/attendance/check-in

# 퇴근
POST /api/hr/attendance/check-out

# 오늘 근태 조회
GET /api/hr/attendance/today

# 내 근태 내역
GET /api/hr/attendance/my

# 월별 근태 조회
GET /api/hr/attendance/monthly?year=2025&month=11
```

#### 휴가 관리
```bash
# 휴가 신청
POST /api/hr/leaves
{
  "type": "ANNUAL",
  "startDate": "2025-11-20",
  "endDate": "2025-11-22",
  "days": 3,
  "reason": "개인 사유"
}

# 내 휴가 목록
GET /api/hr/leaves/my

# 잔여 연차 조회
GET /api/hr/leaves/remaining-days

# 휴가 승인 (관리자)
POST /api/hr/leaves/{leaveId}/approve

# 휴가 반려 (관리자)
POST /api/hr/leaves/{leaveId}/reject
{
  "reason": "반려 사유"
}

# 대기 중인 휴가 목록 (관리자)
GET /api/hr/leaves/pending

# 휴가 취소
DELETE /api/hr/leaves/{leaveId}
```

### 웹 UI

```
http://localhost:8080/hr/attendance
```

**주요 화면:**
- 출퇴근 관리 탭: 출근/퇴근 버튼, 오늘의 근태, 월별 근태 기록
- 휴가 관리 탭: 휴가 신청 폼, 잔여 연차, 내 휴가 신청 내역

### 테스트

```bash
./test-hr-api.sh
```

이 스크립트는 다음을 테스트합니다:
- 사용자 로그인
- 출근/퇴근 처리
- 근태 조회 (오늘, 월별)
- 휴가 신청
- 잔여 연차 조회
- 관리자 로그인
- 휴가 승인 (관리자)

### 비즈니스 로직

#### 출퇴근 규칙
- 하루에 한 번만 출근 가능
- 출근 후에만 퇴근 가능
- 출퇴근 시간 자동 기록
- 근무 시간 자동 계산

#### 휴가 규칙
- 기본 연차: 15일
- 휴가 신청 시 잔여 연차 차감
- 승인 전까지는 취소 가능
- 관리자만 승인/반려 가능
- 반려 시 사유 필수

### 권한 체계
- **일반 사용자 (USER)**: 출퇴근 체크, 휴가 신청, 본인 내역 조회
- **관리자 (ADMIN)**: 모든 기능 + 휴가 승인/반려, 대기 목록 조회

### 향후 확장 가능 기능
- 급여 관리
- 인사 평가
- 조직도 관리
- 근태 통계 대시보드
- 알림 시스템 (휴가 승인 알림 등)
- 모바일 앱 연동


## 🪙 실시간 코인 시세 (업비트 웹소켓)

PayFlow는 **업비트 웹소켓을 이용한 실시간 코인 시세 조회 기능**

### 주요 특징

#### 1. 실시간 웹소켓 연동
- ✅ **업비트 웹소켓 API**: 실시간 시세 데이터 수신
- ✅ **자동 재연결**: 연결 끊김 시 자동 재연결
- ✅ **다중 클라이언트 지원**: 여러 사용자 동시 접속 가능
- ✅ **실시간 브로드캐스트**: 모든 클라이언트에게 즉시 전송

#### 2. 주요 코인 시세
- 비트코인 (BTC)
- 이더리움 (ETH)
- 리플 (XRP)
- 에이다 (ADA)
- 솔라나 (SOL)
- 도지코인 (DOGE)
- 아발란체 (AVAX)
- 폴리곤 (MATIC)
- 폴카닷 (DOT)
- 시바이누 (SHIB)

#### 3. 제공 정보
- ✅ 현재가
- ✅ 전일 대비 변동률 및 변동가
- ✅ 시가/고가/저가
- ✅ 24시간 거래대금 및 거래량
- ✅ 실시간 업데이트 시간

### API 엔드포인트

```bash
# 전체 코인 시세 조회
GET /api/crypto/tickers

# 특정 코인 시세 조회
GET /api/crypto/tickers/{market}
# 예: GET /api/crypto/tickers/KRW-BTC
```

### 웹 UI

```
http://localhost:8080/crypto
```

**주요 기능:**
- 실시간 시세 업데이트
- 거래대금 순 정렬
- 상승/하락 색상 구분
- 반응형 디자인 (모바일 지원)
- 자동 재연결

### 웹소켓 연결

```javascript
const ws = new WebSocket('ws://localhost:8080/ws/crypto');

ws.onmessage = (event) => {
    const ticker = JSON.parse(event.data);
    console.log(ticker);
    // {
    //   market: "KRW-BTC",
    //   koreanName: "비트코인",
    //   tradePrice: 50000000,
    //   changeRate: 2.5,
    //   ...
    // }
};
```

### 테스트

```powershell
# Windows (PowerShell)
.\test-crypto-api.ps1
```

이 스크립트는 다음을 테스트합니다:
- 전체 코인 시세 조회
- 비트코인 상세 정보
- 이더리움 상세 정보

### 아키텍처

```
[업비트 웹소켓 API]
        ↓
[UpbitWebSocketService]
   - 업비트 연결 관리
   - 시세 데이터 캐싱
   - 클라이언트 세션 관리
        ↓
[CryptoWebSocketHandler]
   - 클라이언트 연결 처리
   - 실시간 브로드캐스트
        ↓
[웹 클라이언트]
   - 실시간 UI 업데이트
   - 자동 재연결
```

### DDD 패턴 적용

```
crypto/
├── domain/
│   └── CoinTicker.java          # 코인 시세 도메인 모델
├── application/
│   └── UpbitWebSocketService.java  # 업비트 웹소켓 서비스
├── infrastructure/
│   ├── WebSocketConfig.java        # 웹소켓 설정
│   └── CryptoWebSocketHandler.java # 웹소켓 핸들러
└── presentation/
    ├── CryptoController.java       # REST API
    └── CryptoWebController.java    # 웹 페이지
```

### 기술 스택
- **WebSocket**: Spring WebSocket
- **외부 API**: 업비트 웹소켓 API
- **실시간 통신**: WebSocket (양방향)
- **데이터 캐싱**: ConcurrentHashMap
- **JSON 처리**: Gson

### 핵심 포인트

1. **실시간 데이터 처리**
   - 업비트 웹소켓으로부터 실시간 시세 수신
   - 모든 클라이언트에게 즉시 브로드캐스트

2. **안정적인 연결 관리**
   - 자동 재연결 메커니즘
   - 연결 상태 모니터링
   - 에러 핸들링

3. **효율적인 데이터 관리**
   - 시세 데이터 캐싱
   - 신규 연결 시 캐시 데이터 전송
   - 메모리 효율적 관리

4. **확장 가능한 구조**
   - 새로운 코인 추가 용이
   - 다중 클라이언트 지원
   - DDD 패턴 적용

5. **사용자 경험**
   - 실시간 업데이트
   - 직관적인 UI
   - 반응형 디자인

## 🎯 온톨로지 기반 인사 채용 시스템

PayFlow는 **AI 없이 규칙 기반 추론으로 구현한 지능형 채용 매칭 시스템**을 제공합니다.

### 주요 특징

#### 1. 온톨로지 기반 지식 표현
- ✅ **기술 온톨로지**: 기술 간 유사도 관계 정의 (Java ≈ Kotlin, Spring ≈ Spring Boot)
- ✅ **개념 계층**: Skill, Candidate, JobPosting, Requirement 등 도메인 개념
- ✅ **관계 정의**: requires, possesses, matches, similarTo 등

#### 2. 규칙 기반 매칭 엔진
```
매칭 알고리즘:
- 필수 기술 매칭 (40%)
- 우대 기술 매칭 (20%)
- 경력 매칭 (25%)
- 학력 매칭 (15%)

→ 최종 매칭 스코어 (0-100점)
```

#### 3. 지능형 추천 시스템
- ✅ 공고별 Top N 지원자 추천
- ✅ 지원자별 적합 공고 추천
- ✅ 유사 지원자 찾기 (Jaccard 유사도)

#### 4. 자동 매칭 스코어
- ✅ 지원 시 자동으로 매칭 스코어 계산
- ✅ 기술 유사도, 숙련도, 경력 종합 평가
- ✅ 상세 매칭 분석 제공

### 도메인 모델

```
recruitment/
├── domain/
│   ├── Skill.java              # 기술 (온톨로지 개념)
│   ├── Candidate.java          # 지원자
│   ├── CandidateSkill.java     # 지원자 보유 기술
│   ├── WorkExperience.java     # 경력
│   ├── JobPosting.java         # 채용 공고
│   ├── JobRequirement.java     # 요구사항
│   └── JobApplication.java     # 지원
├── application/
│   ├── SkillMatchingEngine.java        # 기술 매칭 엔진
│   ├── CandidateMatchingService.java   # 지원자 매칭 서비스
│   ├── RecommendationEngine.java       # 추천 엔진
│   ├── RecruitmentService.java         # 채용 공고 관리
│   ├── CandidateService.java           # 지원자 관리
│   └── ApplicationService.java         # 지원 관리
└── presentation/
    ├── RecruitmentController.java
    ├── CandidateController.java
    ├── ApplicationController.java
    ├── SkillController.java
    └── RecommendationController.java
```

### API 엔드포인트

#### 기술 관리
```bash
# 기술 생성
POST /api/recruitment/skills

# 유사 기술 관계 추가 (온톨로지)
POST /api/recruitment/skills/{skillId}/similar/{similarSkillId}

# 기술 조회
GET /api/recruitment/skills
GET /api/recruitment/skills/{id}
GET /api/recruitment/skills/category/{category}
GET /api/recruitment/skills/search?keyword=Java

# 유사 기술 조회
GET /api/recruitment/skills/{skillId}/similar
```

#### 채용 공고
```bash
# 공고 생성
POST /api/recruitment/jobs

# 요구사항 추가
POST /api/recruitment/jobs/{jobPostingId}/requirements

# 공고 공개/마감
POST /api/recruitment/jobs/{jobPostingId}/publish
POST /api/recruitment/jobs/{jobPostingId}/close

# 공고 조회
GET /api/recruitment/jobs
GET /api/recruitment/jobs/active
GET /api/recruitment/jobs/{id}
GET /api/recruitment/jobs/department/{departmentId}
```

#### 지원자 관리
```bash
# 지원자 생성
POST /api/recruitment/candidates

# 기술 추가
POST /api/recruitment/candidates/{candidateId}/skills

# 경력 추가
POST /api/recruitment/candidates/{candidateId}/experiences

# 지원자 조회
GET /api/recruitment/candidates
GET /api/recruitment/candidates/{id}
GET /api/recruitment/candidates/skill/{skillId}
```

#### 지원 관리
```bash
# 지원 (자동 매칭 스코어 계산)
POST /api/recruitment/applications

# 지원 상태 변경
PUT /api/recruitment/applications/{applicationId}/status

# 매칭 스코어 재계산
POST /api/recruitment/applications/{applicationId}/recalculate-score

# 지원 조회
GET /api/recruitment/applications/{id}
GET /api/recruitment/applications/job/{jobPostingId}
GET /api/recruitment/applications/candidate/{candidateId}

# 상세 매칭 분석
GET /api/recruitment/applications/{id}/matching-detail
```

#### 추천 시스템
```bash
# 공고에 적합한 지원자 추천
GET /api/recruitment/recommendations/job/{jobPostingId}/candidates?topN=10

# 지원자에게 적합한 공고 추천
GET /api/recruitment/recommendations/candidate/{candidateId}/jobs?topN=10

# 유사 지원자 찾기
GET /api/recruitment/recommendations/candidate/{candidateId}/similar?topN=5
```

### 웹 UI

```
http://localhost:8080/recruitment/dashboard
```

**주요 화면:**
- **대시보드** (`/recruitment/dashboard`): 통계, 최신 공고, 최근 지원자, 최고 매칭 지원
- **채용 공고 목록** (`/recruitment/jobs`): 필터링, 검색, 지원자 수 표시
- **채용 공고 상세** (`/recruitment/jobs/{id}`): 요구사항, 매칭 지원자, 추천 지원자
- **지원자 목록** (`/recruitment/candidates`): 학력/경력 필터링, 보유 기술 표시
- **지원자 상세** (`/recruitment/candidates/{id}`): 프로필, 기술, 경력, 추천 공고
- **지원 현황** (`/recruitment/applications`): 매칭 스코어 순, 상태별 필터링

### 테스트

```bash
# Windows (PowerShell)
.\test-recruitment-api.sh

# 또는 Git Bash
bash test-recruitment-api.sh
```

이 스크립트는 다음을 테스트합니다:
- 기술 온톨로지 조회
- 유사 기술 관계 확인
- 채용 공고 생성 및 조회
- 지원자 생성 및 기술/경력 추가
- 지원 및 자동 매칭 스코어 계산
- 추천 시스템 (공고별 지원자, 지원자별 공고)
- 유사 지원자 찾기
- 상세 매칭 분석

### 온톨로지 추론 예시

#### 기술 매칭
```
요구사항: Java (ADVANCED, 3년)
지원자 기술: Kotlin (EXPERT, 5년)

→ 유사도: 0.8 (온톨로지 관계)
→ 숙련도: 1.0 (EXPERT >= ADVANCED)
→ 경력: 1.0 (5년 >= 3년)
→ 최종 점수: 0.92
```

#### 종합 매칭
```
공고: 백엔드 개발자
- 필수: Java, Spring Boot, MySQL
- 우대: AWS, MSA

지원자: 김개발
- Java (EXPERT, 5년)
- Spring Boot (EXPERT, 4년)
- MySQL (ADVANCED, 5년)
- AWS (INTERMEDIATE, 3년)
- MSA (ADVANCED, 3년)

→ 필수 기술: 95점
→ 우대 기술: 90점
→ 경력: 100점 (5년 >= 3년)
→ 학력: 100점 (학사)
→ 최종 매칭 스코어: 96.5점
```

### 초기 데이터

시스템 시작 시 자동으로 생성되는 데이터:
- **기술 18개**: Java, Kotlin, Python, JavaScript, Spring, Django, React, MySQL, PostgreSQL, MongoDB, Redis, AWS, Docker, Kubernetes, MSA, DDD, Agile 등
- **온톨로지 관계**: Java↔Kotlin, Spring↔Spring Boot, Python↔Django, MySQL↔PostgreSQL, Docker↔Kubernetes
- **지원자 3명**: 백엔드 개발자, 프론트엔드 개발자, 풀스택 개발자
- **채용 공고 2개**: 백엔드 개발자, 프론트엔드 개발자
- **지원 4건**: 자동 매칭 스코어 계산됨

### 기술 스택

- **온톨로지 구현**: JPA Entity 관계 (ManyToMany, OneToMany)
- **추론 엔진**: 규칙 기반 알고리즘 (Java)
- **매칭 알고리즘**: 가중치 기반 점수 계산
- **추천 시스템**: Jaccard 유사도, 매칭 스코어 기반 정렬

### 핵심 포인트

1. **온톨로지 기반 지식 표현**
   - 기술 간 유사도 관계를 그래프로 표현
   - 도메인 개념과 관계를 명확히 정의

2. **규칙 기반 추론**
   - AI/ML 없이 규칙만으로 지능형 매칭
   - 투명하고 설명 가능한 알고리즘

3. **자동 매칭 스코어**
   - 지원 시 자동으로 적합도 계산
   - 다차원 평가 (기술, 경력, 학력)

4. **추천 시스템**
   - 양방향 추천 (공고→지원자, 지원자→공고)
   - 유사 지원자 찾기

5. **DDD 패턴**
   - 도메인 주도 설계
   - 명확한 레이어 분리

6. **확장 가능성**
   - 새로운 기술 추가 용이
   - 매칭 알고리즘 조정 가능
   - 온톨로지 관계 확장 가능

### 실무 적용 가능성

이 시스템은 실제 채용 플랫폼에서 다음과 같이 활용 가능:
- 지원자 자동 스크리닝
- 적합 인재 추천
- 채용 담당자 업무 효율화
- 지원자 경험 개선 (적합 공고 추천)
- 채용 데이터 분석

## 🍽️ 식자재 발주 명세서 자동처리

**식자재 발주**

### 주요 특징

#### 1. 매장 발주 시스템
- ✅ **발주 작성**: 품목별 수량, 단가, 단위 입력
- ✅ **유통사 선택**: 신선식자재, 프리미엄푸드 등
- ✅ **발주 내역 관리**: 상태별 필터링, 상세 조회
- ✅ **실시간 통계**: 이번 달 발주, 대기 중인 발주, 발주 금액

#### 2. 유통사 확인 시스템
- ✅ **발주 확인**: 대기 중인 발주 목록
- ✅ **단가 조정**: 품목별 단가 수정 기능
- ✅ **발주 승인/거절**: 사유 입력 및 처리
- ✅ **실시간 통계**: 대기 중, 오늘 확인, 월별 금액

#### 3. 명세서 자동 파싱
- ✅ **CSV 업로드**: 명세서 파일 업로드
- ✅ **자동 파싱**: 품목명, 수량, 단가 자동 추출
- ✅ **Kafka 이벤트**: 업로드 시 자동 파싱 트리거
- ✅ **파싱 결과 조회**: 품목별 내역 확인

#### 4. 정산 및 미수금 관리
- ✅ **자동 정산 생성**: 발주 확인 시 자동 생성
- ✅ **정산 완료 처리**: 지불 금액 입력
- ✅ **미수금 자동 계산**: 정산금액 - 지불금액
- ✅ **매장/유통사별 조회**: 보기 전환 기능

### 웹 UI

**매장 화면:**
```
발주 관리: http://localhost:8080/ingredient/store
정산 내역: http://localhost:8080/ingredient/store/settlements
```

**유통사 화면:**
```
발주 확인: http://localhost:8080/ingredient/distributor
정산 관리: http://localhost:8080/ingredient/distributor/settlements
```

**통합 정산 대시보드:**
```
http://localhost:8080/ingredient/settlement
```

### API 엔드포인트

#### 매장 발주
```bash
# 발주 생성
POST /api/ingredient-orders
{
  "storeId": "STORE_001",
  "distributorId": "DIST_001",
  "items": [
    {
      "itemName": "양파",
      "quantity": 10,
      "unitPrice": 5000,
      "unit": "kg"
    }
  ]
}

# 발주 조회
GET /api/ingredient-orders/{orderId}
GET /api/ingredient-orders/store/{storeId}
```

#### 유통사 확인
```bash
# 대기 중인 발주
GET /api/distributor/orders/pending?distributorId=DIST_001

# 발주 확인
POST /api/distributor/orders/{orderId}/confirm

# 발주 거절
POST /api/distributor/orders/{orderId}/reject
{
  "reason": "재고 부족"
}

# 품목 단가 수정
PUT /api/distributor/orders/{orderId}/items/{itemId}/price
{
  "newPrice": 5500
}
```

#### 명세서 관리
```bash
# 명세서 업로드
POST /api/invoices/upload
Content-Type: multipart/form-data
orderId: INGR_ORDER_xxx
file: @sample-invoice.csv

# 명세서 조회
GET /api/invoices/{invoiceId}
GET /api/invoices/order/{orderId}
```

#### 정산 관리
```bash
# 매장별 정산 내역
GET /api/settlements/store/{storeId}

# 유통사별 정산 내역
GET /api/settlements/distributor/{distributorId}

# 정산 완료
POST /api/settlements/{settlementId}/complete
{
  "paidAmount": 100000
}

# 총 미수금 조회
GET /api/settlements/store/{storeId}/outstanding
```

### E2E 테스트

```bash
./test-ingredient-order-flow.sh
```

이 스크립트는 전체 플로우를 자동으로 테스트합니다:
1. 매장 발주 생성
2. 유통사 발주 확인
3. 명세서 업로드 및 파싱
4. 정산 생성 및 완료
5. 미수금 확인

### Kafka 이벤트 플로우

```
매장 발주 생성 → IngredientOrderCreated
    ↓
유통사 확인 → IngredientOrderConfirmed
    ↓
정산 자동 생성 → SettlementCreated
    ↓
명세서 업로드 → InvoiceUploaded
    ↓
자동 파싱 → InvoiceParsed
    ↓
정산 완료 → SettlementCompleted
```

### 도메인 모델

```
ingredientorder/
├── domain/
│   ├── IngredientOrder.java        # 발주
│   ├── IngredientOrderItem.java    # 발주 품목
│   ├── IngredientOrderStatus.java  # 발주 상태
│   └── event/
│       ├── IngredientOrderCreatedEvent.java
│       └── IngredientOrderConfirmedEvent.java
├── application/
│   └── IngredientOrderService.java
└── presentation/
    ├── IngredientOrderController.java
    └── IngredientWebController.java

distributor/
├── domain/
│   └── Distributor.java            # 유통사
└── application/
    └── DistributorOrderService.java

invoice/
├── domain/
│   ├── Invoice.java                # 명세서
│   ├── InvoiceItem.java            # 명세서 품목
│   └── InvoiceStatus.java
└── application/
    ├── InvoiceService.java
    └── InvoiceParsingService.java  # CSV 파싱

settlement/
├── domain/
│   ├── IngredientSettlement.java   # 정산
│   └── SettlementStatus.java
└── application/
    └── IngredientSettlementService.java

store/
└── domain/
    └── Store.java                  # 매장
```

### 초기 데이터

시스템 시작 시 자동으로 생성:
- **매장**: STORE_001 (맛있는 식당), STORE_002 (행복한 카페)
- **유통사**: DIST_001 (신선식자재), DIST_002 (프리미엄푸드)

## 🤖 단가 자동 학습 & 급등 경고 시스템 (신규 추가!)

**AI 없이 통계 기반으로 구현한 지능형 단가 학습 및 급등 경고 시스템**

### 주요 특징

#### 1. 단가 자동 학습
- ✅ **이력 자동 수집**: 발주 생성 시 품목별 단가 자동 저장
- ✅ **통계 분석**: 평균, 최저가, 최고가, 변동성 계산
- ✅ **추천 단가**: 최근 7일 70% + 30일 평균 30% 가중치
- ✅ **유통사별 분석**: 유통사별 평균 단가 비교

#### 2. 급등 감지 알고리즘
- ✅ **실시간 모니터링**: 발주 생성 시 자동 체크
- ✅ **3단계 경고**:
  - 그저그런 급등 (20-50%)
  - 크레이지 급등 (50-100%)
  - 지옥 급등 (100% 이상)
- ✅ **Kafka 이벤트**: 급등 감지 시 실시간 알림
- ✅ **경고 관리**: 확인/해결 처리

#### 3. 단가 통계 대시보드
- ✅ **품목별 통계**: 평균, 최저, 최고, 추천 단가
- ✅ **변동성 분석**: LOW/MEDIUM/HIGH 3단계
- ✅ **단가 추이 차트**: Chart.js 기반 시각화
- ✅ **이력 조회**: 품목별 단가 이력 테이블

### 웹 UI

```
http://localhost:8080/ingredient/price-learning
```

**주요 화면:**
- **급등 경고 탭**: 활성 경고, 최근 경고 내역
- **단가 통계 탭**: 품목별 통계, 추이 차트
- **단가 이력 탭**: 품목별 이력 테이블

### API 엔드포인트

#### 단가 통계
```bash
# 품목별 단가 통계 (30일 기준)
GET /api/price-learning/items/{itemName}/statistics?days=30

# 응답 예시
{
  "itemName": "양파",
  "averagePrice": 5100,
  "minPrice": 5000,
  "maxPrice": 7500,
  "recentPrice": 7500,
  "recommendedPrice": 5800,
  "dataPoints": 3,
  "volatility": 22.5,
  "volatilityLevel": "MEDIUM"
}

# 품목별 단가 이력
GET /api/price-learning/items/{itemName}/history?days=30

# 추천 단가 조회
GET /api/price-learning/items/{itemName}/recommended-price

# 모든 품목 목록
GET /api/price-learning/items
```

#### 급등 경고
```bash
# 활성 경고 목록
GET /api/price-learning/alerts/active

# 최근 경고 목록 (최대 10개)
GET /api/price-learning/alerts/recent

# 경고 상세 조회
GET /api/price-learning/alerts/{alertId}

# 경고 확인 처리
POST /api/price-learning/alerts/{alertId}/acknowledge

# 경고 해결 처리
POST /api/price-learning/alerts/{alertId}/resolve
```

### 테스트

```bash
./test-price-learning.sh
```

이 스크립트는 다음을 테스트합니다:
1. 정상 단가로 발주 생성 (기준 데이터)
2. 비슷한 단가로 발주 생성 (학습 데이터)
3. 급등 단가로 발주 생성 (경고 발생!)
4. 활성 경고 조회
5. 품목별 통계 조회
6. 추천 단가 조회
7. 단가 이력 조회
8. 경고 확인/해결 처리

### 급등 감지 알고리즘

```java
// 최근 30일 평균 단가 계산
Double avgPrice = calculateAveragePrice(itemName, 30);

// 급등률 계산
double surgePercentage = ((currentPrice - avgPrice) / avgPrice) * 100;

// 급등 기준
if (surgePercentage >= 100%) → EXTREME_SURGE (그저그런)
if (surgePercentage >= 50%)  → HIGH_SURGE (크레이지 급등)
if (surgePercentage >= 20%)  → MODERATE_SURGE (지옥 급등)
```

### 추천 단가 계산

```java
// 최근 7일 평균
Double recentAvg = calculateAveragePrice(itemName, 7);

// 최근 30일 평균
Double monthlyAvg = calculateAveragePrice(itemName, 30);

// 가중 평균 (최근 70%, 월평균 30%)
Long recommendedPrice = Math.round(recentAvg * 0.7 + monthlyAvg * 0.3);
```

### Kafka 이벤트 플로우

```
발주 생성 → IngredientOrderCreated
    ↓
PriceHistoryCollector (Kafka Listener)
    ↓
단가 이력 저장 + 급등 체크
    ↓
급등 감지 시 → PriceSurgeAlertEvent 발행
    ↓
이벤트 로그 시스템에 기록
```

### 도메인 모델

```
pricelearning/
├── domain/
│   ├── ItemPriceHistory.java       # 단가 이력
│   ├── PriceAlert.java             # 급등 경고
│   ├── PriceAlertType.java         # 경고 유형
│   ├── PriceAlertStatus.java       # 경고 상태
│   ├── PriceStatistics.java        # 단가 통계
│   └── event/
│       └── PriceSurgeAlertEvent.java
├── application/
│   ├── PriceLearningService.java   # 단가 학습 서비스
│   └── PriceAlertService.java      # 급등 경고 서비스
├── infrastructure/
│   └── PriceHistoryCollector.java  # Kafka 리스너
└── presentation/
    ├── PriceLearningController.java
    ├── PriceLearningWebController.java
    └── dto/
        ├── PriceStatisticsResponse.java
        ├── PriceAlertResponse.java
        └── PriceHistoryResponse.java
```

**확장 가능성**
   - ML 모델 추가 가능 (시계열 예측)
   - 외부 시세 API 연동
   - 자동 발주 시스템 연계
   - 계절별 패턴 학습

### 실무로 쓴다면

유통 플랫폼에서 다음과 같이 활용 가능:
- 매장의 과다 청구 방지
- 유통사의 적정 단가 제시
- 시장 가격 모니터링
- 원가 관리 및 절감
- 구매 담당자 의사결정 지원


## 🎯 Dynamic Par Level - 재고 예측 발주

**규칙 기반 통계로 구현한 지능형 재고 예측 및 자동 발주 시스템**

### 주요 특징

#### 1. Par Level 관리
- ✅ **최소/최대 재고 설정**: 품목별 적정 재고 수준 관리
- ✅ **안전 재고**: 리드타임 동안 소진 방지
- ✅ **리드타임 설정**: 발주 후 입고까지 소요 시간
- ✅ **자동 발주 활성화**: 품목별 자동 발주 ON/OFF

#### 2. 소비 패턴 분석
- ✅ **일별 소비 기록**: 품목별 소비량 자동 수집
- ✅ **이동 평균**: 최근 7일/30일 평균 소비량 계산
- ✅ **요일별 패턴**: 주말 vs 평일 소비 차이 반영
- ✅ **변동성 분석**: 표준편차 기반 변동성 계산

#### 3. 발주 예측 엔진
- ✅ **재고 예측**: 리드타임 동안 예상 소비량 계산
- ✅ **재발주 시점 감지**: 현재 재고 < 최소 재고
- ✅ **발주량 계산**: 최대 재고 - 현재 재고
- ✅ **예측 근거 제공**: 투명한 의사결정 지원

#### 4. 자동 발주 실행
- ✅ **단가 연동**: 단가 학습 시스템과 연계하여 최적 가격 적용
- ✅ **일괄 발주**: 모든 예측 품목 한 번에 발주
- ✅ **발주 이력 추적**: 예측 → 실제 발주 연결
- ✅ **스케줄러**: 매일 오전 6시 자동 체크

### 예측 알고리즘 (규칙 기반)

#### 1. 이동 평균 (Moving Average)
```java
// 최근 7일 평균 소비량
Double avg7Days = calculateAverageDailyConsumption(storeId, itemName, 7);

// 최근 30일 평균 소비량
Double avg30Days = calculateAverageDailyConsumption(storeId, itemName, 30);

// 가중 평균: 최근 7일 70%, 30일 30%
double weightedAvg = (avg7Days * 0.7) + (avg30Days * 0.3);
```

#### 2. 안전 재고 계산
```java
// 표준편차 (변동성)
Double stdDev = calculateStandardDeviation(storeId, itemName, 30);

// 안전 재고 = 리드타임 소비량 + (표준편차 * 1.65)
// 1.65 = 95% 신뢰수준
int safetyStock = (int) Math.ceil((avgDaily * leadTimeDays) + (stdDev * 1.65));
```

#### 3. Par Level 자동 계산
```java
// 최소 재고 = 리드타임 소비량 + 안전 재고
int minLevel = (int) Math.ceil(avgDaily * leadTimeDays) + safetyStock;

// 최대 재고 = 최소 재고 + (평균 일일 소비량 * 7일)
int maxLevel = minLevel + (int) Math.ceil(avgDaily * 7);
```

#### 4. 발주 예측
```java
// 리드타임 동안 예상 소비량
int predictedConsumption = predictConsumption(storeId, itemName, leadTimeDays);

// 예상 잔여 재고
int projectedStock = currentStock - predictedConsumption;

// 재발주 필요 여부
if (projectedStock <= minLevel) {
    int orderQuantity = maxLevel - currentStock;
    // 자동 발주 생성
}
```

### 웹 UI

```
http://localhost:8080/parlevel/dashboard
```

**주요 화면:**
- **대시보드**: 통계, 대기 중인 예측, 자동 발주 실행
- **Par Level 설정**: 품목별 최소/최대 재고 관리
- **발주 예측**: 예측 목록, 자동 발주, 건너뛰기
- **소비 패턴**: 품목별 소비 이력 및 통계

### API 엔드포인트

#### Par Level 관리
```bash
# Par Level 생성
POST /api/parlevel/settings
{
  "storeId": "STORE_001",
  "itemName": "양파",
  "unit": "kg",
  "minLevel": 50,
  "maxLevel": 150,
  "safetyStock": 30,
  "leadTimeDays": 2,
  "autoOrderEnabled": true
}

# Par Level 수정
PUT /api/parlevel/settings/{id}
{
  "minLevel": 60,
  "maxLevel": 180,
  "safetyStock": 35,
  "leadTimeDays": 2
}

# 자동 발주 활성화/비활성화
POST /api/parlevel/settings/{id}/enable-auto-order
POST /api/parlevel/settings/{id}/disable-auto-order

# Par Level 조회
GET /api/parlevel/settings/{storeId}
GET /api/parlevel/settings/{storeId}/{itemName}

# Par Level 자동 계산
POST /api/parlevel/settings/auto-calculate?storeId=STORE_001&itemName=양파&unit=kg&leadTimeDays=2
```

#### 발주 예측
```bash
# 예측 생성
POST /api/parlevel/predictions/{storeId}/generate

# 대기 중인 예측 조회
GET /api/parlevel/predictions/{storeId}/pending

# 전체 예측 조회
GET /api/parlevel/predictions/{storeId}

# 예측 건너뛰기
POST /api/parlevel/predictions/{predictionId}/skip
```

#### 자동 발주
```bash
# 단일 자동 발주
POST /api/parlevel/auto-order/{predictionId}?distributorId=DIST_001

# 전체 자동 발주
POST /api/parlevel/auto-order/{storeId}/execute-all?distributorId=DIST_001
```

#### 소비 패턴 분석
```bash
# 소비 이력 조회
GET /api/parlevel/consumption/{storeId}/{itemName}?days=30

# 소비 통계
GET /api/parlevel/consumption/{storeId}/{itemName}/statistics?days=30

# 응답 예시
{
  "averageDailyConsumption": 25.5,
  "standardDeviation": 5.2,
  "predicted7DaysConsumption": 180,
  "analysisWindow": "30 days"
}
```

### 테스트

```bash
./test-parlevel-api.sh
```

이 스크립트는 다음을 테스트합니다:
1. Par Level 설정 조회
2. 소비 패턴 통계 (양파)
3. 소비 이력 조회 (최근 7일)
4. 발주 예측 생성
5. 대기 중인 예측 조회
6. 자동 발주 실행
7. Par Level 자동 계산 (새 품목)
8. 전체 예측 조회

### 시스템 통합

#### 1. 식자재 발주 시스템 연동
```
[Par Level 체크] → 재발주 필요 감지
    ↓
[발주 예측 생성] → 권장 발주량 계산
    ↓
[자동 발주 실행] → IngredientOrderService 호출
    ↓
[발주 생성] → 기존 발주 시스템 활용
```

#### 2. 단가 학습 시스템 연동
```
[자동 발주] → 최적 단가 조회
    ↓
[PriceLearningService] → 추천 단가 반환
    ↓
[발주 생성] → 최적 가격으로 발주
```

#### 3. 소비 데이터 수집
```
[발주 완료] → IngredientOrderCompleted 이벤트
    ↓
[ConsumptionDataCollector] → Kafka Listener
    ↓
[소비 패턴 기록] → 품목별 소비량 저장
    ↓
[예측 정확도 향상] → 데이터 축적
```

### 초기 데이터

시스템 시작 시 자동으로 생성:
- **Par Level 설정**: 양파, 당근, 감자, 대파, 마늘 (5개 품목)
- **소비 패턴**: 최근 30일 소비 데이터 (주말 20% 증가 반영)

### 스케줄러

```java
@Scheduled(cron = "0 0 6 * * *")  // 매일 오전 6시
public void checkParLevelsAndGeneratePredictions() {
    // 모든 매장의 Par Level 체크
    // 재발주 필요 품목 예측 생성
}
```

### 실무 적용 가능성

유통/외식 플랫폼에서 다음과 같이 활용 가능:
- **재고 최적화**: 과다/부족 재고 방지
- **운영 효율화**: 수동 발주 업무 자동화
- **원가 절감**: 적정 재고 유지로 폐기 손실 감소
- **현금 흐름 개선**: 과다 재고 방지로 자금 효율화
- **데이터 기반 의사결정**: 소비 패턴 분석 기반 구매 전략

### 차별화 포인트

기존 시스템과의 통합:
- ✅ **단가 학습 + 재고 예측**: 최적 가격 + 최적 수량
- ✅ **급등 경고 + 자동 발주**: 가격 급등 시 발주 보류
- ✅ **이벤트 기반**: 실시간 소비 데이터 수집
- ✅ **DDD 패턴**: 명확한 도메인 로직 분리
- ✅ **규칙 기반 추론**: AI 없이도 지능형 예측



## 📸 명세표 OCR + LLM 파싱 시스템

PayFlow는 **PaddleOCR + Ollama LLM을 이용한 명세표 자동 파싱 시스템**을 제공합니다.

### 주요 기능

#### 1. 이미지 OCR 추출
- ✅ **PaddleOCR**: 한글 지원 고정확도 OCR
- ✅ **자동 텍스트 추출**: 이미지에서 텍스트 자동 인식
- ✅ **신뢰도 필터링**: 70% 이상 신뢰도 텍스트만 추출
- ✅ **다양한 형식 지원**: JPG, PNG, GIF 등

#### 2. LLM 기반 파싱
- ✅ **Ollama + Qwen2.5:7b**: 무료 오픈소스 LLM
- ✅ **자동 JSON 생성**: 정규화된 JSON 구조 자동 생성
- ✅ **필드 추출**: 상품명, 카테고리, 가격, 수량, 명세 항목
- ✅ **구조화된 데이터**: 데이터베이스 저장 가능한 형식

#### 3. 데이터 저장 및 조회
- ✅ **DB 저장**: H2/MySQL에 명세표 정보 저장
- ✅ **이미지 보관**: 원본 이미지 저장
- ✅ **텍스트 보관**: 추출된 텍스트 저장
- ✅ **JSON 보관**: 파싱된 JSON 저장
- ✅ **상태 추적**: 업로드 → 추출 → 파싱 → 완료

#### 4. 웹 UI
- ✅ **업로드 페이지**: 드래그 앤 드롭 지원
- ✅ **목록 페이지**: 모든 명세표 조회
- ✅ **상세 페이지**: 원본 이미지, 추출 텍스트, 파싱 결과 표시

### 도메인 모델

```
specification/
├── domain/
│   ├── Specification.java           # 명세표 엔티티
│   ├── SpecificationItem.java       # 명세 항목
│   ├── ProcessingStatus.java        # 처리 상태
│   └── SpecificationRepository.java
├── application/
│   ├── OCRService.java              # PaddleOCR 호출
│   ├── LLMParsingService.java       # Ollama LLM 호출
│   └── SpecificationService.java    # 비즈니스 로직
└── presentation/
    ├── SpecificationController.java # REST API
    ├── SpecificationWebController.java # 웹 페이지
    └── dto/
        ├── SpecificationResponse.java
        └── ParsedSpecificationDto.java
```

### API 엔드포인트

#### 명세표 관리
```bash
# 명세표 업로드 및 처리
POST /api/specifications/upload
Content-Type: multipart/form-data
- file: 이미지 파일

# 명세표 상세 조회
GET /api/specifications/{id}

# 모든 명세표 조회
GET /api/specifications

# 상태별 조회
GET /api/specifications/status/{status}
# 상태: UPLOADED, TEXT_EXTRACTED, PARSING, PARSED, ERROR

# 상품명으로 검색
GET /api/specifications/search?productName=검색어
```

### 웹 UI

```
http://localhost:8080/specification              # 목록
http://localhost:8080/specification/upload       # 업로드
http://localhost:8080/specification/{id}         # 상세
```

### 처리 흐름

```
1. 사용자가 명세표 이미지 업로드
   ↓
2. Spring Boot가 이미지 저장
   ↓
3. PaddleOCR 호출 → 텍스트 추출
   ↓
4. 추출된 텍스트 → Ollama LLM 전송
   ↓
5. LLM이 JSON 생성
   {
     "productName": "상품명",
     "category": "카테고리",
     "price": 50000,
     "quantity": 10,
     "specifications": [
       {"name": "크기", "value": "100x100mm", "unit": "mm"}
     ]
   }
   ↓
6. JSON 파싱 → DB 저장
   ↓
7. Kafka 이벤트 발행
   ↓
8. 프론트에서 결과 표시
```

### 처리 상태

- `UPLOADED`: 이미지 업로드됨
- `TEXT_EXTRACTED`: OCR 텍스트 추출 완료
- `PARSING`: LLM 파싱 중
- `PARSED`: 파싱 완료
- `ERROR`: 처리 중 오류 발생

### Docker 설정

```yaml
# docker-compose.yml
services:
  ollama:
    image: ollama/ollama:latest
    ports:
      - "11434:11434"
    volumes:
      - ollama_data:/root/.ollama

  paddleocr:
    image: paddlepaddle/paddleocr:latest-en
    ports:
      - "8501:8501"
```

### 설치 및 실행

#### 1단계: Docker 서비스 시작

```bash
# Ollama 실행
docker run -d --name ollama -p 11434:11434 ollama/ollama:latest

# Qwen2.5:7b 모델 다운로드 (약 4.7GB)
docker exec ollama ollama pull qwen2.5:7b

# 또는 더 가벼운 Phi-3 (약 2.3GB)
docker exec ollama ollama pull phi:3

# PaddleOCR 실행
docker run -d --name paddleocr -p 8501:8501 paddlepaddle/paddleocr:latest-en

# 또는 전체 docker-compose 실행
docker-compose up -d
```

#### 2단계: 애플리케이션 빌드 및 실행

```bash
./gradlew clean build
./gradlew bootRun
```

#### 3단계: 웹 접속

```
http://localhost:8080/specification/upload
```

### 테스트

```bash
./test-specification-api.sh
```

이 스크립트는 다음을 테스트합니다:
- 명세표 목록 조회
- 테스트 이미지 생성
- 명세표 업로드 및 처리
- 명세표 상세 조회
- 상태별 조회
- 상품명 검색

### 응답 예시

#### 업로드 성공

```json
{
  "id": 1,
  "imagePath": "uploads/specifications/uuid_filename.png",
  "extractedText": "상품명: 테스트 명세표\n카테고리: 전자제품\n...",
  "parsedJson": "{\"productName\": \"테스트 명세표\", ...}",
  "items": [
    {
      "id": 1,
      "itemName": "크기",
      "itemValue": "100x100mm",
      "unit": "mm",
      "sequence": 0
    }
  ],
  "productName": "테스트 명세표",
  "category": "전자제품",
  "price": 50000,
  "quantity": 10,
  "status": "PARSED",
  "createdAt": "2025-11-24T12:00:00",
  "updatedAt": "2025-11-24T12:00:05"
}
```

### 기술 스택

- **OCR**: PaddleOCR (Python)
- **LLM**: Ollama + Qwen2.5:7b
- **백엔드**: Spring Boot 3.5.7
- **데이터베이스**: H2 (또는 MySQL)
- **메시징**: Kafka
- **프론트엔드**: Thymeleaf + Vanilla JS

### 성능 지표

| 항목 | 시간 |
|------|------|
| OCR 추출 | 1-3초 |
| LLM 파싱 | 2-5초 |
| 총 처리 시간 | 3-8초 |
| 메모리 사용 | ~2GB (Qwen2.5:7b) |

### 비용

- **PaddleOCR**: 무료 (오픈소스)
- **Ollama**: 무료 (오픈소스)
- **Qwen2.5:7b**: 무료 (오픈소스)
- **Docker**: 무료
- **Spring Boot**: 무료

**총 비용: 0원** ✅

### 확장 가능성

#### 1. 다양한 LLM 모델 지원
```bash
# Phi-3 (더 빠름, 약 2.3GB)
ollama pull phi:3

# Mistral (더 강력함, 약 4.1GB)
ollama pull mistral:7b

# Llama2 (다목적, 약 3.8GB)
ollama pull llama2:7b
```

#### 2. 배치 처리
```java
@Scheduled(cron = "0 0 * * * *")  // 매시간
public void processPendingSpecifications() {
    // 대기 중인 명세표 일괄 처리
}
```

#### 3. 이미지 전처리
```java
// 회전, 크롭, 명도 조정 등
BufferedImage preprocessed = preprocessImage(originalImage);
```

#### 4. 정확도 개선
```java
// 여러 LLM 모델 앙상블
String result1 = callOllama(text, "qwen2.5:7b");
String result2 = callOllama(text, "phi:3");
// 결과 병합 및 검증
```

### 실무 활용

- **전자상거래**: 상품 명세표 자동 파싱
- **유통**: 발주 명세표 자동 처리
- **제조**: 부품 명세서 자동 입력
- **물류**: 송장 정보 자동 추출
- **금융**: 영수증/청구서 자동 처리


## 🍽️ 메뉴 원가 자동 계산 + 마진 시뮬레이터

**식자재 단가 학습 데이터를 활용한 메뉴 원가 자동 계산 및 마진 시뮬레이션 시스템**

### 주요 특징

#### 1. 메뉴 레시피 관리
- ✅ **메뉴 등록**: 메뉴명, 설명, 카테고리, 판매가 설정
- ✅ **레시피 구성**: 메뉴별 재료 및 사용량 관리
- ✅ **재료 단위**: kg, g, L, ml, 개, 모 등 다양한 단위 지원
- ✅ **메뉴 활성화**: 판매 중/중지 상태 관리

#### 2. 자동 원가 계산
- ✅ **실시간 단가 연동**: 단가 학습 시스템에서 최신 추천 단가 조회
- ✅ **재료별 원가**: 사용량 × 단가로 재료별 원가 자동 계산
- ✅ **총 원가**: 모든 재료 원가의 합계
- ✅ **원가 비율**: 각 재료가 전체 원가에서 차지하는 비율

#### 3. 마진 분석
```
마진율 = (판매가 - 원가) / 판매가 × 100
마크업율 = (판매가 - 원가) / 원가 × 100
매출 총이익 = 판매가 - 원가
```

#### 4. 마진 시뮬레이터
- ✅ **가격 기반 시뮬레이션**: 목표 판매가 입력 → 예상 마진율 계산
- ✅ **마진율 기반 시뮬레이션**: 목표 마진율 입력 → 필요 판매가 계산
- ✅ **실시간 비교**: 현재 대비 가격/마진율 차이 표시
- ✅ **의사결정 지원**: 가격 정책 수립에 필요한 데이터 제공

### DDD 패턴 적용

```
menu/
├── domain/
│   ├── Menu.java                # 메뉴 집합 루트
│   ├── RecipeIngredient.java    # 레시피 재료 엔티티
│   ├── MenuCostAnalysis.java    # 원가 분석 VO
│   ├── MarginSimulation.java    # 마진 시뮬레이션 VO
│   └── MenuRepository.java
├── application/
│   ├── MenuService.java         # 메뉴 관리
│   ├── MenuCostCalculator.java  # 원가 계산 엔진
│   └── MarginSimulator.java     # 마진 시뮬레이터
├── presentation/
│   ├── MenuController.java      # REST API
│   ├── MenuWebController.java   # 웹 UI
│   └── dto/
└── infrastructure/
    └── MenuDataInitializer.java # 초기 데이터
```

### API 엔드포인트

#### 메뉴 관리
```bash
# 메뉴 생성
POST /api/menu
{
  "name": "김치찌개",
  "description": "묵은지와 돼지고기로 끓인 얼큰한 김치찌개",
  "category": "한식",
  "storeId": "STORE-001",
  "sellingPrice": 8000,
  "recipeIngredients": [
    {
      "ingredientName": "김치",
      "quantity": 0.3,
      "unit": "kg",
      "notes": "묵은지 사용"
    },
    {
      "ingredientName": "돼지고기",
      "quantity": 0.15,
      "unit": "kg",
      "notes": "삼겹살"
    }
  ]
}

# 메뉴 조회
GET /api/menu/{menuId}

# 전체 메뉴 조회
GET /api/menu

# 매장별 메뉴 조회
GET /api/menu/store/{storeId}

# 카테고리별 메뉴 조회
GET /api/menu/category/{category}

# 메뉴 수정
PUT /api/menu/{menuId}

# 메뉴 삭제
DELETE /api/menu/{menuId}

# 메뉴 활성화/비활성화
POST /api/menu/{menuId}/activate
POST /api/menu/{menuId}/deactivate
```

#### 원가 계산
```bash
# 메뉴 원가 계산
GET /api/menu/{menuId}/cost

# 응답 예시
{
  "menuId": 1,
  "menuName": "김치찌개",
  "totalCost": 2650,
  "sellingPrice": 8000,
  "grossProfit": 5350,
  "marginRate": 66.88,
  "markupRate": 201.89,
  "ingredientCosts": [
    {
      "ingredientName": "김치",
      "quantity": 0.3,
      "unit": "kg",
      "unitPrice": 3000,
      "cost": 900,
      "costRatio": 33.96
    },
    {
      "ingredientName": "돼지고기",
      "quantity": 0.15,
      "unit": "kg",
      "unitPrice": 8000,
      "cost": 1200,
      "costRatio": 45.28
    }
  ]
}

# 매장 전체 메뉴 원가 분석
GET /api/menu/store/{storeId}/costs
```

#### 마진 시뮬레이션
```bash
# 가격 기반 시뮬레이션
GET /api/menu/{menuId}/simulate/price?targetPrice=10000

# 응답 예시
{
  "menuId": 1,
  "menuName": "김치찌개",
  "totalCost": 2650,
  "currentSellingPrice": 8000,
  "currentMarginRate": 66.88,
  "targetSellingPrice": 10000,
  "targetMarginRate": 73.50,
  "priceDifference": 2000,
  "marginDifference": 6.62
}

# 마진율 기반 시뮬레이션
GET /api/menu/{menuId}/simulate/margin?targetMargin=35

# 응답 예시
{
  "menuId": 1,
  "menuName": "김치찌개",
  "totalCost": 2650,
  "currentSellingPrice": 8000,
  "currentMarginRate": 66.88,
  "targetSellingPrice": 4077,
  "targetMarginRate": 35.00,
  "priceDifference": -3923,
  "marginDifference": -31.88
}
```

### 웹 UI

```
http://localhost:8080/menu
```

**주요 페이지:**
- **메뉴 목록** (`/menu`): 전체 메뉴 조회, 필터링, 검색
- **메뉴 상세** (`/menu/{id}`): 원가 분석, 재료별 원가 비율
- **메뉴 추가** (`/menu/create`): 새 메뉴 및 레시피 등록
- **마진 시뮬레이터** (`/menu/{id}/simulator`): 가격/마진율 시뮬레이션
- **매장 원가 분석** (`/menu/store/{storeId}/costs`): 매장 전체 메뉴 원가 현황

### 테스트

```bash
./test-menu-api.sh
```

이 스크립트는 다음을 테스트합니다:
- 전체 메뉴 조회
- 매장별 메뉴 조회
- 메뉴 상세 조회
- 메뉴 원가 계산
- 매장 전체 원가 분석
- 가격 기반 마진 시뮬레이션
- 마진율 기반 시뮬레이션
- 새 메뉴 생성

### 초기 데이터

시스템 시작 시 자동으로 생성되는 샘플 메뉴:

1. **김치찌개** (8,000원)
   - 김치 0.3kg, 돼지고기 0.15kg, 두부 0.5모, 대파 0.05kg, 고춧가루 0.01kg
   - 예상 원가: 약 2,650원
   - 예상 마진율: 약 67%

2. **된장찌개** (7,000원)
   - 된장 0.05kg, 두부 0.5모, 감자 0.1kg, 애호박 0.1kg, 양파 0.05kg, 대파 0.03kg
   - 예상 원가: 약 1,510원
   - 예상 마진율: 약 78%

3. **불고기** (15,000원)
   - 소고기 0.2kg, 양파 0.1kg, 대파 0.05kg, 간장 0.03L, 설탕 0.02kg, 참기름 0.01L, 마늘 0.01kg
   - 예상 원가: 약 5,470원
   - 예상 마진율: 약 64%

4. **비빔밥** (9,000원)
   - 쌀 0.15kg, 소고기 0.05kg, 시금치 0.05kg, 콩나물 0.05kg, 고사리 0.03kg, 애호박 0.05kg, 당근 0.03kg, 계란 1개, 고추장 0.03kg, 참기름 0.01L
   - 예상 원가: 약 2,380원
   - 예상 마진율: 약 74%

### 실무 활용 시나리오

#### 1. 신메뉴 개발
```
1. 레시피 구성 → 2. 원가 계산 → 3. 목표 마진율 설정 → 4. 판매가 결정
```

#### 2. 가격 정책 수립
```
- 현재 마진율 분석
- 경쟁사 가격 비교
- 목표 마진율 시뮬레이션
- 최적 판매가 도출
```

#### 3. 원가 절감
```
- 재료별 원가 비율 분석
- 고비용 재료 파악
- 대체 재료 검토
- 원가 절감 효과 시뮬레이션
```

#### 4. 메뉴 포트폴리오 관리
```
- 매장 전체 메뉴 원가 분석
- 저마진 메뉴 파악
- 메뉴 구성 최적화
- 수익성 개선
```

### 확장 가능성

#### 1. 고급 원가 분석
```java
// 인건비, 임대료 등 간접비 포함
public MenuCostAnalysis calculateFullCost(Long menuId) {
    // 재료비 + 인건비 + 간접비
}
```

#### 2. 시즌별 단가 변동
```java
// 계절별 재료 단가 변동 반영
public Long getSeasonalPrice(String ingredient, LocalDate date) {
    // 시즌별 가중치 적용
}
```

#### 3. 메뉴 추천
```java
// 고마진 메뉴 추천
public List<Menu> recommendHighMarginMenus(String storeId) {
    // 마진율 기준 정렬
}
```

#### 4. 원가 알림
```java
// 원가 급등 알림
@Scheduled(cron = "0 0 9 * * *")
public void checkCostIncrease() {
    // 전일 대비 원가 상승률 체크
}
```


## 🚚 카탈로그 주문 배송 관리 시스템

**가게사장님과 유통업자 간 상품 주문 후 배송 프로세스를 관리하는 시스템**

### 주요 특징

#### 1. 완전한 주문 생명주기 관리
```
PENDING (주문대기)
   ↓ 결제 완료
CONFIRMED (주문확정)
   ↓ 유통업자가 배송 정보 생성
PREPARING (상품준비중)
   ↓ 유통업자가 배송 시작
SHIPPED (배송중)
   ↓ 유통업자가 배송 완료
DELIVERED (배송완료)
```

#### 2. 유통업자 배송 관리
- ✅ **배송 정보 생성**: 주문 확정 후 배송 정보 자동 생성
- ✅ **상품 준비**: 배송 전 상품 준비 단계 관리
- ✅ **배송 시작**: 송장번호, 배송사, 예상 도착일 입력
- ✅ **배송 완료**: 배송 완료 처리
- ✅ **실시간 통계**: 주문확정, 상품준비중, 배송중, 배송완료 건수

#### 3. 가게사장님 배송 조회
- ✅ **배송 상태 조회**: 실시간 배송 상태 확인
- ✅ **송장 정보**: 송장번호, 배송사, 예상 도착일 확인
- ✅ **배송 이력**: 모든 주문의 배송 내역 조회
- ✅ **배송지 정보**: 배송지 주소, 연락처, 요청사항 확인

#### 4. 배송 정보 관리
- ✅ **송장번호**: 배송 추적을 위한 송장번호 관리
- ✅ **배송사**: CJ대한통운, 로젠택배, 한진택배, 우체국택배 등
- ✅ **배송사 연락처**: 배송 문의를 위한 연락처
- ✅ **예상 배송일**: 고객에게 제공할 예상 도착일
- ✅ **배송 메모**: 유통업자용 배송 관련 메모

### DDD 패턴 적용

```
catalog/
├── domain/
│   ├── DistributorOrder.java      # 주문 집합 루트
│   ├── OrderStatus.java            # 주문 상태
│   ├── DeliveryInfo.java           # 배송 정보 엔티티
│   ├── DeliveryStatus.java         # 배송 상태
│   └── DeliveryInfoRepository.java
├── application/
│   ├── CatalogOrderService.java    # 주문 관리
│   └── DeliveryService.java        # 배송 관리
└── presentation/
    ├── CatalogOrderController.java
    ├── DeliveryController.java
    ├── DeliveryWebController.java
    └── dto/
        ├── DeliveryResponse.java
        └── StartShippingRequest.java
```

### API 엔드포인트

#### 배송 정보 생성 (유통업자)
```bash
# 배송 정보 생성 (주문 확정 후)
POST /api/deliveries/order/{orderId}

# 응답 예시
{
  "id": 1,
  "orderId": 123,
  "orderNumber": "ORD-20251128-143022-456",
  "storeId": "store001",
  "distributorId": "dist001",
  "status": "PREPARING",
  "statusDescription": "상품준비중",
  "deliveryAddress": "서울시 강남구 테헤란로 123",
  "deliveryPhone": "010-1234-5678",
  "deliveryRequest": "문 앞에 놓아주세요",
  "totalAmount": 50000,
  "preparedAt": "2025-11-28T14:30:22"
}
```

#### 배송 시작 (유통업자)
```bash
# 배송 시작
POST /api/deliveries/order/{orderId}/ship
{
  "trackingNumber": "1234567890",
  "courierCompany": "CJ대한통운",
  "courierPhone": "1588-1255",
  "estimatedDeliveryDate": "2025-11-30T18:00:00",
  "deliveryNotes": "신선식품 주의"
}

# 응답 예시
{
  "id": 1,
  "orderId": 123,
  "orderNumber": "ORD-20251128-143022-456",
  "trackingNumber": "1234567890",
  "courierCompany": "CJ대한통운",
  "courierPhone": "1588-1255",
  "status": "SHIPPED",
  "statusDescription": "배송중",
  "shippedAt": "2025-11-28T15:00:00",
  "estimatedDeliveryDate": "2025-11-30T18:00:00",
  "deliveryNotes": "신선식품 주의"
}
```

#### 배송 완료 (유통업자)
```bash
# 배송 완료
POST /api/deliveries/order/{orderId}/complete

# 응답 예시
{
  "id": 1,
  "orderId": 123,
  "orderNumber": "ORD-20251128-143022-456",
  "status": "DELIVERED",
  "statusDescription": "배송완료",
  "deliveredAt": "2025-11-30T17:30:00"
}
```

#### 배송 조회
```bash
# 배송 정보 조회 (주문 ID로)
GET /api/deliveries/order/{orderId}

# 유통업자별 배송 목록
GET /api/deliveries/distributor

# 매장별 배송 목록
GET /api/deliveries/store

# 상태별 배송 목록
GET /api/deliveries/status/{status}
# 상태: PREPARING, SHIPPED, DELIVERED
```

### 웹 UI

#### 유통업자 배송 관리
```
http://localhost:8080/deliveries/distributor
```

**주요 기능:**
- 📊 **실시간 통계**: 주문확정, 상품준비중, 배송중, 배송완료 건수
- 📋 **주문 목록**: 상태별 필터링 (전체, 주문확정, 상품준비중, 배송중, 배송완료)
- 🚀 **배송 정보 생성**: 주문 확정된 주문에 대해 배송 정보 생성
- 📦 **배송 시작**: 송장번호, 배송사, 예상 도착일 입력 후 배송 시작
- ✅ **배송 완료**: 배송 완료 처리

#### 가게사장님 배송 조회
```
http://localhost:8080/deliveries/store
```

**주요 기능:**
- 📦 **배송 목록**: 모든 주문의 배송 상태 조회
- 🔍 **배송 상세**: 송장번호, 배송사, 예상 도착일 확인
- 📍 **배송지 정보**: 배송지 주소, 연락처, 요청사항 확인
- 🚚 **배송 추적**: 배송 상태별 색상 구분 (준비중/배송중/완료)

### 테스트

```bash
./test-delivery-api.sh
```

이 스크립트는 다음을 테스트합니다:
1. 매장 사용자 로그인
2. 유통업자 로그인
3. 주문 생성 (매장)
4. 주문 확정 (매장)
5. 배송 정보 생성 (유통업자)
6. 배송 시작 (유통업자)
7. 배송 정보 조회 (매장)
8. 유통업자 배송 목록 조회
9. 매장 배송 목록 조회
10. 배송 완료 (유통업자)

### 배송 상태 흐름

```
[주문 생성] → PENDING
    ↓
[결제 완료] → CONFIRMED
    ↓
[배송 정보 생성] → PREPARING (상품준비중)
    ↓
[배송 시작] → SHIPPED (배송중)
    - 송장번호 입력
    - 배송사 선택
    - 예상 도착일 설정
    ↓
[배송 완료] → DELIVERED (배송완료)
```

### 데이터베이스 스키마

#### delivery_info 테이블
```sql
CREATE TABLE delivery_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    tracking_number VARCHAR(50),
    courier_company VARCHAR(50),
    courier_phone VARCHAR(20),
    status VARCHAR(20) NOT NULL,
    prepared_at TIMESTAMP,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    estimated_delivery_date TIMESTAMP,
    delivery_address VARCHAR(255),
    delivery_phone VARCHAR(20),
    delivery_request VARCHAR(255),
    delivery_notes VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (order_id) REFERENCES distributor_orders(id)
);
```

### 권한 체계

- **STORE_OWNER (가게사장님)**:
  - 주문 생성
  - 주문 확정 (결제 후)
  - 배송 정보 조회
  - 배송 목록 조회

- **DISTRIBUTOR (유통업자)**:
  - 주문 목록 조회
  - 배송 정보 생성
  - 배송 시작
  - 배송 완료
  - 배송 목록 조회

### 실무 활용 시나리오

#### 1. 일반적인 배송 프로세스
```
1. 가게사장님이 상품 주문
2. 결제 완료 후 주문 확정
3. 유통업자가 주문 확인
4. 유통업자가 배송 정보 생성 (상품 준비 시작)
5. 상품 준비 완료 후 배송 시작 (송장번호 입력)
6. 배송 완료 처리
7. 가게사장님이 배송 완료 확인
```

#### 2. 배송 추적
```
- 가게사장님: 송장번호로 배송사 홈페이지에서 실시간 추적
- 유통업자: 배송 상태별 필터링으로 진행 중인 배송 관리
```

#### 3. 배송 지연 처리
```
- 유통업자가 배송 메모에 지연 사유 기록
- 예상 도착일 업데이트
- 가게사장님에게 알림 (향후 확장)
```

### 향후 확장 가능 기능

#### 1. 배송 추적 API 연동
```java
// CJ대한통운, 로젠택배 등 배송사 API 연동
public DeliveryTrackingInfo getTrackingInfo(String trackingNumber) {
    // 배송사 API 호출
    // 실시간 배송 위치 조회
}
```

#### 2. 알림 시스템
```java
// 배송 상태 변경 시 알림
@EventListener
public void onDeliveryStatusChanged(DeliveryStatusChangedEvent event) {
    // 가게사장님에게 SMS/푸시 알림
}
```

#### 3. 배송 통계
```java
// 유통업자별 배송 성과 분석
public DeliveryStatistics getDeliveryStats(String distributorId) {
    // 평균 배송 시간
    // 정시 배송률
    // 배송 완료율
}
```

#### 4. 배송비 계산
```java
// 거리/무게 기반 배송비 자동 계산
public Long calculateDeliveryFee(String address, Double weight) {
    // 배송지 거리 계산
    // 무게별 요금 적용
}
```

### 기술 스택

- **백엔드**: Spring Boot 3.5.7, Spring Security
- **데이터베이스**: H2 (또는 MySQL)
- **인증**: JWT
- **프론트엔드**: Thymeleaf + Vanilla JavaScript
- **스타일**: CSS3 (반응형 디자인)

### 핵심 포인트

1. **완전한 배송 생명주기**
   - 주문 확정부터 배송 완료까지 전 과정 관리
   - 각 단계별 상태 전이 규칙 적용

2. **역할 기반 접근 제어**
   - 유통업자: 배송 관리 권한
   - 가게사장님: 배송 조회 권한

3. **실시간 정보 제공**
   - 송장번호, 배송사, 예상 도착일
   - 배송 상태별 색상 구분

4. **DDD 패턴**
   - 도메인 주도 설계
   - 명확한 레이어 분리
   - 엔티티 간 관계 명확화

5. **확장 가능한 구조**
   - 배송사 API 연동 준비
   - 알림 시스템 연계 가능
   - 배송 통계 분석 확장 가능

### 실무 적용 가능성

이 시스템은 실제 B2B 유통 플랫폼에서 다음과 같이 활용 가능:
- 식자재 유통 플랫폼
- 도매 거래 플랫폼
- B2B 전자상거래
- 공급망 관리 시스템 (SCM)
- 물류 관리 시스템 (WMS)


## ⭐ 리뷰 및 평점 시스템

**배송 완료 후 양방향 리뷰 시스템**

### 주요 특징

#### 1. 양방향 리뷰
- ✅ **가게사장님 → 유통업자**: 배송 품질, 상품 품질, 서비스 품질 평가
- ✅ **유통업자 → 가게사장님**: 결제 신뢰도, 소통 품질, 주문 정확도 평가
- ✅ **배송 완료 후 작성**: 실제 거래 경험 기반 리뷰
- ✅ **중복 방지**: 한 주문당 한 번만 리뷰 작성 가능

#### 2. 세부 평점 시스템
**가게사장님 → 유통업자**:
- 배송 품질 (1-5): 배송 속도, 포장 상태 등
- 상품 품질 (1-5): 신선도, 품질 등
- 서비스 품질 (1-5): 응대, 문제 해결 등

**유통업자 → 가게사장님**:
- 결제 신뢰도 (1-5): 결제 이행, 신속성 등
- 소통 품질 (1-5): 응답 속도, 명확성 등
- 주문 정확도 (1-5): 주문 내용 정확성 등

#### 3. 리뷰 통계
- ✅ **평균 평점**: 전체 평균 평점 계산
- ✅ **평점별 분포**: 5점, 4점, 3점, 2점, 1점 개수
- ✅ **세부 평점 평균**: 각 항목별 평균 점수
- ✅ **총 리뷰 개수**: 받은 리뷰 총 개수

#### 4. 리뷰 관리
- ✅ **내가 받은 리뷰**: 다른 사용자가 나에게 작성한 리뷰
- ✅ **내가 작성한 리뷰**: 내가 다른 사용자에게 작성한 리뷰
- ✅ **주문별 리뷰 조회**: 특정 주문의 리뷰 확인
- ✅ **리뷰 통계 조회**: 사용자별 평점 통계

### DDD 패턴 적용

```
catalog/
├── domain/
│   ├── Review.java              # 리뷰 엔티티
│   ├── ReviewType.java          # 리뷰 타입 (양방향)
│   └── ReviewRepository.java
├── application/
│   └── ReviewService.java       # 리뷰 비즈니스 로직
└── presentation/
    ├── ReviewController.java    # REST API
    └── dto/
        ├── CreateReviewRequest.java
        ├── ReviewResponse.java
        └── ReviewStatisticsResponse.java
```

### API 엔드포인트

#### 리뷰 작성
```bash
# 가게사장님 → 유통업자 리뷰
POST /api/reviews/store
Authorization: Bearer {token}
{
  "orderId": 1,
  "rating": 5,
  "comment": "배송이 빠르고 상품 품질이 좋습니다!",
  "deliveryQuality": 5,
  "productQuality": 5,
  "serviceQuality": 4
}

# 유통업자 → 가게사장님 리뷰
POST /api/reviews/distributor
Authorization: Bearer {token}
{
  "orderId": 1,
  "rating": 5,
  "comment": "결제가 빠르고 소통이 원활합니다!",
  "paymentReliability": 5,
  "communicationQuality": 5,
  "orderAccuracy": 4
}
```

#### 리뷰 조회
```bash
# 내가 받은 리뷰
GET /api/reviews/received
Authorization: Bearer {token}

# 내가 작성한 리뷰
GET /api/reviews/written
Authorization: Bearer {token}

# 주문별 리뷰
GET /api/reviews/order/{orderId}?type=STORE_TO_DISTRIBUTOR
Authorization: Bearer {token}

# 리뷰 통계
GET /api/reviews/statistics/{userId}
Authorization: Bearer {token}

# 내 리뷰 통계
GET /api/reviews/statistics
Authorization: Bearer {token}
```

### 응답 예시

#### 리뷰 작성 응답
```json
{
  "id": 1,
  "orderId": 1,
  "orderNumber": "ORD-20251128-143022-456",
  "reviewType": "STORE_TO_DISTRIBUTOR",
  "reviewTypeDescription": "가게사장님 → 유통업자",
  "reviewerId": "store001",
  "reviewerName": "매장-store001",
  "revieweeId": "dist001",
  "revieweeName": "유통업체-dist001",
  "rating": 5,
  "comment": "배송이 빠르고 상품 품질이 좋습니다!",
  "deliveryQuality": 5,
  "productQuality": 5,
  "serviceQuality": 4,
  "createdAt": "2025-11-28T18:00:00"
}
```

#### 리뷰 통계 응답
```json
{
  "userId": "dist001",
  "userName": "유통업체-dist001",
  "averageRating": 4.8,
  "totalReviews": 25,
  "rating5Count": 20,
  "rating4Count": 4,
  "rating3Count": 1,
  "rating2Count": 0,
  "rating1Count": 0,
  "avgDeliveryQuality": 4.9,
  "avgProductQuality": 4.7,
  "avgServiceQuality": 4.8,
  "avgPaymentReliability": null,
  "avgCommunicationQuality": null,
  "avgOrderAccuracy": null
}
```

### 데이터베이스 스키마

#### reviews 테이블
```sql
CREATE TABLE reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    review_type VARCHAR(50) NOT NULL,
    reviewer_id VARCHAR(50) NOT NULL,
    reviewer_name VARCHAR(100) NOT NULL,
    reviewee_id VARCHAR(50) NOT NULL,
    reviewee_name VARCHAR(100) NOT NULL,
    rating INT NOT NULL,
    comment VARCHAR(1000),
    delivery_quality INT,
    product_quality INT,
    service_quality INT,
    payment_reliability INT,
    communication_quality INT,
    order_accuracy INT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES distributor_orders(id)
);
```

### 비즈니스 규칙

1. **작성 조건**:
   - 주문 상태가 `DELIVERED` (배송완료)여야 함
   - 본인의 주문에만 리뷰 작성 가능
   - 한 주문당 한 번만 리뷰 작성 가능

2. **평점 범위**:
   - 전체 평점: 1-5 (필수)
   - 세부 평점: 1-5 (선택)

3. **리뷰 타입**:
   - `STORE_TO_DISTRIBUTOR`: 가게사장님이 유통업자 평가
   - `DISTRIBUTOR_TO_STORE`: 유통업자가 가게사장님 평가

### 실무 활용 시나리오

#### 1. 신뢰도 평가
```
- 유통업자 선택 시 평균 평점 참고
- 높은 평점의 유통업자 우선 선택
- 세부 평점으로 강점 파악
```

#### 2. 서비스 개선
```
- 낮은 평점 항목 파악
- 고객 피드백 분석
- 서비스 품질 향상
```

#### 3. 인센티브 제공
```
- 높은 평점 유통업자에게 혜택
- 우수 매장 인증 배지
- 추천 시스템 연동
```

### 향후 확장 가능 기능

#### 1. 리뷰 수정/삭제
```java
// 작성 후 24시간 내 수정 가능
public ReviewResponse updateReview(Long reviewId, UpdateReviewRequest request) {
    // 수정 로직
}
```

#### 2. 리뷰 신고
```java
// 부적절한 리뷰 신고
public void reportReview(Long reviewId, String reason) {
    // 신고 로직
}
```

#### 3. 리뷰 답글
```java
// 리뷰에 대한 답글 작성
public ReviewReplyResponse createReply(Long reviewId, String content) {
    // 답글 로직
}
```

#### 4. 베스트 리뷰
```java
// 도움이 된 리뷰 선정
public List<Review> getBestReviews(String userId) {
    // 베스트 리뷰 조회
}
```

### 기술 스택

- **백엔드**: Spring Boot 3.5.7, Spring Data JPA
- **데이터베이스**: H2 (또는 MySQL)
- **인증**: JWT
- **검증**: Bean Validation

### 핵심 포인트

1. **양방향 리뷰**
   - 가게사장님과 유통업자 모두 평가 가능
   - 공정한 거래 환경 조성

2. **세부 평점**
   - 단순 별점이 아닌 항목별 평가
   - 구체적인 피드백 제공

3. **통계 분석**
   - 평균 평점 자동 계산
   - 평점별 분포 시각화
   - 세부 항목별 평균

4. **신뢰도 구축**
   - 실제 거래 기반 리뷰
   - 중복 방지로 신뢰성 확보
   - 투명한 평가 시스템

5. **DDD 패턴**
   - 명확한 도메인 로직
   - 레이어 분리
   - 확장 가능한 구조

### 실무 적용 가능성

이 시스템은 실제 B2B 플랫폼에서 다음과 같이 활용 가능:
- 유통업자 신뢰도 평가
- 우수 거래처 선정
- 서비스 품질 관리
- 고객 만족도 측정
- 분쟁 해결 근거 자료
