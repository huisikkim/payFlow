# 3. 도메인 모델 & Aggregate 설계 (DDD 관점)

## 3.1 필수 Aggregate

### Aggregate 1: Applicant (지원자)

**Aggregate Root**: `Applicant`

```java
@Entity
public class Applicant {
    @Id
    private Long id;
    private String name;
    private String email;
    private String phone;
    private LocalDate birthDate;
    
    // Aggregate 내부 엔티티
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Education> educations;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Career> careers;
    
    // Value Object
    @Embedded
    private Address address;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**내부 엔티티:**

```java
@Entity
public class Education {
    @Id
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Applicant applicant;  // 부모 참조
    
    @Enumerated(EnumType.STRING)
    private EducationLevel level;  // HIGH_SCHOOL, ASSOCIATE, BACHELOR, MASTER, DOCTORATE
    
    @ManyToOne
    private Major major;
    
    private String schoolName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;  // GRADUATED, IN_PROGRESS, LEAVE
}

@Entity
public class Career {
    @Id
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Applicant applicant;
    
    private String companyName;
    private String position;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Career 내부의 스킬 (Value Object Collection)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CareerSkill> skills;
    
    // 경력 기간 계산 (도메인 로직)
    public int getYearsOfExperience() {
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        return Period.between(startDate, end).getYears();
    }
}

@Entity
public class CareerSkill {
    @Id
    private Long id;
    
    @ManyToOne
    private Career career;
    
    @ManyToOne
    private Skill skill;
    
    private Integer proficiencyLevel;  // 1-5
}
```

**Value Object:**

```java
@Embeddable
public class Address {
    private String city;
    private String district;
    private String detail;
    
    // Value Object는 불변
    public Address(String city, String district, String detail) {
        this.city = city;
        this.district = district;
        this.detail = detail;
    }
}
```

**Invariant (불변 조건):**
1. 지원자는 최소 1개 이상의 학력을 가져야 함
2. 이메일은 중복 불가 (Unique)
3. 경력의 종료일은 시작일보다 이후여야 함
4. 학력 레벨은 정의된 Enum 값만 허용

---

### Aggregate 2: Resume (이력서)

**설계 결정: 별도 Aggregate로 분리**

**이유:**
- ✅ 이력서는 버전 관리 필요 (이력 추적)
- ✅ 지원자 정보 변경과 이력서 제출은 별도 트랜잭션
- ✅ 이력서 파일은 S3에 저장 (대용량)
- ✅ 한 지원자가 여러 버전의 이력서 보유 가능

```java
@Entity
public class Resume {
    @Id
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Applicant applicant;  // 외부 Aggregate 참조 (ID만)
    
    private String title;
    private String s3Key;  // S3 파일 경로
    private String fileName;
    private Long fileSize;
    private String contentType;
    
    private Integer version;  // 버전 번호
    private Boolean isActive;  // 현재 활성 이력서
    
    private LocalDateTime uploadedAt;
}
```

**Invariant:**
1. 한 지원자는 최대 1개의 활성 이력서만 가능
2. 이력서 파일은 PDF, DOCX만 허용
3. 파일 크기는 10MB 이하

---

### Aggregate 3: JobPosting (채용 공고)

**Aggregate Root**: `JobPosting`

```java
@Entity
public class JobPosting {
    @Id
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Company company;
    
    private String title;
    private String description;
    private String position;  // BACKEND, FRONTEND, FULLSTACK, etc.
    
    // 자격 요건 (Value Object)
    @Embedded
    private Qualification qualification;
    
    // 요구 스킬
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobPostingSkill> requiredSkills;
    
    @Enumerated(EnumType.STRING)
    private JobPostingStatus status;  // DRAFT, OPEN, CLOSED
    
    private LocalDate openDate;
    private LocalDate closeDate;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 도메인 로직: 공고 마감
    public void close() {
        if (this.status == JobPostingStatus.CLOSED) {
            throw new IllegalStateException("Already closed");
        }
        this.status = JobPostingStatus.CLOSED;
        this.closeDate = LocalDate.now();
    }
    
    // 도메인 로직: 지원 가능 여부
    public boolean isApplicable() {
        return status == JobPostingStatus.OPEN 
            && LocalDate.now().isBefore(closeDate);
    }
}

@Embeddable
public class Qualification {
    @Enumerated(EnumType.STRING)
    private EducationLevel minEducationLevel;  // 최소 학력
    
    @ElementCollection
    private List<String> acceptedMajors;  // 허용 전공 (예: 컴공, 소프트웨어공학)
    
