# 8. 핵심 기능 & API 설계

## 8.1 API 기본 규격

### Base URL
```
Production: https://api.ainjob.com/v1
Staging: https://api-staging.ainjob.com/v1
Development: http://localhost:8080/api/v1
```

### 인증
```
Authorization: Bearer {access_token}
```

### 공통 응답 포맷

**성공 응답:**
```json
{
  "success": true,
  "data": { ... },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**에러 응답:**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_REQUEST",
    "message": "요청 데이터가 유효하지 않습니다.",
    "details": [
      {
        "field": "email",
        "message": "이메일 형식이 올바르지 않습니다."
      }
    ]
  },
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/api/v1/applicants"
}
```

### 에러 코드

| HTTP Status | Error Code | 설명 |
|-------------|------------|------|
| 400 | INVALID_REQUEST | 요청 데이터 검증 실패 |
| 401 | UNAUTHORIZED | 인증 실패 |
| 403 | FORBIDDEN | 권한 없음 |
| 404 | NOT_FOUND | 리소스 없음 |
| 409 | CONFLICT | 중복 또는 상태 충돌 |
| 500 | INTERNAL_ERROR | 서버 내부 오류 |

---

## 8.2 채용 공고 API

### 8.2.1 공고 등록

```http
POST /api/v1/job-postings
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
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

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "companyId": 123,
    "title": "백엔드 개발자 채용",
    "position": "BACKEND",
    "status": "DRAFT",
    "createdAt": "2024-01-15T10:30:00Z"
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

## 8.3 지원자 관리 API

### 8.3.1 지원자 목록 조회 (필터링)

```http
GET /api/v1/applicants?jobPostingId=1&status=APPLIED&education=BACHELOR&major=컴퓨터공학&careerYears=5-10&skills=Java,Spring Boot&sort=appliedAt,desc&page=0&size=20
Authorization: Bearer {token}
```

**Query Parameters:**

| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| jobPostingId | Long | ✅ | 공고 ID | 1 |
| status | String | | 지원 상태 | APPLIED, DOCUMENT_PASS |
| education | String | | 최소 학력 | BACHELOR, MASTER |
| major | String | | 전공 | 컴퓨터공학 |
| careerYears | String | | 경력 범위 | 0-3, 3-5, 5-8, 8+ |
| skills | String | | 스킬 (쉼표 구분) | Java,Spring Boot |
| sort | String | | 정렬 | appliedAt,desc |
| page | Integer | | 페이지 번호 (0부터) | 0 |
| size | Integer | | 페이지 크기 | 20 |

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "김철수",
        "email": "kim.cs@email.com",
        "phone": "010-1111-0001",
        "education": {
          "level": "BACHELOR",
          "major": "컴퓨터공학",
          "schoolName": "서울대학교"
        },
        "totalExperience": 6,
        "skills": ["Java", "Spring Boot", "AWS", "Kubernetes"],
        "applicationStatus": "APPLIED",
        "appliedAt": "2024-01-15T09:00:00Z",
        "matchingScore": 95
      },
      {
        "id": 2,
        "name": "이영희",
        "email": "lee.yh@email.com",
        "phone": "010-1111-0002",
        "education": {
          "level": "MASTER",
          "major": "소프트웨어공학",
          "schoolName": "KAIST"
        },
        "totalExperience": 8,
        "skills": ["Java", "Spring Boot", "AWS", "Docker"],
        "applicationStatus": "APPLIED",
        "appliedAt": "2024-01-15T10:30:00Z",
        "matchingScore": 98
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "sort": {
        "sorted": true,
        "orders": [
          {
            "property": "appliedAt",
            "direction": "DESC"
          }
        ]
      }
    },
    "totalElements": 6,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

### 8.3.2 지원자 상세 조회

```http
GET /api/v1/applicants/1
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "김철수",
    "email": "kim.cs@email.com",
    "phone": "010-1111-0001",
    "birthDate": "1990-01-15",
    "address": {
      "city": "서울",
      "district": "강남구"
    },
    "educations": [
      {
        "id": 1,
        "level": "BACHELOR",
        "major": "컴퓨터공학",
        "schoolName": "서울대학교",
        "startDate": "2008-03-01",
        "endDate": "2012-02-28",
        "status": "GRADUATED"
      }
    ],
    "careers": [
      {
        "id": 1,
        "companyName": "네이버",
        "position": "Backend Developer",
        "description": "Java/Spring Boot 기반 서비스 개발",
        "startDate": "2012-03-01",
        "endDate": "2017-12-31",
        "yearsOfExperience": 5,
        "skills": [
          {
            "name": "Java",
            "proficiencyLevel": 5
          },
          {
            "name": "Spring Boot",
            "proficiencyLevel": 4
          }
        ]
      },
      {
        "id": 2,
        "companyName": "카카오",
        "position": "Senior Backend Developer",
        "description": "MSA 아키텍처 설계 및 구현",
        "startDate": "2018-01-01",
        "endDate": null,
        "yearsOfExperience": 6,
        "skills": [
          {
            "name": "Java",
            "proficiencyLevel": 5
          },
          {
            "name": "Spring Boot",
            "proficiencyLevel": 5
          },
          {
            "name": "AWS",
            "proficiencyLevel": 5
          },
          {
            "name": "Kubernetes",
            "proficiencyLevel": 4
          }
        ]
      }
    ],
    "resume": {
      "id": 1,
      "title": "김철수 이력서",
      "fileName": "김철수_이력서.pdf",
      "fileSize": 524288,
      "downloadUrl": "https://ainjob-resumes.s3.amazonaws.com/resumes/2024/01/applicant-1-v1.pdf?signature=...",
      "uploadedAt": "2024-01-10T15:30:00Z"
    },
    "application": {
      "id": 1,
      "jobPostingId": 1,
      "jobPostingTitle": "백엔드 개발자 채용",
      "status": "APPLIED",
      "appliedAt": "2024-01-15T09:00:00Z",
      "statusHistory": [
        {
          "fromStatus": null,
          "toStatus": "APPLIED",
          "reason": "지원 접수",
          "changedBy": "SYSTEM",
          "changedAt": "2024-01-15T09:00:00Z"
        }
      ]
    },
    "matchingScore": 95,
    "createdAt": "2024-01-10T15:30:00Z",
    "updatedAt": "2024-01-15T09:00:00Z"
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

