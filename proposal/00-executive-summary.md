# AINJOB HR SaaS 플랫폼 - 기술 아키텍처 제안서 요약

## 프로젝트 개요

**AINJOB**은 기업회원이 적합한 지원자를 채용 공고를 통해 접수·관리하는 HR SaaS 플랫폼입니다.

### 핵심 목표
- **팀 규모**: 4-5명 (BE 2-3명, FE 1-2명, 디자이너 1명)
- **개발 기간**: 6-8개월
- **아키텍처**: 실무 적용 가능한 현실적 설계
- **기술 스택**: Java 17/Spring Boot 3.1, PostgreSQL, AWS

---

## 주요 설계 결정

### 1. 기술 스택

| 영역 | 선택 | 이유 |
|------|------|------|
| **Backend** | Java 17 + Spring Boot 3.1 | 안정성, 엔터프라이즈 지원, 풍부한 생태계 |
| **Database** | PostgreSQL 15 | 복잡한 쿼리 성능, JSON 지원, 트랜잭션 안정성 |
| **Infrastructure** | AWS (EC2, RDS, S3) | 확장성, 관리 편의성, 풍부한 서비스 |
| **CI/CD** | GitHub Actions | 빠른 설정, GitHub 네이티브, 유지보수 간편 |

### 2. 아키텍처 방식: 모듈러 모놀리스

**선택 이유:**
- ✅ 팀 규모(4-5명)에 적합
- ✅ 빠른 개발 속도 (6-8개월 내 완성 가능)
- ✅ 단일 배포 단위로 운영 복잡도 낮음
- ✅ 향후 MSA 전환 가능한 구조

**모듈 구조:**
```
applicant-module (지원자 관리)
jobposting-module (채용 공고 관리)
application-module (지원 추적)
notification-module (알림)
common-module (공통)
```

### 3. AWS 인프라 구성

```
CloudFront (CDN)
  ↓
ALB (Load Balancer)
  ↓
EC2 Auto Scaling Group (API Server)
  ↓
RDS PostgreSQL (Multi-AZ) + ElastiCache Redis
  ↓
S3 (파일 저장)
```

**보안:**
- VPC (Public/Private Subnet 분리)
- Security Group (최소 권한 원칙)
- WAF (SQL Injection, XSS 방어)
- Secrets Manager (비밀 정보 관리)

---

## 도메인 설계 (DDD)

### 핵심 Aggregate

1. **Applicant** (지원자)
   - Education (학력)
   - Career (경력)
   - CareerSkill (경력 기술)

2. **Resume** (이력서)
   - 버전 관리
   - S3 파일 저장

3. **JobPosting** (채용 공고)
   - Qualification (자격 요건)
   - JobPostingSkill (요구 기술)

4. **ApplicationTracking** (지원 추적)
   - ApplicationStatusHistory (상태 이력)
   - 상태 전이 규칙 (역행 금지)

### 트랜잭션 경계

- **강한 일관성**: Aggregate 내부 (단일 트랜잭션)
- **최종 일관성**: Aggregate 간 (Domain Event)

---

## 필터 매칭 시스템

### Filter Matching (필수 조건)

**백엔드 개발자 예시:**
1. 학력: 학사 이상
2. 전공: 컴퓨터공학 OR 소프트웨어공학 OR 정보보안
3. 스킬: Java AND Spring Boot AND AWS (모두 보유)
4. 경력: 5년 이상

**결과:** 모든 조건 충족 시 합격, 하나라도 미충족 시 불합격

### Soft Matching (가산점)

**온톨로지 기반 유사도:**
- Spring Boot 경험 → Java 점수 +80% (의존 관계)
- MySQL 경험 → PostgreSQL 점수 +60% (유사 기술)
- ❌ Java ≠ Python (언어 간 자동 매칭 금지)

**최종 점수:** Filter 통과 + Soft Matching 점수로 추천 순위 결정

---

## 데이터베이스 설계

### 주요 테이블 (12개)

