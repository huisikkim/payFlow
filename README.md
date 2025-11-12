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

## 🤖 채용 공고 검색 챗봇

PayFlow에는 대화형 채용 공고 검색 챗봇이 통합되어 있습니다.

### 주요 특징
- ✅ **대화형 검색**: 단계별로 조건을 물어보며 맞춤 채용 공고 검색
- ✅ **다양한 검색 조건**: 지역, 업종, 연봉 범위로 필터링
- ✅ **실시간 결과**: 조건에 맞는 채용 공고를 즉시 표시
- ✅ **DDD 패턴**: Domain, Application, Presentation 레이어 분리
- ✅ **EDA 적용**: Kafka를 통한 이벤트 발행
- ✅ **대화 컨텍스트 관리**: 사용자의 검색 진행 상태 추적

### 검색 흐름
1. **채용 검색 시작**: "채용" 또는 "일자리" 입력
2. **지역 선택**: 원하는 근무 지역 입력 (예: 서울, 경기, 부산)
3. **업종 선택**: 관심 업종 입력 (예: IT, 금융, 스타트업)
4. **연봉 입력**: 희망 연봉 범위 입력 (예: 4000만원~6000만원)
5. **결과 확인**: 조건에 맞는 채용 공고 목록 표시

### 지원하는 검색 조건
- **지역**: 서울, 경기, 인천, 부산, 대구, 광주, 대전, 울산, 세종, 강원, 충북, 충남, 전북, 전남, 경북, 경남, 제주
- **업종**: IT, 금융, 제조, 유통, 서비스, 교육, 의료, 건설, 미디어, 게임, 스타트업
- **연봉**: 자유 입력 (예: 3000만원, 4000~6000만원)

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

### 샘플 대화 예시
```
사용자: 안녕하세요
챗봇: 안녕하세요! 채용 공고 검색 챗봇입니다. 💼

사용자: 채용 찾고 싶어요
챗봇: 좋아요! 먼저, 어느 지역에서 일하고 싶으신가요?

사용자: 서울에서 일하고 싶어요
챗봇: '서울' 지역을 선택하셨네요! 👍
      다음으로, 어떤 업종에 관심이 있으신가요?

사용자: IT 업종이요
챗봇: 'IT' 업종을 선택하셨네요! 💼
      마지막으로, 희망 연봉 범위를 알려주세요.

사용자: 4000만원에서 6000만원 사이요
챗봇: 🎉 총 5개의 채용 공고를 찾았습니다!
      [채용 공고 목록 표시]
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