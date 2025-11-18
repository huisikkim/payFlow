# AINJOB - HR SaaS Platform

proposal 문서를 기반으로 구현한 채용 관리 시스템입니다.

## 📋 주요 기능

### 1. 기업 관리
- 기업 정보 등록 및 조회
- 사업자등록번호 기반 중복 체크

### 2. 채용 공고 관리
- 채용 공고 등록 (제목, 설명, 포지션, 자격 요건)
- 요구 스킬 설정 (필수/선택, 최소 숙련도)
- 공고 상태 관리 (임시저장, 공개, 마감)
- 최소 학력 및 경력 연차 설정
- 허용 전공 설정

### 3. 지원자 관리
- 지원자 정보 등록 (기본 정보, 학력, 경력)
- 경력별 스킬 및 숙련도 관리
- 총 경력 연차 자동 계산
- 이력서 버전 관리

### 4. 지원 관리
- 채용 공고 지원
- 지원 상태 관리 (지원완료 → 서류합격 → 1차면접 → 2차면접 → 최종합격)
- 상태 변경 이력 추적
- 역행 방지 (불합격 제외)

## 🏗️ 아키텍처

### 도메인 모델 (DDD 기반)

```
AINApplicant (지원자)
├── AINEducation (학력)
│   └── AINMajor (전공)
└── AINCareer (경력)
    └── AINCareerSkill (경력 스킬)
        └── AINSkill (스킬)

AINJobPosting (채용 공고)
├── AINCompany (기업)
└── AINJobPostingSkill (요구 스킬)
    └── AINSkill (스킬)

AINApplicationTracking (지원 추적)
└── AINApplicationStatusHistory (상태 이력)

AINResumeTracking (이력서)
```

### 계층 구조

```
presentation/     # Controller, Web UI
├── AINCompanyController
├── AINJobPostingController
├── AINApplicantController
├── AINApplicationController
└── AINWebController

application/      # Service, DTO
├── AINCompanyService
├── AINJobPostingService
├── AINApplicantService
└── AINApplicationService

domain/          # Entity, Enum, Value Object
├── AINCompany
├── AINJobPosting
├── AINApplicant
├── AINApplicationTracking
└── ...

infrastructure/  # Repository, Data Initializer
├── AINCompanyRepository
├── AINJobPostingRepository
├── AINApplicantRepository
├── AINApplicationTrackingRepository
└── AINDataInitializer
```

## 🚀 실행 방법

### 1. 애플리케이션 시작

```bash
./gradlew bootRun
```

### 2. 웹 UI 접속

```
http://localhost:8080/ain
```

### 3. 초기 데이터

애플리케이션 시작 시 자동으로 생성됩니다:
- 기업 2개
- 채용 공고 2개 (백엔드, 프론트엔드)
- 지원자 2명
- 지원 2건

## 📡 API 엔드포인트

### 기업 API

```http
POST   /api/ain/companies          # 기업 등록
GET    /api/ain/companies          # 기업 목록
GET    /api/ain/companies/{id}     # 기업 상세
```

### 채용 공고 API

```http
POST   /api/ain/job-postings       # 공고 등록
GET    /api/ain/job-postings       # 공고 목록
GET    /api/ain/job-postings/{id}  # 공고 상세
PATCH  /api/ain/job-postings/{id}/open   # 공고 공개
PATCH  /api/ain/job-postings/{id}/close  # 공고 마감
DELETE /api/ain/job-postings/{id}  # 공고 삭제
```

### 지원자 API

```http
POST   /api/ain/applicants         # 지원자 등록
GET    /api/ain/applicants         # 지원자 목록
GET    /api/ain/applicants/{id}    # 지원자 상세
DELETE /api/ain/applicants/{id}    # 지원자 삭제
```

### 지원 API

```http
POST   /api/ain/applications       # 지원하기
GET    /api/ain/applications       # 전체 지원 목록
GET    /api/ain/applications/{id}  # 지원 상세
GET    /api/ain/applications/{id}/matching-score  # 매칭 점수 조회
GET    /api/ain/applications/job-posting/{jobPostingId}  # 공고별 지원 목록
GET    /api/ain/applications/applicant/{applicantId}     # 지원자별 지원 목록
PATCH  /api/ain/applications/{id}/status  # 상태 변경
```

### 매칭 API

```http
GET    /api/ain/job-postings/{id}/qualified-applicants  # 자격 요건 충족 지원자 목록
```

## 📝 API 사용 예시

### 1. 기업 등록

```bash
curl -X POST http://localhost:8080/api/ain/companies \
  -H "Content-Type: application/json" \
  -d '{
    "name": "테크스타트업",
    "businessNumber": "111-22-33444",
    "industry": "IT",
    "address": "서울시 강남구",
    "phone": "02-1234-5678",
    "email": "contact@techstartup.com"
  }'
```

### 2. 채용 공고 등록