    private Integer minYearsOfExperience;  // 최소 경력 연차
    
    // 자격 요건 검증 로직
    public boolean meetsEducationRequirement(EducationLevel level) {
        return level.ordinal() >= minEducationLevel.ordinal();
    }
}

@Entity
public class JobPostingSkill {
    @Id
    private Long id;
    
    @ManyToOne
    private JobPosting jobPosting;
    
    @ManyToOne
    private Skill skill;
    
    private Boolean isRequired;  // 필수 여부
    private Integer minProficiency;  // 최소 숙련도
}
```

**Invariant:**
1. 공고는 최소 1개 이상의 요구 스킬을 가져야 함
2. 마감일은 오픈일보다 이후여야 함
3. CLOSED 상태에서는 수정 불가
4. 최소 경력 연차는 0 이상

---

### Aggregate 4: ApplicationTracking (지원 추적)

**Aggregate Root**: `ApplicationTracking`

```java
@Entity
public class ApplicationTracking {
    @Id
    private Long id;
    
    // 외부 Aggregate 참조 (ID만)
    @Column(name = "applicant_id")
    private Long applicantId;
    
    @Column(name = "job_posting_id")
    private Long jobPostingId;
    
    @Column(name = "resume_id")
    private Long resumeId;
    
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;  // APPLIED, DOCUMENT_PASS, INTERVIEW_1, INTERVIEW_2, FINAL_PASS, REJECTED
    
    // 상태 변경 이력 (내부 엔티티)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("changedAt DESC")
    private List<ApplicationStatusHistory> statusHistories;
    
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
    
    // 도메인 로직: 상태 변경
    public void changeStatus(ApplicationStatus newStatus, String reason, String changedBy) {
        // 비즈니스 규칙 검증
        validateStatusTransition(this.status, newStatus);
        
        // 이력 추가
        ApplicationStatusHistory history = new ApplicationStatusHistory(
            this.status, newStatus, reason, changedBy, LocalDateTime.now()
        );
        this.statusHistories.add(history);
        
        // 상태 변경
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
        
        // 도메인 이벤트 발행
        DomainEvents.raise(new ApplicationStatusChangedEvent(
            this.id, this.applicantId, this.status, newStatus
        ));
    }
    
    private void validateStatusTransition(ApplicationStatus from, ApplicationStatus to) {
        // 역행 금지 (예외: REJECTED는 어느 단계에서나 가능)
        if (to == ApplicationStatus.REJECTED) {
            return;
        }
        
        if (to.ordinal() < from.ordinal()) {
            throw new IllegalStateException(
                "Cannot move backward from " + from + " to " + to
            );
        }
    }
}

@Entity
public class ApplicationStatusHistory {
    @Id
    private Long id;
    
    @ManyToOne
    private ApplicationTracking application;
    
    @Enumerated(EnumType.STRING)
    private ApplicationStatus fromStatus;
    
    @Enumerated(EnumType.STRING)
    private ApplicationStatus toStatus;
    
    private String reason;
    private String changedBy;  // 변경자 (HR 담당자 ID)
    private LocalDateTime changedAt;
}
```

**Invariant:**
1. 한 지원자는 같은 공고에 1번만 지원 가능 (Unique: applicantId + jobPostingId)
2. 상태는 순방향으로만 변경 (REJECTED 제외)
3. 상태 변경 시 반드시 이력 기록
4. 지원 시 이력서 필수

---

## 3.2 Entity vs Value Object 구분 기준

| 특성 | Entity | Value Object |
|------|--------|--------------|
| 식별자 | 있음 (ID) | 없음 |
| 생명주기 | 독립적 | 부모에 종속 |
| 변경 가능성 | 가변 (Mutable) | 불변 (Immutable) |
| 동등성 비교 | ID 기준 | 속성 값 기준 |
| 예시 | Applicant, Career | Address, Qualification |

**Entity 예시:**
```java
Applicant a1 = new Applicant(1L, "홍길동");
Applicant a2 = new Applicant(1L, "김철수");
a1.equals(a2);  // true (ID가 같으면 동일)
```

**Value Object 예시:**
```java
Address addr1 = new Address("서울", "강남구", "테헤란로 123");
Address addr2 = new Address("서울", "강남구", "테헤란로 123");
addr1.equals(addr2);  // true (모든 속성이 같으면 동일)
```

---

## 3.3 Domain Event

### 이벤트 정의

```java
// 지원 완료
public class ApplicantAppliedEvent {
    private Long applicationId;
    private Long applicantId;
    private Long jobPostingId;
    private LocalDateTime appliedAt;
}

