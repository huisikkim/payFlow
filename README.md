# PayFlow - MSA + EDA + DDD 기반 결제 시스템

토스페이먼츠를 연동한 실전형 결제 시스템 MVP입니다.

## 아키텍처

### MSA (Microservices Architecture)
- **Order Service**: 주문 생성 및 관리
- **Payment Service**: 결제 처리 및 토스페이먼츠 연동
- **Chatbot Service**: AI 챗봇 고객 지원 (규칙 기반)

### EDA (Event-Driven Architecture)
- Kafka를 통한 서비스 간 비동기 통신
- `OrderCreated`, `StageStarted`, `PaymentDue`, `PayoutReady` 등 이벤트 발행/구독
- 느슨한 결합으로 서비스 독립성 확보

### DDD (Domain-Driven Design)
```
domain/          # 도메인 모델 (Entity, VO, Repository)
application/     # 애플리케이션 서비스
presentation/    # 컨트롤러, DTO
infrastructure/  # 외부 시스템 연동, 스케줄러
```

## 기술 스택

- **Backend**: Java 17, Spring Boot 3.5.7
- **ORM**: Spring Data JPA
- **Database**: H2 (인메모리)
- **Messaging**: Apache Kafka
- **Payment**: Toss Payments API
- **Frontend**: Thymeleaf, Vanilla JS

## 사전 준비

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