1. company (기업)
2. job_posting (채용 공고)
3. job_posting_skill (공고별 요구 기술)
4. skill (기술)
5. applicant (지원자)
6. education (학력)
7. major (전공)
8. career (경력)
9. career_skill (경력 기술)
10. resume_tracking (이력서 버전 관리)
11. application_tracking (지원 추적)
12. application_status_history (지원 상태 이력)

### 인덱스 전략

- 검색 조건 기반: 학력, 전공, 경력, 스킬, 상태
- 복합 인덱스: (job_posting_id, status), (applicant_id, is_active)
- N+1 방지: Fetch Join, EntityGraph

---

## 핵심 API

### 지원자 목록 조회 (필터링)

```http
GET /api/v1/applicants?jobPostingId=1&status=APPLIED&education=BACHELOR&skills=Java,Spring Boot&page=0&size=20
```

**응답:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "김철수",
        "education": {"level": "BACHELOR", "major": "컴퓨터공학"},
        "totalExperience": 6,
        "skills": ["Java", "Spring Boot", "AWS"],
        "matchingScore": 95
      }
    ],
    "totalElements": 6
  }
}
```

### 지원 상태 변경

```http
PATCH /api/v1/applications/1/status
Body: {"fromStatus": "APPLIED", "toStatus": "DOCUMENT_PASS", "reason": "..."}
```

**워크플로우:**
```
APPLIED → DOCUMENT_PASS → INTERVIEW_1 → INTERVIEW_2 → FINAL_PASS
                                                ↓
                                            REJECTED (어느 단계에서나 가능)
```

---

## 비기능 요구사항

### 성능

- **응답 시간**: 단순 조회 < 200ms, 목록 조회 < 500ms
- **캐싱**: Redis (지원자 상세 5분, 공고 목록 10분)
- **페이지네이션**: 기본 20개, 최대 100개

### 가용성

- **SLA**: 99.9% (월 43분 다운타임 허용)
- **RTO**: 5분 (Auto Scaling 자동 복구)
- **RPO**: 5분 (RDS 자동 백업)

### 보안

- **인증**: JWT (Access Token 15분, Refresh Token 7일)
- **인가**: RBAC (APPLICANT, COMPANY_HR, COMPANY_ADMIN, SYSTEM_ADMIN)
- **암호화**: HTTPS 필수, 비밀번호 BCrypt, S3 AES256
- **방어**: WAF, Rate Limiting, 브루트포스 방어

---

## 개발 조직 & 협업

### 팀 구성

- Backend Developer 1 (Tech Lead + DevOps 겸임)
- Backend Developer 2
- Frontend Developer 1
- Designer 1

### 브랜치 전략: Git Flow

```
main (production)
  ↑
develop (integration)
  ↑
