# 10. 시퀀스 다이어그램

## 10.1 지원자 목록 조회 및 필터 적용

```mermaid
sequenceDiagram
    participant HR as HR 담당자
    participant Web as Web Client
    participant API as API Server
    participant Cache as Redis Cache
    participant DB as PostgreSQL
    
    HR->>Web: 지원자 목록 페이지 접속
    Web->>API: GET /api/v1/applicants?jobPostingId=1&status=APPLIED
    
    API->>API: JWT 토큰 검증
    API->>API: 권한 확인 (COMPANY_HR)
    
    API->>Cache: 캐시 조회 (key: applicants:1:APPLIED:page:0)
    
    alt 캐시 히트
        Cache-->>API: 캐시된 데이터 반환
        API-->>Web: 200 OK (지원자 목록)
    else 캐시 미스
        Cache-->>API: null
        API->>DB: SELECT applicants with filters
        DB-->>API: 지원자 데이터
        API->>API: DTO 변환
        API->>Cache: 캐시 저장 (TTL: 5분)
        API-->>Web: 200 OK (지원자 목록)
    end
    
    Web-->>HR: 지원자 목록 표시
    
    HR->>Web: 필터 적용 (학력: 석사 이상, 스킬: Java)
    Web->>API: GET /api/v1/applicants?jobPostingId=1&education=MASTER&skills=Java
    
    API->>DB: SELECT with new filters
    DB-->>API: 필터링된 데이터
    API-->>Web: 200 OK (필터링된 목록)
    Web-->>HR: 필터링된 지원자 목록 표시
```

---

## 10.2 지원자 상세 페이지 열기

```mermaid
sequenceDiagram
    participant HR as HR 담당자
    participant Web as Web Client
    participant API as API Server
    participant DB as PostgreSQL
    participant S3 as AWS S3
    
    HR->>Web: 지원자 이름 클릭
    Web->>API: GET /api/v1/applicants/1
    
    API->>API: JWT 토큰 검증
    API->>API: 권한 확인
    
    API->>DB: SELECT applicant with educations, careers, skills
    Note over API,DB: Fetch Join으로 N+1 방지
    DB-->>API: 지원자 상세 데이터
    
    API->>DB: SELECT resume (active)
    DB-->>API: 이력서 메타데이터
    
    API->>S3: Generate Pre-signed URL
    S3-->>API: Pre-signed URL (유효기간: 1시간)
    
    API->>API: DTO 변환 및 조합
    API-->>Web: 200 OK (지원자 상세 정보)
    
    Web-->>HR: 지원자 상세 페이지 표시
    
    HR->>Web: 이력서 다운로드 버튼 클릭
    Web->>S3: GET {pre-signed-url}
    S3-->>Web: 이력서 파일 (PDF)
    Web-->>HR: 파일 다운로드
```

---

## 10.3 지원 상태 변경 및 알림 발송

```mermaid
sequenceDiagram
    participant HR as HR 담당자
    participant Web as Web Client
    participant API as API Server
    participant DB as PostgreSQL
    participant Event as Event Bus
    participant Notif as Notification Service
    participant Email as Email Service (SES)
    
    HR->>Web: 상태 변경 (지원 → 서류합격)
    Web->>API: PATCH /api/v1/applications/1/status
    Note over Web,API: Body: {from: APPLIED, to: DOCUMENT_PASS, reason: "..."}
    
    API->>API: JWT 토큰 검증
    API->>API: 권한 확인
    
    API->>DB: BEGIN TRANSACTION
    
    API->>DB: SELECT application FOR UPDATE
    DB-->>API: Application 데이터
    
    API->>API: 상태 전이 검증
    Note over API: APPLIED → DOCUMENT_PASS (허용)
    
    API->>DB: UPDATE application SET status = 'DOCUMENT_PASS'
    API->>DB: INSERT INTO application_status_history
    
    API->>DB: COMMIT TRANSACTION
    
    API->>Event: Publish ApplicationStatusChangedEvent
    Note over API,Event: 이벤트: {applicationId: 1, from: APPLIED, to: DOCUMENT_PASS}
    
    API-->>Web: 200 OK (상태 변경 완료)
    Web-->>HR: 성공 메시지 표시
    
    Event->>Notif: Handle ApplicationStatusChangedEvent (비동기)
    
    Notif->>DB: SELECT applicant, job_posting
    DB-->>Notif: 지원자 및 공고 정보
    
    Notif->>Notif: 알림 템플릿 렌더링
    Note over Notif: 템플릿: DOCUMENT_PASS<br/>변수: 지원자명, 공고명, 회사명
    
    Notif->>Email: Send Email
    Email-->>Notif: Email Sent (Message ID)
    
    Notif->>DB: INSERT INTO notification_log
    Note over Notif,DB: 발송 이력 저장
```

---

## 10.4 알림 템플릿 미리보기

