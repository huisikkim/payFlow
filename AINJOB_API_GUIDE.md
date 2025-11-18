# AINJOB API 가이드

## 개요

AINJOB 모듈은 proposal 문서의 설계를 기반으로 구현된 HR SaaS 플랫폼의 핵심 API입니다.

### 주요 기능

1. **지원자 관리 (Applicant Management)**
   - 지원자 등록 (학력, 경력, 스킬 포함)
   - 지원자 조회

2. **채용 공고 관리 (Job Posting Management)**
   - 채용 공고 등록 (자격 요건, 요구 스킬 포함)
   - 공고 상태 관리 (DRAFT → OPEN → CLOSED)

3. **지원 추적 (Application Tracking)**
   - 지원 접수
   - 지원 상태 변경 (APPLIED → DOCUMENT_PASS → INTERVIEW_1 → INTERVIEW_2 → FINAL_PASS)
   - 상태 이력 관리

## 아키텍처

### DDD 기반 설계

```
ainjob/
├── domain/              # 도메인 모델
│   ├── Applicant       # 지원자 Aggregate
│   ├── AinjobJobPosting # 채용 공고 Aggregate
│   └── ApplicationTracking # 지원 추적 Aggregate
├── application/         # 애플리케이션 서비스
│   ├── ApplicantService
│   ├── JobPostingService
│   └── ApplicationTrackingService
└── presentation/        # REST API 컨트롤러
    ├── ApplicantController
    ├── JobPostingController
    └── ApplicationController
```

### 주요 Aggregate

1. **Applicant (지원자)**
   - Education (학력)
   - Career (경력)
   - CareerSkill (경력별 스킬)

2. **AinjobJobPosting (채용 공고)**
   - Qualification (자격 요건)
   - JobPostingSkill (요구 스킬)

3. **ApplicationTracking (지원 추적)**
   - ApplicationStatusHistory (상태 이력)

## API 엔드포인트

### 1. 지원자 API

#### 지원자 생성
```http
POST /api/ainjob/applicants
Content-Type: application/json

{
  "name": "김철수",
  "email": "kim.cs@email.com",
  "phone": "010-1111-0001",
  "birthDate": "1990-01-15",
  "address": {
    "city": "서울",
    "district": "강남구",
    "detail": "테헤란로 123"
  },
  "educations": [
    {
      "level": "BACHELOR",
      "majorName": "컴퓨터공학",
      "schoolName": "서울대학교",
      "startDate": "2008-03-01",
      "endDate": "2012-02-28",
      "status": "GRADUATED"
    }
  ],
  "careers": [
    {
      "companyName": "네이버",
      "position": "Backend Developer",
      "description": "Java/Spring Boot 기반 서비스 개발",
      "startDate": "2012-03-01",
      "endDate": "2017-12-31",
      "skills": [
        {
          "skillName": "Java",
          "proficiencyLevel": 5
        },
        {
          "skillName": "Spring Boot",
          "proficiencyLevel": 4
        }
      ]
    }
  ]
}
```

#### 지원자 조회
```http
GET /api/ainjob/applicants/{id}
GET /api/ainjob/applicants
GET /api/ainjob/applicants/email/{email}
```

### 2. 채용 공고 API

#### 채용 공고 생성
```http
POST /api/ainjob/job-postings
Content-Type: application/json

{
  "companyId": 1,
  "title": "백엔드 개발자 채용",
  "description": "Java/Spring Boot 기반 백엔드 개발자를 모집합니다.",
  "position": "BACKEND",
  "qualification": {
    "minEducationLevel": "BACHELOR",
    "acceptedMajors": ["컴퓨터공학", "소프트웨어공학", "정보보안"],
    "minYearsOfExperience": 5
  },
  "requiredSkills": [
    {
      "skillName": "Java",
      "isRequired": true,
      "minProficiency": 4
    },
    {
      "skillName": "Spring Boot",
      "isRequired": true,
      "minProficiency": 4
    },
    {
      "skillName": "AWS",
      "isRequired": true,
      "minProficiency": 3
    }
  ],
  "openDate": "2024-01-01",
  "closeDate": "2024-12-31"
}
```