feature/* (개발)
```

### 코드 리뷰

- 최소 1명 이상 Approve 필요
- Tech Lead 최종 승인
- Squash Merge to develop

### 협업 도구

- **Jira**: 이슈 트래킹, 스프린트 관리
- **Notion**: 문서 관리, 회의록
- **Slack**: 커뮤니케이션, 알림
- **Figma**: 디자인, 프로토타입

---

## 개발 일정 (8개월)

### Phase 1: 기반 구축 (0-2개월)
- 인프라 구축 (AWS VPC, RDS, S3)
- 프로젝트 셋업 (Spring Boot, React)
- CI/CD 파이프라인 (GitHub Actions)
- 기본 기능 (인증, CRUD)

### Phase 2: 핵심 기능 개발 (2-5개월)
- 지원자 관리 (목록, 상세, 필터링)
- 지원 추적 (상태 관리, 알림)
- 고급 기능 (매칭, 통계, Admin)

### Phase 3: 고도화 및 안정화 (5-8개월)
- 보안 강화 (WAF, Multi-AZ, 모니터링)
- 성능 최적화 (캐싱, 인덱스, 부하 테스트)
- 출시 준비 (프로덕션 배포, 안정화)

### 마일스톤

| 마일스톤 | 기간 | 주요 산출물 |
|---------|------|-----------|
| M1: 프로젝트 셋업 | Week 1-4 | 인프라, CI/CD |
| M2: 기본 기능 | Week 5-8 | 인증, CRUD |
| M3: 지원자 관리 | Week 9-12 | 목록, 필터링 |
| M4: 지원 추적 | Week 13-16 | 상태 관리, 알림 |
| M5: 고급 기능 | Week 17-20 | 매칭, Admin |
| M6: 보안 강화 | Week 21-24 | WAF, HA |
| M7: 고도화 | Week 25-28 | 검색, 이벤트 |
| M8: 출시 | Week 29-32 | 프로덕션 배포 |

---

## 리스크 관리

| 리스크 | 확률 | 영향 | 대응 방안 |
|--------|------|------|----------|
| 요구사항 변경 | 높음 | 중간 | 애자일 방식, 2주 스프린트 |
| 기술 학습 곡선 | 중간 | 중간 | 페어 프로그래밍, 기술 공유 |
| 인프라 장애 | 낮음 | 높음 | Multi-AZ, Auto Scaling |
| 성능 이슈 | 중간 | 높음 | 조기 부하 테스트, 캐싱 |
| 일정 지연 | 중간 | 중간 | 버퍼 기간, 우선순위 조정 |

---

## 성공 지표 (KPI)

### 개발 지표

- 코드 커버리지: 80% 이상
- API 응답 시간: 500ms 이하
- 배포 빈도: 주 2회 이상

### 비즈니스 지표

- 시스템 가용성: 99.9%
- 사용자 만족도: 4.0/5.0 이상
- 지원 처리 시간: 평균 3일 이내

---

## 확장 계획 (Post-Launch)

### Phase 4: 고급 기능 (9-12개월)

- **검색 엔진**: OpenSearch 도입 (전문 검색, 자동완성)
- **이벤트 시스템**: SQS 기반 비동기 처리
- **AI 추천**: 머신러닝 기반 지원자 추천
- **모바일 앱**: React Native 기반 모바일 앱
- **다국어 지원**: i18n 적용

### MSA 전환 검토

**전환 시점:** 팀 규모 10명 이상, 트래픽 10배 증가 시

**전환 전략:**
1. 모듈별 독립 배포 가능하도록 API 분리
2. 데이터베이스 분리 (Applicant DB, JobPosting DB)
3. 이벤트 기반 통신 (Kafka or SQS)
4. API Gateway 도입

---

## 결론

본 제안서는 **AINJOB HR SaaS 플랫폼**을 6-8개월 내에 구축 가능한 현실적이고 확장 가능한 아키텍처를 제시합니다.

### 핵심 강점

1. **실무 적용 가능**: 팀 규모와 기간을 고려한 현실적 설계
2. **확장 가능**: 모듈러 모놀리스 → MSA 전환 가능
3. **안정성**: Multi-AZ, Auto Scaling, 모니터링
4. **보안**: WAF, Secrets Manager, RBAC
5. **협업 프로세스**: Git Flow, 코드 리뷰, 애자일

### 차별화 포인트

- **필터 매칭 시스템**: 하드 조건 + 온톨로지 기반 Soft Matching
- **도메인 중심 설계**: DDD 기반 Aggregate 설계
- **이벤트 기반 아키텍처**: 최종 일관성, 비동기 처리
- **운영 자동화**: CI/CD, Auto Scaling, 모니터링

---

## 제출 문서 목록

1. ✅ 기술 스택 정의
2. ✅ 시스템 아키텍처 설계 (AWS 구성도)
3. ✅ 도메인 모델 & DDD 설계
4. ✅ 필터 매칭 로직 설계
5. ✅ 데이터베이스 & ERD 설계
6. ✅ 테이블 정의 & SQL 스크립트
7. ✅ 더미 데이터 & 합격자 조회 쿼리
8. ✅ 핵심 기능 & API 설계
9. ✅ 비기능 요구사항 & 보안
10. ✅ 시퀀스 다이어그램
11. ✅ 개발 조직 & 협업 프로세스
12. ✅ 개발 일정표

**총 페이지 수**: 약 150페이지 (Markdown 기준)

---

## 연락처

**지원자**: [이름]
**이메일**: [이메일]
**GitHub**: [GitHub URL]
**LinkedIn**: [LinkedIn URL]

**제출일**: 2024년 11월 17일