```mermaid
sequenceDiagram
    participant HR as HR 담당자
    participant Web as Web Client
    participant API as API Server
    participant DB as PostgreSQL
    participant Template as Template Engine
    
    HR->>Web: 상태 변경 화면에서 "알림 미리보기" 클릭
    Web->>API: POST /api/v1/applications/1/notifications/preview
    Note over Web,API: Body: {template: DOCUMENT_PASS, placeholders: {...}}
    
    API->>API: JWT 토큰 검증
    
    API->>DB: SELECT applicant, job_posting, company
    DB-->>API: 관련 데이터
    
    API->>Template: Render Template
    Note over API,Template: 템플릿: DOCUMENT_PASS<br/>변수: 지원자명, 공고명, 회사명, 면접일시
    
    Template-->>API: 렌더링된 이메일 내용
    
    API-->>Web: 200 OK (미리보기 데이터)
    Note over API,Web: {subject: "...", body: "...", recipientEmail: "..."}
    
    Web-->>HR: 알림 미리보기 모달 표시
    
    HR->>Web: "확인" 버튼 클릭
    Web->>API: PATCH /api/v1/applications/1/status
    Note over Web,API: 실제 상태 변경 및 알림 발송
```

---

## 10.5 지원자 지원 프로세스

```mermaid
sequenceDiagram
    participant App as 지원자
    participant Web as Web Client
    participant API as API Server
    participant DB as PostgreSQL
    participant S3 as AWS S3
    participant Event as Event Bus
    participant Notif as Notification Service
    
    App->>Web: 채용 공고 페이지 접속
    Web->>API: GET /api/v1/job-postings/1
    API->>DB: SELECT job_posting
    DB-->>API: 공고 정보
    API-->>Web: 200 OK
    Web-->>App: 공고 상세 표시
    
    App->>Web: "지원하기" 버튼 클릭
    Web-->>App: 지원 폼 표시
    
    App->>Web: 이력서 파일 선택 및 정보 입력
    Web->>API: POST /api/v1/resumes (multipart/form-data)
    
    API->>API: 파일 검증 (크기, 타입)
    API->>S3: Upload File
    S3-->>API: S3 Key
    
    API->>DB: INSERT INTO resume_tracking
    DB-->>API: Resume ID
    
    API-->>Web: 201 Created (Resume ID)
    
    Web->>API: POST /api/v1/applications
    Note over Web,API: Body: {applicantId, jobPostingId, resumeId}
    
    API->>DB: BEGIN TRANSACTION
    
    API->>DB: Check duplicate (applicantId + jobPostingId)
    
    alt 중복 지원
        DB-->>API: 이미 존재
        API-->>Web: 409 Conflict
        Web-->>App: "이미 지원한 공고입니다"
    else 신규 지원
        API->>DB: INSERT INTO application_tracking
        API->>DB: INSERT INTO application_status_history
        API->>DB: COMMIT TRANSACTION
        
        API->>Event: Publish ApplicantAppliedEvent
        
        API-->>Web: 201 Created
        Web-->>App: "지원이 완료되었습니다"
        
        Event->>Notif: Handle ApplicantAppliedEvent (비동기)
        Notif->>Notif: 지원 완료 이메일 발송
        Notif->>Notif: HR 담당자에게 알림
    end
```

---

## 10.6 에러 처리 흐름

```mermaid
sequenceDiagram
    participant HR as HR 담당자
    participant Web as Web Client
    participant API as API Server
    participant DB as PostgreSQL
    
    HR->>Web: 잘못된 상태 변경 시도
    Web->>API: PATCH /api/v1/applications/1/status
    Note over Web,API: Body: {from: DOCUMENT_PASS, to: APPLIED}
    
    API->>API: JWT 토큰 검증 (성공)
    
    API->>DB: SELECT application
    DB-->>API: Application 데이터
    
    API->>API: 상태 전이 검증
    Note over API: DOCUMENT_PASS → APPLIED (역행 금지)
    
    API->>API: throw InvalidStatusTransitionException
    
    API->>API: Global Exception Handler
    Note over API: @ControllerAdvice
    
    API-->>Web: 409 Conflict
    Note over API,Web: {<br/>  "success": false,<br/>  "error": {<br/>    "code": "INVALID_STATUS_TRANSITION",<br/>    "message": "상태 변경이 불가능합니다."<br/>  }<br/>}
    
    Web-->>HR: 에러 메시지 표시
```

---

## 10.7 텍스트 기반 시퀀스 (간단 버전)

### 시나리오 1: 지원자 목록 조회
```
1. HR 담당자가 지원자 목록 페이지 접속
2. Web Client가 API Server에 GET /api/v1/applicants 요청
3. API Server가 JWT 토큰 검증 및 권한 확인
4. Redis Cache 조회
   - 캐시 히트: 캐시된 데이터 반환
   - 캐시 미스: PostgreSQL 조회 → 캐시 저장 → 데이터 반환
5. Web Client가 지원자 목록 표시
```

### 시나리오 2: 상태 변경
```
1. HR 담당자가 상태 변경 (지원 → 서류합격)
2. Web Client가 API Server에 PATCH /api/v1/applications/1/status 요청
3. API Server가 트랜잭션 시작
4. Application 조회 및 상태 전이 검증
5. Application 상태 업데이트 및 이력 기록
6. 트랜잭션 커밋
7. ApplicationStatusChangedEvent 발행 (비동기)
8. Notification Service가 이벤트 처리 및 이메일 발송
```

### 시나리오 3: 알림 미리보기
```
1. HR 담당자가 "알림 미리보기" 클릭
2. Web Client가 API Server에 POST /api/v1/applications/1/notifications/preview 요청
3. API Server가 지원자, 공고, 회사 정보 조회
4. Template Engine이 알림 템플릿 렌더링
5. Web Client가 미리보기 모달 표시
6. HR 담당자가 확인 후 실제 상태 변경 진행
```
