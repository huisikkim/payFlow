# 1. 기술 스택

## 1.1 필수 기술 스택 (Required)

### Backend
- **언어**: Java 17 (LTS)
  - 이유: 안정성, 엔터프라이즈 지원, 풍부한 라이브러리 생태계
- **프레임워크**: Spring Boot 3.1+
  - Spring Data JPA (데이터 접근 계층)
  - Spring Security (인증/인가)
  - Spring Validation (입력 검증)
- **API**: REST API
  - OpenAPI 3.0 (Swagger) 문서화

### Database
- **RDBMS**: PostgreSQL 15
  - 선택 이유:
    - MySQL 대비 복잡한 쿼리 성능 우수
    - JSON 타입 지원 (유연한 스킬/필터 데이터 저장)
    - 트랜잭션 안정성
    - 오픈소스, AWS RDS 완벽 지원
- **설계 원칙**:
  - 3NF 정규화 기본
  - PK/FK 제약조건 명시
  - 인덱스 전략: 검색 조건 기반 (학력, 전공, 경력연차, 스킬, 상태)

### Infrastructure (AWS 기반)
- **Compute**: 
  - EC2 t3.medium (초기) → Auto Scaling Group
  - Application Load Balancer (ALB)
- **Database**: 
  - RDS PostgreSQL (Multi-AZ 프로덕션)
- **Storage**: 
  - S3 (이력서 파일, 프로필 이미지)
  - CloudFront (정적 자산 CDN)
- **Network**:
  - VPC (Public/Private Subnet 분리)
  - Security Group (최소 권한 원칙)
- **Monitoring**:
  - CloudWatch (로그, 메트릭, 알람)
  - CloudWatch Logs Insights (로그 분석)

### CI/CD
- **VCS**: GitHub
- **CI/CD**: GitHub Actions
  - 파이프라인: Build → Test → Deploy
  - 단계:
    1. Gradle Build & Unit Test
    2. Integration Test (Testcontainers)
    3. Docker Image Build & Push (ECR)
    4. Deploy to EC2 (Blue-Green or Rolling)
- **컨테이너**: Docker
- **이미지 저장소**: AWS ECR

### 설계 관점
- **계층 구조**:
  ```
  Controller (API Layer)
    ↓
  Service (Business Logic)
    ↓
  Repository (Data Access)
    ↓
  Entity (Domain Model)
  ```
- **모듈 분리**:
  - `applicant-module`: 지원자 관리
  - `jobposting-module`: 채용 공고 관리
  - `application-module`: 지원 추적 관리
  - `common-module`: 공통 유틸리티
  - `notification-module`: 알림 (이메일/SMS)

---

## 1.2 Advanced 기술 스택 (선택)

### AWS 고급 서비스
- **ECS Fargate** (Phase 2 고려)
  - 서버리스 컨테이너 실행
  - Auto Scaling 간소화
  - 운영 부담 감소
  
- **Secrets Manager**
  - DB 자격증명, API 키 안전 보관
  - 자동 로테이션
  
- **WAF (Web Application Firewall)**
  - SQL Injection, XSS 방어
  - Rate Limiting
  - IP 화이트리스트/블랙리스트

- **CloudFront + S3**
  - 정적 자산 CDN
  - 글로벌 배포 시 레이턴시 감소

### Event System (Phase 2 도입 검토)
- **AWS SQS (Simple Queue Service)**
  - 도입 시점: 비동기 처리 필요 시 (알림, 이메일 발송)
  - 장점: 관리형 서비스, 설정 간단
  - 전략:
    - Standard Queue (At-least-once delivery)
    - Dead Letter Queue (DLQ) 설정
    - Retry with Exponential Backoff
    - Visibility Timeout 조정

- **Domain Event (내부 이벤트)**
  - Spring ApplicationEventPublisher 활용
  - 초기에는 동기 처리, 필요 시 @Async 전환
  - 이벤트 예시:
    - `ApplicantAppliedEvent`
    - `ApplicationStatusChangedEvent`
    - `JobPostingClosedEvent`

