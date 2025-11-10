# PayFlow - MSA + EDA + DDD 기반 결제 시스템

토스페이먼츠를 연동한 실전형 결제 시스템 MVP입니다.

## 아키텍처

### MSA (Microservices Architecture)
- **Order Service**: 주문 생성 및 관리
- **Payment Service**: 결제 처리 및 토스페이먼츠 연동
- **Stage Service**: 스테이지(계) 생성 및 관리
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

### 1. Kafka 설치 및 실행

**macOS (Homebrew):**
```bash
brew install kafka
brew services start zookeeper
brew services start kafka
```

**Docker:**
```bash
docker run -d --name zookeeper -p 2181:2181 zookeeper:3.7
docker run -d --name kafka -p 9092:9092 \
  -e KAFKA_ZOOKEEPER_CONNECT=host.docker.internal:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  confluentinc/cp-kafka:latest
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

### Stage Service (스테이지/계 - 인증 필요)
- `POST /api/stages` - 스테이지 생성
- `GET /api/stages/{id}` - 스테이지 조회
- `GET /api/stages` - 스테이지 목록 조회 (상태별 필터링)
- `GET /api/stages/my` - 내가 참여한 스테이지 목록
- `POST /api/stages/{id}/join` - 스테이지 참여
- `POST /api/stages/{id}/start` - 스테이지 시작
- `GET /api/stages/{id}/participants` - 참여자 목록 조회
- `GET /api/stages/{id}/payments` - 스테이지별 결제 내역
- `GET /api/stages/payments/my` - 내 결제 내역
- `GET /api/stages/{id}/payouts` - 스테이지별 약정금 내역
- `GET /api/stages/payouts/my` - 내 약정금 내역
- `POST /api/stages/payouts/{id}/complete` - 약정금 지급 완료 처리
- `POST /api/stages/{id}/settlement` - 정산 생성
- `GET /api/stages/{id}/settlement` - 정산 조회
- `GET /api/stages/{id}/settlement/participants` - 참여자별 정산 내역
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
- `GET /stages` - 스테이지 목록 페이지
- `GET /stages/{id}/settlement` - 정산 대시보드 페이지
- `GET /chatbot` - AI 챗봇 페이지

## 데이터베이스

H2 Console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:payflowdb`
- Username: `sa`
- Password: (비워두기)

### Kafka 토픽 확인
```bash
kafka-topics --list --bootstrap-server localhost:9092
kafka-console-consumer --topic OrderCreated --from-beginning --bootstrap-server localhost:9092
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

### 스테이지 기능 테스트
```bash
./test-stage-api.sh
```

이 스크립트는 다음을 테스트합니다:
- 스테이지 생성
- 참여자 모집 (순번 선택)
- 스테이지 시작
- 참여자 목록 조회
- 내 스테이지 목록 조회

## 🤖 AI 챗봇 기능

PayFlow에는 규칙 기반 AI 챗봇이 통합되어 있습니다.

### 주요 특징
- ✅ **비용 없음**: 외부 API 없이 내부 규칙 기반으로 동작
- ✅ **DDD 패턴**: Domain, Application, Presentation 레이어 분리
- ✅ **EDA 적용**: Kafka를 통한 이벤트 발행
- ✅ **대화 이력 관리**: H2 DB에 모든 대화 저장

### 지원하는 기능
- 인사 및 환영 메시지
- 주문 조회 안내
- 결제 내역 확인 안내
- 배송 상태 조회 안내
- 환불/취소 처리 안내
- 스테이지 참여 및 시작 방법 안내
- 정산 내역 확인 안내
- 도움말 제공

### 챗봇 사용하기

**웹 UI:**
```
http://localhost:8080/chatbot
```
또는 메인 페이지 우측 하단의 💬버튼 클릭!

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