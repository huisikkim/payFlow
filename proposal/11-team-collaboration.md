# 11. 개발 조직 & 협업 프로세스

## 11.1 팀 구성

### 초기 팀 구성 (4-5명)

| 역할 | 인원 | 책임 |
|------|------|------|
| **Backend Developer** | 2-3명 | API 개발, DB 설계, 비즈니스 로직 |
| **Frontend Developer** | 1-2명 | React 웹 개발, UI/UX 구현 |
| **Designer** | 1명 | UI/UX 디자인, 프로토타입 |
| **DevOps** | 겸임 | 인프라 구축, CI/CD (Backend 1명 겸임) |

### 역할별 상세

**Backend Developer 1 (Tech Lead)**
- 아키텍처 설계 및 기술 의사결정
- 핵심 도메인 모델 설계 (Applicant, JobPosting)
- 코드 리뷰 및 기술 멘토링
- DevOps 겸임 (AWS 인프라, CI/CD)

**Backend Developer 2**
- Application Tracking 모듈 개발
- 필터링 및 검색 기능 구현
- API 문서화 (Swagger)

**Backend Developer 3 (선택)**
- Notification 모듈 개발
- 이벤트 시스템 구현
- 배치 작업 (통계, 리포트)

**Frontend Developer 1**
- 지원자 관리 화면 개발
- 공고 관리 화면 개발
- 상태 관리 (Redux/Zustand)

**Frontend Developer 2 (선택)**
- Admin 대시보드 개발
- 통계 및 차트 구현
- 반응형 디자인

**Designer**
- UI/UX 디자인 (Figma)
- 프로토타입 제작
- 디자인 시스템 구축

---

## 11.2 브랜치 전략 (Git Flow)

### 브랜치 구조

```
main (production)
  ↑
develop (integration)
  ↑
feature/* (개발)
  ↑
hotfix/* (긴급 수정)
```

### 브랜치 규칙

**main**
- 프로덕션 배포 브랜치
- 직접 커밋 금지
- develop에서 PR 후 머지
- 태그로 버전 관리 (v1.0.0, v1.1.0)

**develop**
- 개발 통합 브랜치
- feature 브랜치 머지 대상
- 매일 자동 배포 (Staging 환경)