```bash
curl -X POST http://localhost:8080/api/ain/job-postings \
  -H "Content-Type: application/json" \
  -d '{
    "companyId": 1,
    "title": "백엔드 개발자 채용",
    "description": "Java/Spring Boot 개발자를 모집합니다.",
    "position": "BACKEND",
    "minEducationLevel": "BACHELOR",
    "acceptedMajors": ["컴퓨터공학", "소프트웨어공학"],
    "minYearsOfExperience": 3,
    "openDate": "2024-01-01",
    "closeDate": "2024-12-31",
    "requiredSkills": [
      {
        "skillName": "Java",
        "isRequired": true,
        "minProficiency": 4
      },
      {
        "skillName": "Spring Boot",
        "isRequired": true,
        "minProficiency": 3
      }
    ]
  }'
```

### 3. 지원자 등록

```bash
curl -X POST http://localhost:8080/api/ain/applicants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "홍길동",
    "email": "hong@example.com",
    "phone": "010-1234-5678",
    "birthDate": "1995-03-15",
    "city": "서울",
    "district": "강남구",
    "educations": [
      {
        "educationLevel": "BACHELOR",
        "majorName": "컴퓨터공학",
        "schoolName": "서울대학교",
        "startDate": "2013-03-01",
        "endDate": "2017-02-28",
        "status": "GRADUATED"
      }
    ],
    "careers": [
      {
        "companyName": "네이버",
        "position": "Backend Developer",
        "description": "Java/Spring 개발",
        "startDate": "2017-03-01",
        "endDate": null,
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
  }'
```

### 4. 지원 상태 변경

```bash
curl -X PATCH http://localhost:8080/api/ain/applications/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "fromStatus": "APPLIED",
    "toStatus": "DOCUMENT_PASS",
    "reason": "서류 검토 완료. 기술 스택 및 경력이 요구사항에 부합함.",
    "changedBy": "HR-001"
  }'
```

## 🎨 웹 UI 화면

### 메인 화면
- `/ain` - 대시보드 (기업, 공고, 지원자, 지원 관리 메뉴)

### 기업 관리
- `/ain/companies` - 기업 목록
- `/ain/companies/new` - 기업 등록

### 채용 공고
- `/ain/job-postings` - 공고 목록 (상태별 필터)
- `/ain/job-postings/{id}` - 공고 상세
- `/ain/job-postings/new` - 공고 등록

### 지원자 관리
- `/ain/applicants` - 지원자 목록
- `/ain/applicants/{id}` - 지원자 상세
- `/ain/applicants/new` - 지원자 등록

### 지원 관리
- `/ain/applications` - 지원 목록 (상태별 필터)
- `/ain/applications/{id}` - 지원 상세

## 🔑 주요 특징

### 1. DDD 기반 설계
- Aggregate 단위로 트랜잭션 경계 명확화
- Entity와 Value Object 구분
- 도메인 로직을 도메인 모델에 캡슐화

### 2. 상태 관리
- 지원 상태 전이 규칙 (역행 금지)
- 상태 변경 이력 자동 기록
- 불합격은 어느 단계에서나 가능

### 3. 데이터 무결성
- 중복 지원 방지 (지원자 + 공고 유니크)
- 이메일 중복 체크
- 사업자등록번호 중복 체크

### 4. 카프카 이벤트 없음
- 요청사항에 따라 카프카 이벤트 기능 제외
- 동기 처리로 단순화

## 📊 데이터베이스 테이블

```
ain_company                    # 기업
ain_job_posting               # 채용 공고
ain_job_posting_skill         # 공고별 요구 스킬
ain_skill                     # 스킬
ain_applicant                 # 지원자
ain_education                 # 학력
ain_major                     # 전공
ain_career                    # 경력
ain_career_skill              # 경력별 스킬
ain_resume_tracking           # 이력서
ain_application_tracking      # 지원 추적
ain_application_status_history # 지원 상태 이력
```

## 🛠️ 기술 스택

- **Backend**: Java 17, Spring Boot 3.x
- **Database**: H2 (In-Memory)
- **ORM**: Spring Data JPA
- **Template Engine**: Thymeleaf
- **Build Tool**: Gradle

## 📌 참고사항

- 모든 클래스명에 "AIN" 접두사를 사용하여 기존 recruitment 모듈과 충돌 방지
- H2 인메모리 DB 사용으로 애플리케이션 재시작 시 데이터 초기화
- 초기 데이터는 AINDataInitializer에서 자동 생성
- proposal 문서의 설계를 최대한 반영하여 구현

### 5. 합격자 자동 필터링 ⭐ NEW
- 자격 요건 기반 자동 매칭
- 학력, 전공, 스킬, 경력 조건 체크
- 매칭 점수 계산 (100점 만점)
- 합격자/불합격자 자동 분류
- 상세 매칭 분석 리포트

## 🎯 향후 개선 사항

- 파일 업로드 기능 (S3 연동)
- 통계 및 대시보드
- 알림 기능
- 검색 기능 강화
- 페이지네이션 개선