// 상태 변경
public class ApplicationStatusChangedEvent {
    private Long applicationId;
    private Long applicantId;
    private ApplicationStatus fromStatus;
    private ApplicationStatus toStatus;
    private LocalDateTime changedAt;
}

// 공고 마감
public class JobPostingClosedEvent {
    private Long jobPostingId;
    private Long companyId;
    private LocalDateTime closedAt;
    private Integer totalApplicants;
}

// 합격 통보
public class ApplicantPassedEvent {
    private Long applicationId;
    private Long applicantId;
    private ApplicationStatus finalStatus;  // FINAL_PASS
    private LocalDateTime passedAt;
}
```

### 이벤트 발행 위치

| Aggregate | 발행 이벤트 | 발행 시점 |
|-----------|------------|----------|
| ApplicationTracking | ApplicantAppliedEvent | 지원 생성 시 |
| ApplicationTracking | ApplicationStatusChangedEvent | 상태 변경 시 |
| JobPosting | JobPostingClosedEvent | 공고 마감 시 |
| ApplicationTracking | ApplicantPassedEvent | 최종 합격 시 |

### 이벤트 처리 (Event Handler)

```java
@Component
public class ApplicationEventHandler {
    
    @EventListener
    @Async
    public void handleApplicantApplied(ApplicantAppliedEvent event) {
        // 1. 지원 완료 이메일 발송
        notificationService.sendApplicationConfirmation(event.getApplicantId());
        
        // 2. HR 담당자에게 알림
        notificationService.notifyHR(event.getJobPostingId(), "새로운 지원자");
        
        // 3. 통계 업데이트
        statisticsService.incrementApplicationCount(event.getJobPostingId());
    }
    
    @EventListener
    @Async
    public void handleStatusChanged(ApplicationStatusChangedEvent event) {
        // 1. 지원자에게 상태 변경 알림
        notificationService.sendStatusChangeNotification(
            event.getApplicantId(),
            event.getToStatus()
        );
        
        // 2. 합격 시 추가 처리
        if (event.getToStatus() == ApplicationStatus.FINAL_PASS) {
            onboardingService.initiateOnboarding(event.getApplicationId());
        }
    }
}
```

---

## 3.4 트랜잭션 경계

### 강한 일관성 (Strong Consistency) - 단일 트랜잭션

**시나리오 1: 지원자 생성 + 학력/경력 추가**
```java
@Transactional
public void createApplicant(ApplicantCreateRequest request) {
    // 1개 Aggregate 내부 → 단일 트랜잭션
    Applicant applicant = new Applicant(request.getName(), request.getEmail());
    
    request.getEducations().forEach(edu -> 
        applicant.addEducation(new Education(edu))
    );
    
    request.getCareers().forEach(career -> 
        applicant.addCareer(new Career(career))
    );
    
    applicantRepository.save(applicant);
}
```

**시나리오 2: 지원 상태 변경 + 이력 기록**
```java
@Transactional
public void changeApplicationStatus(Long applicationId, ApplicationStatus newStatus) {
    // 1개 Aggregate 내부 → 단일 트랜잭션
    ApplicationTracking application = applicationRepository.findById(applicationId);
    application.changeStatus(newStatus, "서류 검토 완료", "HR-001");
    
    // 상태 변경 + 이력 추가가 원자적으로 처리
}
```

### 최종 일관성 (Eventual Consistency) - 이벤트 기반

**시나리오 3: 지원 완료 → 알림 발송**
```java
@Transactional
public void applyToJob(Long applicantId, Long jobPostingId, Long resumeId) {
    // 1. 지원 생성 (트랜잭션 1)
    ApplicationTracking application = new ApplicationTracking(
        applicantId, jobPostingId, resumeId
    );
    applicationRepository.save(application);
    
    // 2. 이벤트 발행 (트랜잭션 커밋 후)
    eventPublisher.publish(new ApplicantAppliedEvent(application.getId()));
}

// 별도 트랜잭션 (비동기)
@EventListener
@Async
public void sendNotification(ApplicantAppliedEvent event) {
    // 알림 발송 실패해도 지원 데이터는 보존
    emailService.send(...);
}
```

**시나리오 4: 공고 마감 → 통계 업데이트**
```java
@Transactional
public void closeJobPosting(Long jobPostingId) {
    // 1. 공고 마감 (트랜잭션 1)
    JobPosting jobPosting = jobPostingRepository.findById(jobPostingId);
    jobPosting.close();
    
    // 2. 이벤트 발행
    eventPublisher.publish(new JobPostingClosedEvent(jobPostingId));
}

