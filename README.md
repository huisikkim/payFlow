# PayFlow - MSA + EDA + DDD 기반 결제 시스템

토스페이먼츠를 연동한 실전형 결제 시스템 MVP입니다.

## 🆕 Pick Swap - 중고 거래 플랫폼 (신규 추가!)

PayFlow에 **Pick Swap 중고 거래 플랫폼**의 상품 관리 및 **경매 기능**이 추가되었습니다!

### 주요 기능
- ✅ **Home Feed**: 최신 상품 리스트 (Flutter 앱 메인 화면용)
- ✅ **카테고리별 조회**: 전자기기, 패션, 가전, 가구 등 11개 카테고리
- ✅ **검색 기능**: 키워드, 가격 범위, 지역별 검색
- ✅ **상품 관리**: 등록, 수정, 삭제, 판매 완료 처리
- ✅ **인기 상품**: 좋아요 많은 순 정렬
- ✅ **판매자 상품**: 판매자별 상품 목록
- ✅ **좋아요 기능**: 상품 좋아요/취소
- 🔨 **경매 시스템**: 실시간 경매 및 입찰 기능 (NEW!)

### Flutter 앱 연동
상세한 API 문서는 [PRODUCT_API_GUIDE.md](PRODUCT_API_GUIDE.md)를 참고하세요.

**주요 API:**
```bash
# Home Feed (최신 상품 20개)
GET /api/products/feed?page=0&size=20

# 카테고리별 조회
GET /api/products/category/ELECTRONICS?page=0&size=20

# 키워드 검색
GET /api/products/search?keyword=아이폰&page=0&size=20

# 상품 상세
GET /api/products/{productId}

# 상품 등록
POST /api/products
```

### 웹 UI 접속
```
http://localhost:8080/pickswap
```

**주요 페이지:**
- **홈 (상품 피드)**: `/pickswap` - 최신 상품 목록, 카테고리 필터, 검색
- **상품 상세**: `/pickswap/products/{id}` - 상품 정보, 이미지, 판매자 정보
- **상품 등록**: `/pickswap/sell` - 새 상품 등록 폼
- **내 상품 관리**: `/pickswap/my-products` - 내가 등록한 상품 목록
- **경매 목록**: `/pickswap/auctions` - 진행 중/예정/종료 경매 목록
- **경매 상세**: `/pickswap/auctions/{id}` - 실시간 입찰, 자동 입찰 설정
- **경매 등록**: `/pickswap/auctions/create` - 새 경매 등록
- **내 경매**: `/pickswap/auctions/my` - 판매 중/낙찰 중인 경매

### API 테스트
```powershell
# 상품 API 테스트
.\test-product-api.ps1

# 경매 API 테스트
.\test-auction-api.ps1
```

이 스크립트는 Home Feed, 카테고리 조회, 검색, 상품 등록/수정/삭제, 경매 생성/입찰/자동입찰 등 모든 기능을 테스트합니다.

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

