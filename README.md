# PayFlow - MSA + EDA + DDD 기반 결제 시스템

토스페이먼츠를 연동한 실전형 결제 시스템 MVP입니다.

## 아키텍처

### MSA (Microservices Architecture)
- **Order Service**: 주문 생성 및 관리
- **Payment Service**: 결제 처리 및 토스페이먼츠 연동

### EDA (Event-Driven Architecture)
- Kafka를 통한 서비스 간 비동기 통신
- `OrderCreated` 이벤트 발행/구독
- 느슨한 결합으로 서비스 독립성 확보

### DDD (Domain-Driven Design)
```
domain/          # 도메인 모델 (Entity, VO, Repository)
application/     # 애플리케이션 서비스
presentation/    # 컨트롤러, DTO
infrastructure/  # 외부 시스템 연동
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

### Order Service
- `POST /api/orders` - 주문 생성
- `GET /api/orders/{orderId}` - 주문 조회

### Payment Service
- `POST /api/payments/confirm` - 결제 승인
- `GET /api/payments/{orderId}` - 결제 조회

### Web UI
- `GET /` - 결제 페이지
- `GET /success` - 결제 성공 페이지
- `GET /fail` - 결제 실패 페이지

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