// 별도 트랜잭션 (비동기)
@EventListener
@Async
public void updateStatistics(JobPostingClosedEvent event) {
    // 통계 업데이트는 최종 일관성 허용
    statisticsService.calculateFinalStats(event.getJobPostingId());
}
```

### 트랜잭션 경계 결정 기준

| 작업 | 트랜잭션 전략 | 이유 |
|------|-------------|------|
| 지원자 생성 + 학력/경력 | 단일 트랜잭션 | 데이터 무결성 필수 |
| 상태 변경 + 이력 기록 | 단일 트랜잭션 | 감사 추적 필수 |
| 지원 완료 + 알림 발송 | 최종 일관성 | 알림 실패가 지원을 막으면 안 됨 |
| 공고 마감 + 통계 업데이트 | 최종 일관성 | 통계는 나중에 계산 가능 |
| 합격 처리 + 온보딩 시작 | 최종 일관성 | 온보딩은 별도 프로세스 |

---

## 3.5 Aggregate 간 참조 규칙

### ❌ 잘못된 참조 (직접 객체 참조)

```java
@Entity
public class ApplicationTracking {
    @ManyToOne(fetch = FetchType.EAGER)  // ❌ 다른 Aggregate를 직접 참조
    private Applicant applicant;
    
    @ManyToOne(fetch = FetchType.EAGER)  // ❌
    private JobPosting jobPosting;
}
```

**문제점:**
- 트랜잭션 경계 모호
- N+1 쿼리 문제
- Aggregate 독립성 훼손

### ✅ 올바른 참조 (ID 참조)

```java
@Entity
public class ApplicationTracking {
    @Column(name = "applicant_id")  // ✅ ID만 참조
    private Long applicantId;
    
    @Column(name = "job_posting_id")  // ✅
    private Long jobPostingId;
}
```

**장점:**
- 트랜잭션 경계 명확
- 성능 최적화 용이
- Aggregate 독립성 유지

### 조회 시 데이터 조합

```java
// Service Layer에서 조합
public ApplicationDetailResponse getApplicationDetail(Long applicationId) {
    ApplicationTracking application = applicationRepository.findById(applicationId);
    Applicant applicant = applicantRepository.findById(application.getApplicantId());
    JobPosting jobPosting = jobPostingRepository.findById(application.getJobPostingId());
    
    return ApplicationDetailResponse.of(application, applicant, jobPosting);
}

// 또는 Query용 DTO 사용 (CQRS 패턴)
@Query("SELECT new com.ainjob.dto.ApplicationDetailDTO(...) " +
       "FROM ApplicationTracking a " +
       "JOIN Applicant ap ON a.applicantId = ap.id " +
       "JOIN JobPosting jp ON a.jobPostingId = jp.id " +
       "WHERE a.id = :id")
ApplicationDetailDTO findApplicationDetail(@Param("id") Long id);
```

---

## 3.6 도메인 모델 다이어그램

```
┌─────────────────────────────────────────────────────────────┐
│                    Applicant Aggregate                       │
├─────────────────────────────────────────────────────────────┤
│  Applicant (Root)                                           │
│    ├─ Education (Entity)                                    │
│    │    └─ Major (Reference)                                │
│    ├─ Career (Entity)                                       │
│    │    └─ CareerSkill (Entity)                             │
│    │         └─ Skill (Reference)                           │
│    └─ Address (Value Object)                                │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                     Resume Aggregate                         │
├─────────────────────────────────────────────────────────────┤
│  Resume (Root)                                              │
│    └─ applicantId (외부 참조)                                │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                  JobPosting Aggregate                        │
├─────────────────────────────────────────────────────────────┤
│  JobPosting (Root)                                          │
│    ├─ Qualification (Value Object)                          │
│    ├─ JobPostingSkill (Entity)                              │
│    │    └─ Skill (Reference)                                │
│    └─ companyId (외부 참조)                                  │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│              ApplicationTracking Aggregate                   │
├─────────────────────────────────────────────────────────────┤
│  ApplicationTracking (Root)                                 │
│    ├─ ApplicationStatusHistory (Entity)                     │
│    ├─ applicantId (외부 참조)                                │
│    ├─ jobPostingId (외부 참조)                               │
│    └─ resumeId (외부 참조)                                   │
└─────────────────────────────────────────────────────────────┘
```
