# 2. 시스템 아키텍처 설계 (AWS 기반)

## 2.1 전체 시스템 구조

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Layer                             │
├─────────────────────────────────────────────────────────────────┤
│  Web App (React)  │  Mobile App  │  Admin Dashboard             │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      CDN (CloudFront)                            │
│                    (정적 자산, 이미지)                             │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Application Load Balancer                      │
│                        (HTTPS, SSL/TLS)                          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      API Server Layer                            │
├─────────────────────────────────────────────────────────────────┤
│  EC2 Instance 1  │  EC2 Instance 2  │  (Auto Scaling Group)     │
│  Spring Boot API │  Spring Boot API │                           │
└─────────────────────────────────────────────────────────────────┘
                              │
                ┌─────────────┼─────────────┐
                ▼             ▼             ▼
┌──────────────────┐  ┌──────────────┐  ┌──────────────┐
│   RDS PostgreSQL │  │  Redis Cache │  │   S3 Bucket  │
│   (Multi-AZ)     │  │  (ElastiCache)│  │  (파일 저장)  │
└──────────────────┘  └──────────────┘  └──────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    External Services                             │
├─────────────────────────────────────────────────────────────────┤
│  Email (SES)  │  SMS (SNS)  │  Monitoring (CloudWatch)          │
└─────────────────────────────────────────────────────────────────┘
```

### 데이터 흐름

1. **사용자 요청**: Web/Mobile → CloudFront → ALB → API Server
2. **인증**: JWT 토큰 검증 (Redis 캐시 활용)
3. **비즈니스 로직**: Service Layer 처리
4. **데이터 접근**: Repository → PostgreSQL
5. **파일 처리**: S3 업로드/다운로드
6. **비동기 작업**: SQS → Lambda/Worker (알림 발송)

---

## 2.2 AWS 구성도 (상세)

### VPC 구조

```
┌─────────────────────────────────────────────────────────────────┐
│                         VPC (10.0.0.0/16)                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │              Public Subnet (10.0.1.0/24)                │    │
│  ├─────────────────────────────────────────────────────────┤    │
│  │  - Internet Gateway                                     │    │
│  │  - NAT Gateway                                          │    │
│  │  - Application Load Balancer                            │    │
│  │  - Bastion Host (관리용)                                 │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │             Private Subnet (10.0.2.0/24)                │    │
│  ├─────────────────────────────────────────────────────────┤    │
│  │  - EC2 Instances (API Server)                           │    │
│  │  - Auto Scaling Group                                   │    │
│  │  - ElastiCache (Redis)                                  │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │          Private Subnet - DB (10.0.3.0/24)              │    │
│  ├─────────────────────────────────────────────────────────┤    │
│  │  - RDS PostgreSQL (Primary)                             │    │
│  │  - RDS PostgreSQL (Standby - Multi-AZ)                  │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

### Security Group 구성

| 리소스 | Inbound | Outbound |
|--------|---------|----------|
| ALB | 0.0.0.0/0:443 (HTTPS) | EC2:8080 |
| EC2 (API) | ALB:8080 | RDS:5432, Redis:6379, 0.0.0.0/0:443 |
| RDS | EC2:5432 | - |
| Redis | EC2:6379 | - |
| Bastion | 관리자 IP:22 | VPC 내부 |

---

## 2.3 인증 & API 전략

### 인증 방식: JWT (JSON Web Token)

**선택 이유:**
- ✅ Stateless: 서버 확장 용이
- ✅ 모바일 앱 지원 용이
- ✅ 마이크로서비스 전환 시 유리
- ✅ Redis 캐시로 토큰 블랙리스트 관리 가능

**구현 방식:**

```
1. 로그인 요청
   POST /api/auth/login
   { "email": "user@example.com", "password": "***" }

2. JWT 발급
   {
     "accessToken": "eyJhbGc...",  // 15분 유효
     "refreshToken": "eyJhbGc...", // 7일 유효
     "tokenType": "Bearer"
   }

3. API 요청
   GET /api/applicants
   Header: Authorization: Bearer eyJhbGc...

4. 토큰 갱신
   POST /api/auth/refresh
   { "refreshToken": "eyJhbGc..." }
```

**JWT Payload:**
```json
{
  "sub": "user-id-123",
  "email": "user@example.com",
  "role": "COMPANY_HR",
  "companyId": "company-456",
  "iat": 1700000000,
  "exp": 1700000900
}
```