**feature/**
- 기능 개발 브랜치
- 네이밍: `feature/JIRA-123-applicant-list`
- develop에서 분기, develop으로 머지
- 작업 완료 후 삭제

**hotfix/**
- 긴급 버그 수정
- 네이밍: `hotfix/JIRA-456-fix-status-bug`
- main에서 분기, main과 develop에 머지

### 커밋 메시지 규칙

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Type:**
- `feat`: 새로운 기능
- `fix`: 버그 수정
- `docs`: 문서 수정
- `style`: 코드 포맷팅
- `refactor`: 리팩토링
- `test`: 테스트 추가/수정
- `chore`: 빌드, 설정 변경

**예시:**
```
feat(applicant): 지원자 목록 필터링 기능 추가

- 학력, 전공, 경력, 스킬 필터 구현
- 페이지네이션 적용
- 캐싱 전략 적용

Resolves: JIRA-123
```

---

## 11.3 코드 리뷰 방식

### PR (Pull Request) 프로세스

```
1. Feature 브랜치에서 작업 완료
2. GitHub에 PR 생성
3. 자동 CI 실행 (빌드, 테스트)
4. 최소 1명 이상 Approve 필요
5. Tech Lead 최종 승인
6. Squash Merge to develop
```

### PR 템플릿

```markdown
## 변경 사항
- 지원자 목록 필터링 API 구현
- 학력, 전공, 경력, 스킬 필터 추가

## 테스트
- [ ] Unit Test 작성
- [ ] Integration Test 작성
- [ ] 수동 테스트 완료

## 체크리스트
- [ ] 코드 컨벤션 준수
- [ ] API 문서 업데이트
- [ ] 로그 추가
- [ ] 에러 처리 추가

## 관련 이슈
- Resolves: JIRA-123
- Related: JIRA-124

## 스크린샷 (선택)
![image](...)
```

### 코드 리뷰 가이드

**리뷰어 체크리스트:**
- [ ] 비즈니스 로직이 요구사항과 일치하는가?
- [ ] 코드가 읽기 쉽고 이해하기 쉬운가?
- [ ] 테스트 커버리지가 충분한가?
- [ ] 에러 처리가 적절한가?
- [ ] 성능 이슈가 없는가? (N+1, 대용량 조인)
- [ ] 보안 이슈가 없는가? (SQL Injection, XSS)
- [ ] API 문서가 업데이트되었는가?

**리뷰 코멘트 예시:**
```
💡 Suggestion: 이 부분은 Stream API로 간결하게 작성할 수 있습니다.
❓ Question: 이 메서드는 어떤 경우에 null을 반환하나요?
⚠️ Warning: N+1 쿼리 문제가 발생할 수 있습니다. Fetch Join을 고려해보세요.
✅ Approved: LGTM! (Looks Good To Me)
```

---

## 11.4 협업 도구

### Jira (이슈 트래킹)

**프로젝트 구조:**
```
AINJOB
├─ Epic: 지원자 관리
│  ├─ Story: 지원자 목록 조회
│  │  ├─ Task: API 개발
│  │  ├─ Task: 프론트엔드 개발
│  │  └─ Task: 테스트 작성
│  └─ Story: 지원자 상세 조회
├─ Epic: 채용 공고 관리
└─ Epic: 지원 추적
```

**스프린트:**
- 기간: 2주
- 스프린트 계획 회의: 월요일 오전
- 스프린트 리뷰: 격주 금요일 오후
- 회고: 리뷰 직후

**이슈 상태:**
```
TODO → IN PROGRESS → CODE REVIEW → DONE
```

### Notion (문서 관리)

**페이지 구조:**
```
AINJOB Workspace
├─ 📋 프로젝트 개요
├─ 🏗️ 아키텍처
│  ├─ 시스템 아키텍처
│  ├─ ERD
│  └─ API 명세
├─ 📝 회의록
│  ├─ 주간 회의
│  ├─ 스프린트 계획
│  └─ 회고
├─ 📚 개발 가이드
│  ├─ 코딩 컨벤션
│  ├─ Git 가이드
│  └─ 배포 가이드
└─ 🐛 트러블슈팅
```

### Slack (커뮤니케이션)

**채널 구조:**
```
#general          - 전체 공지
#dev-backend      - 백엔드 개발
#dev-frontend     - 프론트엔드 개발
#dev-infra        - 인프라, DevOps
#code-review      - 코드 리뷰 알림 (GitHub 연동)
#ci-cd            - 빌드, 배포 알림
#monitoring       - 모니터링 알림 (CloudWatch)
#random           - 잡담
```

**Slack 봇 연동:**
- GitHub: PR 생성, 머지 알림
- Jira: 이슈 생성, 상태 변경 알림
- AWS: 배포 완료, 알람 알림

### Figma (디자인)

**파일 구조:**
```
AINJOB Design System
├─ Components
│  ├─ Button
│  ├─ Input
│  ├─ Table
│  └─ Modal
├─ Pages
│  ├─ 지원자 목록
│  ├─ 지원자 상세
│  └─ 공고 관리
└─ Prototype
```

---

## 11.5 문서 작성 위치

| 문서 유형 | 도구 | 위치 |
|----------|------|------|
| API 명세 | Swagger | `/swagger-ui.html` |
| ERD | ERDCloud | Notion 링크 |
| 아키텍처 다이어그램 | draw.io | Notion 임베드 |
| 기술 문서 | Notion | `📚 개발 가이드` |
| 회의록 | Notion | `📝 회의록` |
| 코드 주석 | JavaDoc | 소스 코드 |
| README | Markdown | GitHub Repository |

---

## 11.6 개발 프로세스

### Daily Standup (매일 오전 10시, 15분)

**공유 내용:**
1. 어제 한 일
2. 오늘 할 일
3. 블로커 (도움 필요한 사항)

**예시:**
```
[김철수 - Backend]
어제: 지원자 목록 API 개발 완료
오늘: 필터링 로직 구현 및 테스트 작성
블로커: 없음

[이영희 - Frontend]
어제: 지원자 목록 화면 레이아웃 작업
오늘: API 연동 및 필터 UI 구현
블로커: API 응답 스펙 확인 필요 (김철수님)
```

### Sprint Planning (격주 월요일, 2시간)

**진행 순서:**
1. 지난 스프린트 리뷰 (30분)
2. 이번 스프린트 목표 설정 (30분)
3. 백로그 우선순위 정리 (30분)
4. 태스크 분배 및 추정 (30분)

### Sprint Review (격주 금요일, 1시간)

**진행 순서:**
1. 완료된 기능 데모 (30분)
2. 미완료 항목 논의 (15분)
3. 다음 스프린트 계획 (15분)

### Retrospective (리뷰 직후, 1시간)

**KPT 방식:**
- **Keep**: 잘한 점, 계속할 것
- **Problem**: 문제점, 개선 필요
- **Try**: 다음 스프린트에 시도할 것

---

## 11.7 코딩 컨벤션

### Java 코드 스타일

**Google Java Style Guide 기반**

```java
// 클래스명: PascalCase
public class ApplicantService {
    
    // 상수: UPPER_SNAKE_CASE
    private static final int MAX_PAGE_SIZE = 100;
    
    // 변수명: camelCase
    private final ApplicantRepository applicantRepository;
    
    // 메서드명: camelCase (동사로 시작)
    public ApplicantDetailResponse getApplicant(Long id) {
        // ...
    }
}
```

**IntelliJ 설정:**
```xml
<code_scheme name="AINJOB">
  <option name="INDENT_SIZE" value="4" />
  <option name="TAB_SIZE" value="4" />
  <option name="USE_TAB_CHARACTER" value="false" />
  <option name="RIGHT_MARGIN" value="120" />
</code_scheme>
```

### 패키지 구조

```
com.ainjob
├─ applicant
│  ├─ controller
│  ├─ service
│  ├─ repository
│  ├─ domain
│  └─ dto
├─ jobposting
│  ├─ controller
│  ├─ service
│  ├─ repository
│  ├─ domain
│  └─ dto
├─ application
│  ├─ controller
│  ├─ service
│  ├─ repository
│  ├─ domain
│  └─ dto
└─ common
   ├─ config
   ├─ exception
   ├─ security
   └─ util
```

---

## 11.8 테스트 전략

### 테스트 피라미드

```
        /\
       /  \  E2E Tests (10%)
      /____\
     /      \
    / Integration Tests (30%)
   /________\
  /          \
 / Unit Tests (60%)
/______________\
```

### 테스트 작성 규칙

**Unit Test:**
```java
@Test
@DisplayName("지원자 ID로 조회 시 존재하면 지원자 정보를 반환한다")
void getApplicant_WhenExists_ReturnsApplicant() {
    // Given
    Long applicantId = 1L;
    Applicant applicant = new Applicant("김철수", "kim@email.com");
    when(applicantRepository.findById(applicantId))
        .thenReturn(Optional.of(applicant));
    
    // When
    ApplicantDetailResponse response = applicantService.getApplicant(applicantId);
    
    // Then
    assertThat(response.getName()).isEqualTo("김철수");
    assertThat(response.getEmail()).isEqualTo("kim@email.com");
}
```

**Integration Test:**
```java
@SpringBootTest
@AutoConfigureMockMvc
class ApplicantControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void getApplicants_ReturnsApplicantList() throws Exception {
        mockMvc.perform(get("/api/v1/applicants")
                .param("jobPostingId", "1")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.totalElements").value(6));
    }
}
```

### 테스트 커버리지 목표

- **전체**: 80% 이상
- **Service Layer**: 90% 이상
- **Controller Layer**: 70% 이상
- **Repository Layer**: 50% 이상 (간단한 쿼리는 제외)

---

## 11.9 배포 프로세스

### 배포 환경

| 환경 | 브랜치 | 배포 시점 | URL |
|------|--------|----------|-----|
| Development | feature/* | 수동 (로컬) | localhost:8080 |
| Staging | develop | 자동 (매일 자정) | https://api-staging.ainjob.com |
| Production | main | 수동 (승인 후) | https://api.ainjob.com |

### 배포 체크리스트

**배포 전:**
- [ ] 모든 테스트 통과
- [ ] 코드 리뷰 완료
- [ ] API 문서 업데이트
- [ ] DB 마이그레이션 스크립트 준비
- [ ] 롤백 계획 수립

**배포 중:**
- [ ] 배포 시작 공지 (Slack)
- [ ] DB 마이그레이션 실행
- [ ] 애플리케이션 배포
- [ ] Health Check 확인
- [ ] Smoke Test 실행

**배포 후:**
- [ ] 모니터링 확인 (5분)
- [ ] 에러 로그 확인
- [ ] 배포 완료 공지 (Slack)
- [ ] 배포 이력 기록 (Notion)

---

## 11.10 온보딩 프로세스

### 신규 팀원 온보딩 (1주)

**Day 1:**
- 팀 소개 및 프로젝트 개요
- 개발 환경 설정 (IDE, Git, Docker)
- Slack, Jira, Notion 계정 생성

**Day 2-3:**
- 아키텍처 문서 리뷰
- ERD 및 도메인 모델 학습
- 코드베이스 탐색

**Day 4-5:**
- 간단한 버그 수정 또는 테스트 작성
- 첫 PR 생성 및 코드 리뷰 경험
- 팀 프로세스 익히기

**Week 2+:**
- 실제 기능 개발 시작
- 페어 프로그래밍 (선택)
- 정기 1:1 미팅 (Tech Lead)