#### 채용 공고 조회
```http
GET /api/ainjob/job-postings/{id}
GET /api/ainjob/job-postings
GET /api/ainjob/job-postings/company/{companyId}
GET /api/ainjob/job-postings/status/{status}
```

#### 채용 공고 상태 변경
```http
PATCH /api/ainjob/job-postings/{id}/open
PATCH /api/ainjob/job-postings/{id}/close
```

### 3. 지원 추적 API

#### 지원하기
```http
POST /api/ainjob/applications
Content-Type: application/json

{
  "applicantId": 1,
  "jobPostingId": 1,
  "resumeId": null
}
```

#### 지원 상태 변경
```http
PATCH /api/ainjob/applications/{id}/status
Content-Type: application/json

{
  "fromStatus": "APPLIED",
  "toStatus": "DOCUMENT_PASS",
  "reason": "서류 검토 완료. 기술 스택 및 경력이 요구사항에 부합함."
}
```

#### 지원 내역 조회
```http
GET /api/ainjob/applications/{id}
GET /api/ainjob/applications/applicant/{applicantId}
GET /api/ainjob/applications/job-posting/{jobPostingId}
GET /api/ainjob/applications/job-posting/{jobPostingId}/status/{status}
```

## 도메인 모델

### Enum 타입

#### EducationLevel
- `HIGH_SCHOOL`: 고등학교
- `ASSOCIATE`: 전문학사
- `BACHELOR`: 학사
- `MASTER`: 석사
- `DOCTORATE`: 박사

#### ApplicationStatus
- `APPLIED`: 지원
- `DOCUMENT_PASS`: 서류합격
- `INTERVIEW_1`: 1차면접
- `INTERVIEW_2`: 2차면접
- `FINAL_PASS`: 최종합격
- `REJECTED`: 불합격

#### JobPostingStatus
- `DRAFT`: 임시저장
- `OPEN`: 공개
- `CLOSED`: 마감

### 비즈니스 규칙

1. **지원자 등록**
   - 이메일 중복 불가
   - 최소 1개 이상의 학력 필요

2. **채용 공고**
   - 최소 1개 이상의 요구 스킬 필요
   - 마감일은 오픈일보다 이후여야 함

3. **지원 추적**
   - 한 지원자는 같은 공고에 1번만 지원 가능
   - 상태는 순방향으로만 변경 (REJECTED 제외)
   - 상태 변경 시 반드시 이력 기록

## 테스트

### PowerShell 스크립트 실행
```powershell
.\test-ainjob-api.ps1
```

### Bash 스크립트 실행
```bash
chmod +x test-ainjob-api.sh
./test-ainjob-api.sh
```

## 데이터베이스 테이블

### 주요 테이블
- `ainjob_applicants`: 지원자
- `ainjob_educations`: 학력
- `ainjob_careers`: 경력
- `ainjob_career_skills`: 경력별 스킬
- `ainjob_majors`: 전공
- `ainjob_skills`: 스킬
- `ainjob_job_postings`: 채용 공고
- `ainjob_job_posting_skills`: 공고별 요구 스킬
- `ainjob_application_trackings`: 지원 추적
- `ainjob_application_status_histories`: 지원 상태 이력

## 향후 개선 사항

1. **필터 매칭 시스템**
   - 학력, 전공, 스킬, 경력 기반 자동 매칭
   - 매칭 점수 계산

2. **Soft Matching**
   - 온톨로지 기반 유사 스킬 매칭
   - 추천 순위 결정

3. **알림 시스템**
   - 지원 완료 알림
   - 상태 변경 알림

4. **이력서 관리**
   - S3 파일 업로드
   - 버전 관리

5. **통계 및 리포트**
   - 공고별 지원자 통계
   - 합격률 분석

## 참고 문서

- [Proposal 문서](./proposal/README.md)
- [API 설계](./proposal/08-api-design.md)
- [도메인 설계](./proposal/03-domain-design.md)
- [데이터베이스 ERD](./proposal/05-database-erd.md)
