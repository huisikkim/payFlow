# AINJOB 구현 요약

## 구현 완료 항목

### 1. Domain Layer (도메인 계층)

#### Aggregate Roots
- **Applicant**: 지원자 정보 관리
- **AinjobJobPosting**: 채용 공고 관리
- **ApplicationTracking**: 지원 추적 및 상태 관리

#### Entities
- **Education**: 학력 정보
- **Career**: 경력 정보
- **CareerSkill**: 경력별 보유 스킬
- **JobPostingSkill**: 공고별 요구 스킬
- **ApplicationStatusHistory**: 지원 상태 변경 이력

#### Value Objects
- **Address**: 주소 정보
- **Qualification**: 채용 공고 자격 요건

#### Reference Data
- **Major**: 전공
- **AinjobSkill**: 기술/스킬

#### Enums
- **EducationLevel**: 학력 수준 (고졸, 학사, 석사, 박사 등)
- **ApplicationStatus**: 지원 상태 (지원, 서류합격, 면접, 최종합격, 불합격)
- **JobPostingStatus**: 공고 상태 (임시저장, 공개, 마감)

### 2. Repository Layer (저장소 계층)

- **ApplicantRepository**: 지원자 조회 (이메일, 상세 정보 포함)
- **AinjobJobPostingRepository**: 채용 공고 조회 (회사별, 상태별, 스킬 포함)
- **ApplicationTrackingRepository**: 지원 내역 조회 (지원자별, 공고별, 상태별, 이력 포함)
- **MajorRepository**: 전공 조회
- **AinjobSkillRepository**: 스킬 조회

### 3. Application Layer (애플리케이션 계층)

#### Services
- **ApplicantService**: 지원자 생성, 조회
- **JobPostingService**: 채용 공고 생성, 조회, 상태 변경
- **ApplicationTrackingService**: 지원 생성, 상태 변경, 조회

#### DTOs
- **ApplicantCreateRequest/Response**: 지원자 생성 요청/응답
- **JobPostingCreateRequest/Response**: 채용 공고 생성 요청/응답
- **ApplicationCreateRequest/Response**: 지원 생성 요청/응답
- **ApplicationStatusChangeRequest**: 지원 상태 변경 요청

### 4. Presentation Layer (프레젠테이션 계층)

#### REST Controllers
- **ApplicantController**: `/api/ainjob/applicants`
  - POST: 지원자 생성
  - GET: 지원자 조회 (전체, ID별, 이메일별)

- **JobPostingController**: `/api/ainjob/job-postings`
  - POST: 채용 공고 생성
  - GET: 채용 공고 조회 (전체, ID별, 회사별, 상태별)
  - PATCH: 공고 오픈/마감

- **ApplicationController**: `/api/ainjob/applications`
  - POST: 지원하기
  - GET: 지원 내역 조회 (ID별, 지원자별, 공고별, 상태별)
  - PATCH: 지원 상태 변경

### 5. 테스트 스크립트

- **test-ainjob-api.sh**: Bash 스크립트
- **test-ainjob-api.ps1**: PowerShell 스크립트

## 주요 비즈니스 로직

### 1. 지원자 생성
- 이메일 중복 체크
- 학력, 경력, 스킬 정보 함께 생성
- Aggregate 내부 일관성 유지

### 2. 채용 공고 생성
- 자격 요건 (학력, 전공, 경력) 설정
- 요구 스킬 목록 설정
- 초기 상태는 DRAFT

### 3. 지원하기
- 공고 지원 가능 여부 확인 (OPEN 상태, 마감일 이전)
- 중복 지원 방지
- 초기 상태는 APPLIED
- 상태 이력 자동 생성

### 4. 지원 상태 변경
- 상태 전이 규칙 검증 (순방향만 허용, REJECTED 제외)
- 상태 이력 자동 기록
- 변경 사유 및 변경자 기록

## 데이터베이스 테이블

총 10개 테이블 생성:
1. `ainjob_applicants`: 지원자
2. `ainjob_educations`: 학력
3. `ainjob_careers`: 경력
4. `ainjob_career_skills`: 경력별 스킬
5. `ainjob_majors`: 전공
6. `ainjob_skills`: 스킬
7. `ainjob_job_postings`: 채용 공고
8. `ainjob_job_posting_skills`: 공고별 요구 스킬
9. `ainjob_application_trackings`: 지원 추적
10. `ainjob_application_status_histories`: 지원 상태 이력

## 설계 원칙

### DDD (Domain-Driven Design)
- Aggregate 단위로 트랜잭션 경계 설정
- Aggregate 간 ID 참조 (직접 객체 참조 금지)
- Value Object 불변성 유지
- 도메인 로직을 엔티티에 캡슐화

### Clean Architecture
- Domain → Application → Presentation 계층 분리
- 의존성 방향: Presentation → Application → Domain
- Repository 인터페이스는 Domain에 정의

### 비즈니스 규칙 강제
- 엔티티 생성자를 protected로 제한
- 정적 팩토리 메서드 (create) 사용
- 상태 변경 메서드에서 비즈니스 규칙 검증

## 기존 코드와의 차이점

### recruitment 모듈과의 구분
- **recruitment**: 간단한 구조, 빠른 프로토타입
- **ainjob**: DDD 기반, 상세한 도메인 모델, proposal 문서 기반

### 테이블 명명 규칙
- 모든 테이블에 `ainjob_` 접두사 사용
- 기존 recruitment 테이블과 충돌 방지

### 클래스 명명 규칙
- `AinjobJobPosting`, `AinjobSkill` 등 접두사 사용
- 기존 `JobPosting`, `Skill` 클래스와 구분

## 향후 확장 가능 항목

### 1. 필터 매칭 시스템
- 학력, 전공, 스킬, 경력 기반 자동 필터링
- 매칭 점수 계산 알고리즘

### 2. Soft Matching
- 온톨로지 기반 유사 스킬 매칭
- 추천 순위 결정

### 3. 이력서 관리
- S3 파일 업로드/다운로드
- 버전 관리
- Resume Aggregate 구현

### 4. 알림 시스템
- 이메일/SMS 알림
- 지원 완료, 상태 변경 알림
- 템플릿 기반 메시지 생성

### 5. 통계 및 리포트
- 공고별 지원자 통계
- 합격률 분석
- 대시보드

### 6. 검색 기능
- 지원자 검색 (이름, 이메일, 스킬)
- 공고 검색 (제목, 포지션, 스킬)
- 페이지네이션 및 정렬

## 빌드 및 실행

### 빌드
```bash
./gradlew clean build
```

### 실행
```bash
./gradlew bootRun
```

### API 테스트
```powershell
# PowerShell
.\test-ainjob-api.ps1

# Bash
./test-ainjob-api.sh
```

## 참고 문서

- [AINJOB API 가이드](./AINJOB_API_GUIDE.md)
- [Proposal 문서](./proposal/README.md)
- [프로젝트 README](./README.md)