**보안 전략:**
- Access Token: 짧은 유효기간 (15분)
- Refresh Token: Redis 저장, 로그아웃 시 블랙리스트 등록
- HTTPS 필수
- XSS 방어: HttpOnly Cookie 옵션 (선택)
- CSRF 방어: SameSite Cookie 속성

### API Gateway (선택 사항)

**Phase 1**: ALB 직접 연결 (단순, 빠른 개발)
**Phase 2**: AWS API Gateway 도입 검토
- Rate Limiting (요청 제한)
- API Key 관리
- 요청/응답 변환
- 캐싱

---

## 2.4 아키텍처 방식 선택

### 비교 분석

| 항목 | 모놀리스 | 모듈러 모놀리스 | MSA |
|------|---------|----------------|-----|
| 개발 속도 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ |
| 배포 복잡도 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ |
| 확장성 | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| 팀 규모 적합성 (4-5명) | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐ |
| 운영 복잡도 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ |
| 기술 부채 위험 | ⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |

### 추천: 모듈러 모놀리스 (Modular Monolith)

**선택 이유:**

1. **팀 규모 (4-5명)**
   - MSA는 팀당 1개 서비스 관리가 이상적 → 현재 팀 규모에 과도
   - 모놀리스는 빠르지만 확장 시 리팩토링 부담

2. **개발 기간 (6-8개월)**
   - MSA 인프라 구축에만 2-3개월 소요
   - 모듈러 모놀리스는 빠른 개발 + 향후 MSA 전환 가능

3. **비즈니스 불확실성**
   - 초기에는 요구사항 변경 빈번
   - 모듈 경계가 명확해진 후 MSA 전환 검토

4. **운영 복잡도**
   - 단일 배포 단위 → CI/CD 간단
   - 로그, 모니터링 통합 관리 용이

### 모듈러 모놀리스 구조

```
ainjob-api/
├── applicant-module/          # 지원자 관리
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── domain/
│       ├── Applicant.java
│       ├── Resume.java
│       └── Education.java
│
├── jobposting-module/         # 채용 공고 관리
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── domain/
│       ├── JobPosting.java
│       └── JobPostingSkill.java
│
├── application-module/        # 지원 추적
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── domain/
│       ├── ApplicationTracking.java
│       └── ApplicationStatus.java
│
├── notification-module/       # 알림
│   ├── service/
│   └── domain/
│       └── NotificationTemplate.java
│
├── common-module/             # 공통
│   ├── security/
│   ├── exception/
│   ├── util/
│   └── config/
│
└── api-gateway/               # API 진입점
    └── controller/
```

**모듈 간 의존성 규칙:**
- ✅ `application-module` → `applicant-module`, `jobposting-module` (읽기만)
- ✅ 모든 모듈 → `common-module`
- ❌ `applicant-module` ↔ `jobposting-module` (직접 의존 금지)
- ❌ 순환 참조 금지

**모듈 간 통신:**
- Phase 1: 직접 메서드 호출 (같은 프로세스)
- Phase 2: Domain Event (Spring ApplicationEvent)
- Phase 3: Message Queue (SQS) - MSA 전환 준비

---

## 2.5 배포 전략

### Phase 1: 단순 배포 (0-3개월)

```
GitHub Push → GitHub Actions
  ↓
Build & Test (Gradle)
  ↓
Docker Image Build
  ↓
Push to ECR
  ↓
SSH to EC2 → Docker Pull & Run
  ↓
Health Check
```

**Downtime**: 약 30초 (허용 가능)

### Phase 2: Rolling Deployment (3-6개월)

```
ALB
 ├─ EC2-1 (현재 버전)
 └─ EC2-2 (현재 버전)

배포 시:
1. EC2-1 트래픽 제거 → 새 버전 배포 → Health Check → 트래픽 복구
2. EC2-2 트래픽 제거 → 새 버전 배포 → Health Check → 트래픽 복구
```

**Downtime**: 0초

### Phase 3: Blue-Green Deployment (6개월+)

```
Blue Environment (현재)
 ├─ EC2-1, EC2-2
 └─ ALB Target Group A

Green Environment (새 버전)
 ├─ EC2-3, EC2-4
 └─ ALB Target Group B

배포 시:
1. Green 환경 배포 & 테스트
2. ALB 트래픽 Blue → Green 전환
3. 문제 발생 시 즉시 롤백 (Blue로 전환)
```