### Search Engine (Phase 2-3)
- **AWS OpenSearch Service**
  - 도입 시점: 지원자 검색 성능 이슈 발생 시
  - 용도:
    - 전문 검색 (이름, 이메일, 스킬)
    - 복잡한 필터 조합 (학력+전공+경력+스킬)
    - Aggregation (통계, 대시보드)
  - 전략:
    - PostgreSQL → OpenSearch 동기화 (Logstash or Lambda)
    - Read: OpenSearch, Write: PostgreSQL

### Observability (Phase 2-3)
- **Prometheus + Grafana**
  - 메트릭 수집 및 시각화
  - 커스텀 메트릭 (지원자 수, API 응답 시간)
  
- **AWS X-Ray**
  - 분산 추적
  - 병목 구간 식별

---

## 1.3 기술 스택 선택 근거

### PostgreSQL vs MySQL
| 항목 | PostgreSQL | MySQL |
|------|-----------|-------|
| 복잡한 쿼리 성능 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| JSON 지원 | ⭐⭐⭐⭐⭐ (JSONB) | ⭐⭐⭐ |
| 트랜잭션 안정성 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| 커뮤니티 | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| 학습 곡선 | 중간 | 낮음 |

**결론**: 복잡한 필터링 쿼리와 JSON 데이터 처리가 많은 HR 시스템 특성상 PostgreSQL 선택

### EC2 vs ECS Fargate
| 항목 | EC2 | ECS Fargate |
|------|-----|-------------|
| 초기 비용 | 낮음 | 중간 |
| 운영 복잡도 | 높음 | 낮음 |
| Auto Scaling | 수동 설정 | 자동 |
| 학습 곡선 | 낮음 | 중간 |

**결론**: 
- **Phase 1 (0-6개월)**: EC2 + Docker (팀 규모 작고, 빠른 개발 필요)
- **Phase 2 (6개월+)**: ECS Fargate 전환 검토 (운영 안정화 후)

### GitHub Actions vs Jenkins
| 항목 | GitHub Actions | Jenkins |
|------|----------------|---------|
| 설정 복잡도 | 낮음 | 높음 |
| 유지보수 | GitHub 관리 | 자체 관리 |
| 비용 | 무료 (Public), 유료 (Private) | 서버 비용 |
| 통합 | GitHub 네이티브 | 플러그인 필요 |

**결론**: GitHub Actions (빠른 설정, 유지보수 부담 적음)

---

## 1.4 기술 스택 로드맵

```
Phase 1 (0-3개월): MVP
├─ Java 17 + Spring Boot 3.1
├─ PostgreSQL (RDS)
├─ EC2 + ALB
├─ S3 (파일 저장)
├─ GitHub Actions (CI/CD)
└─ CloudWatch (기본 모니터링)

Phase 2 (3-6개월): 안정화
├─ Redis (캐싱)
├─ SQS (비동기 알림)
├─ Secrets Manager
├─ WAF (보안 강화)
└─ CloudFront (CDN)

Phase 3 (6-12개월): 확장
├─ ECS Fargate (컨테이너 오케스트레이션)
├─ OpenSearch (검색 엔진)
├─ Prometheus + Grafana (고급 모니터링)
└─ Auto Scaling 최적화
```

---

## 1.5 개발 도구

### IDE & Tools
- IntelliJ IDEA Ultimate (Java 개발)
- DBeaver (DB 관리)
- Postman (API 테스트)
- Docker Desktop (로컬 개발)

### 협업 도구
- Notion (문서, 위키)
- Jira (이슈 트래킹, 스프린트)
- Slack (커뮤니케이션)
- Figma (디자인)

### 문서화
- OpenAPI 3.0 (Swagger UI)
- ERDCloud (ERD)
- Mermaid (다이어그램)
- Confluence (기술 문서)