### 8.3.3 지원 상태 변경

```http
PATCH /api/v1/applications/1/status
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "fromStatus": "APPLIED",
  "toStatus": "DOCUMENT_PASS",
  "reason": "서류 검토 완료. 기술 스택 및 경력이 요구사항에 부합함."
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "applicantId": 1,
    "applicantName": "김철수",
    "jobPostingId": 1,
    "status": "DOCUMENT_PASS",
    "previousStatus": "APPLIED",
    "updatedAt": "2024-01-16T14:30:00Z",
    "updatedBy": "HR-001"
  },
  "timestamp": "2024-01-16T14:30:00Z"
}
```

**Error Response (409 Conflict):**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_STATUS_TRANSITION",
    "message": "상태 변경이 불가능합니다.",
    "details": [
      {
        "field": "toStatus",
        "message": "DOCUMENT_PASS에서 APPLIED로 역행할 수 없습니다."
      }
    ]
  },
  "timestamp": "2024-01-16T14:30:00Z",
  "path": "/api/v1/applications/1/status"
}
```

---

### 8.3.4 알림 템플릿 미리보기

```http
POST /api/v1/applications/1/notifications/preview
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "template": "DOCUMENT_PASS",
  "placeholders": {
    "applicantName": "김철수",
    "jobPostingTitle": "백엔드 개발자 채용",
    "companyName": "테크이노베이션",
    "interviewDate": "2024-01-20",
    "interviewTime": "14:00"
  }
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "template": "DOCUMENT_PASS",
    "subject": "[테크이노베이션] 서류 합격 안내",
    "body": "안녕하세요, 김철수님.\n\n테크이노베이션의 '백엔드 개발자 채용' 공고에 지원해 주셔서 감사합니다.\n\n서류 전형 결과, 합격하셨음을 알려드립니다.\n\n면접 일정은 다음과 같습니다:\n- 일시: 2024년 1월 20일 14:00\n- 장소: 서울시 강남구 테헤란로 123, 테크이노베이션 본사\n\n감사합니다.",
    "channel": "EMAIL",
    "recipientEmail": "kim.cs@email.com",
    "recipientPhone": "010-1111-0001"
  },
  "timestamp": "2024-01-16T14:30:00Z"
}
```

---

## 8.4 채용 워크플로우

### 상태 전이 규칙

```
APPLIED (지원)
  ↓
DOCUMENT_PASS (서류 합격)
  ↓
INTERVIEW_1 (1차 면접)
  ↓
INTERVIEW_2 (2차 면접)
  ↓
FINAL_PASS (최종 합격)

REJECTED (불합격) ← 어느 단계에서나 가능
```

**제약 조건:**
- 역행 금지 (REJECTED 제외)
- 단계 건너뛰기 불가
- 상태 변경 시 반드시 이력 기록

---

## 8.5 API 보안

### Rate Limiting
```
일반 사용자: 100 requests/minute
인증된 사용자: 1000 requests/minute
```

### CORS 설정
```yaml
allowed-origins:
  - https://ainjob.com
  - https://admin.ainjob.com
allowed-methods:
  - GET
  - POST
  - PUT
  - PATCH
  - DELETE
allowed-headers:
  - Authorization
  - Content-Type
```

### API Key (선택 사항)
```
X-API-Key: {api_key}
```

---

## 8.6 API 문서화

### OpenAPI 3.0 (Swagger)
```yaml
openapi: 3.0.0
info:
  title: AINJOB API
  version: 1.0.0
  description: HR SaaS Platform API
servers:
  - url: https://api.ainjob.com/v1
    description: Production
  - url: https://api-staging.ainjob.com/v1
    description: Staging
```

### 접근 URL
```
Swagger UI: https://api.ainjob.com/swagger-ui.html
OpenAPI JSON: https://api.ainjob.com/v3/api-docs
```