---

## 2.6 Event System (Advanced - Phase 2)

### 도입 시점

다음 상황 발생 시 도입 검토:
1. 알림 발송으로 인한 API 응답 지연 (>500ms)
2. 배치 작업 증가 (일일 리포트, 통계)
3. 외부 시스템 연동 (ATS, 채용 플랫폼)

### 아키텍처

```
API Server
  ↓ (이벤트 발행)
SQS Queue
  ↓ (폴링)
Worker (Lambda or EC2)
  ↓
외부 서비스 (Email, SMS, Webhook)
```

### 이벤트 전략

**1. 이벤트 순서 보장**
- SQS FIFO Queue 사용
- Message Group ID: `applicant-{id}` (같은 지원자는 순서 보장)

**2. Retry & Backoff**
```
시도 1: 즉시
시도 2: 1분 후
시도 3: 5분 후
시도 4: 15분 후
시도 5: 1시간 후
→ DLQ (Dead Letter Queue)
```

**3. Dead Letter Queue**
- 5회 재시도 실패 시 DLQ로 이동
- CloudWatch Alarm 발생
- 수동 재처리 또는 로그 분석

**4. At-least-once vs Exactly-once**
- **At-least-once** (기본): 중복 가능, 멱등성 보장 필요
  - 예: 이메일 중복 발송 방지 → DB에 발송 이력 저장
- **Exactly-once** (선택): SQS FIFO + 중복 제거
  - 비용 증가, 처리량 감소

### 이벤트 예시

```java
// 이벤트 발행
@Transactional
public void changeApplicationStatus(Long applicationId, ApplicationStatus newStatus) {
    Application app = applicationRepository.findById(applicationId);
    app.changeStatus(newStatus);
    
    // 이벤트 발행
    eventPublisher.publish(new ApplicationStatusChangedEvent(
        app.getId(),
        app.getApplicantId(),
        app.getOldStatus(),
        newStatus
    ));
}

// 이벤트 처리 (비동기)
@EventListener
@Async
public void handleStatusChanged(ApplicationStatusChangedEvent event) {
    // 알림 발송
    notificationService.sendStatusChangeNotification(event);
}
```

---

## 2.7 확장 전략

### Horizontal Scaling (수평 확장)

**Auto Scaling 정책:**
```
Scale Out (인스턴스 추가):
- CPU > 70% (5분 평균)
- 또는 ALB Request Count > 1000/min

Scale In (인스턴스 제거):
- CPU < 30% (10분 평균)
- 최소 인스턴스: 2개 (가용성)
```

### Vertical Scaling (수직 확장)

**인스턴스 타입 변경:**
```
Phase 1: t3.medium (2 vCPU, 4GB RAM)
Phase 2: t3.large (2 vCPU, 8GB RAM)
Phase 3: c5.xlarge (4 vCPU, 8GB RAM) - CPU 집약적
```

### Database Scaling

**Read Replica:**
```
Primary (Write)
  ↓
Read Replica 1 (Read)
Read Replica 2 (Read)

용도:
- 통계 쿼리
- 리포트 생성
- 검색 (복잡한 필터)
```

**Connection Pooling:**
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```

---

## 2.8 재해 복구 (DR)

### Backup 전략

**RDS 자동 백업:**
- 일일 스냅샷 (보관 기간: 7일)
- Point-in-Time Recovery (5분 단위)

**S3 버전 관리:**
- 파일 삭제/수정 시 이전 버전 보관
- Lifecycle Policy: 90일 후 Glacier 이동

### 장애 복구 시나리오

| 장애 유형 | 복구 방법 | RTO | RPO |
|----------|----------|-----|-----|
| EC2 장애 | Auto Scaling 자동 교체 | 5분 | 0 |
| RDS 장애 | Multi-AZ 자동 Failover | 2분 | 0 |
| AZ 장애 | Multi-AZ 구성 | 2분 | 0 |
| Region 장애 | 수동 복구 (백업에서) | 4시간 | 1일 |

**RTO (Recovery Time Objective)**: 복구 목표 시간
**RPO (Recovery Point Objective)**: 데이터 손실 허용 범위
