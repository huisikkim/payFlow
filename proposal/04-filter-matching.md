# 4. Filter Matching vs Ontology/Soft Matching

## 4.1 Filter Matching (필수 - 하드 조건)

### 개념
지원자가 **반드시 충족해야 하는** 최소 자격 요건을 검증하는 필터링 시스템

### 필터 조건

| 필터 | 조건 | 예시 |
|------|------|------|
| 학력 | 최소 학력 이상 | 학사 이상 (학사, 석사, 박사 허용) |
| 전공 | 허용 전공 목록 포함 | 컴퓨터공학 OR 소프트웨어공학 OR 정보보안 |
| 스킬 | 요구 스킬 모두 보유 | Java AND Spring Boot AND AWS |
| 경력 | 최소 경력 연차 이상 | 5년 이상 |

### 검증 로직

```java
public class FilterMatchingService {
    
    public boolean isQualified(Applicant applicant, JobPosting jobPosting) {
        Qualification qual = jobPosting.getQualification();
        
        // 1. 학력 검증
        if (!checkEducation(applicant, qual)) {
            return false;
        }
        
        // 2. 전공 검증
        if (!checkMajor(applicant, qual)) {
            return false;
        }
        
        // 3. 스킬 검증
        if (!checkSkills(applicant, jobPosting)) {
            return false;
        }
        
        // 4. 경력 검증
        if (!checkExperience(applicant, qual)) {
            return false;
        }
        
        return true;  // 모든 조건 통과
    }
    
    private boolean checkEducation(Applicant applicant, Qualification qual) {
        EducationLevel maxLevel = applicant.getEducations().stream()
            .map(Education::getLevel)
            .max(Comparator.comparing(EducationLevel::ordinal))
            .orElse(EducationLevel.HIGH_SCHOOL);
        
        return maxLevel.ordinal() >= qual.getMinEducationLevel().ordinal();
    }
    
    private boolean checkMajor(Applicant applicant, Qualification qual) {
        Set<String> applicantMajors = applicant.getEducations().stream()
            .map(edu -> edu.getMajor().getName())
            .collect(Collectors.toSet());
        
        return qual.getAcceptedMajors().stream()
            .anyMatch(applicantMajors::contains);
    }
    
    private boolean checkSkills(Applicant applicant, JobPosting jobPosting) {
        Set<String> applicantSkills = applicant.getCareers().stream()
            .flatMap(career -> career.getSkills().stream())
            .map(cs -> cs.getSkill().getName())
            .collect(Collectors.toSet());
        
        List<String> requiredSkills = jobPosting.getRequiredSkills().stream()
            .filter(JobPostingSkill::getIsRequired)
            .map(jps -> jps.getSkill().getName())
            .collect(Collectors.toList());
        
        return applicantSkills.containsAll(requiredSkills);
    }
    
    private boolean checkExperience(Applicant applicant, Qualification qual) {
        int totalYears = applicant.getCareers().stream()
            .mapToInt(Career::getYearsOfExperience)
            .sum();
        
        return totalYears >= qual.getMinYearsOfExperience();
    }
}
```

### SQL 기반 필터링 (성능 최적화)

```sql
-- 백엔드 개발자 합격자 조회
SELECT DISTINCT
    a.id,
    a.name,
    e.level AS education_level,
    m.name AS major_name,
    SUM(TIMESTAMPDIFF(YEAR, c.start_date, COALESCE(c.end_date, CURDATE()))) AS total_experience
FROM applicant a
-- 학력 조인
INNER JOIN education e ON a.id = e.applicant_id
INNER JOIN major m ON e.major_id = m.id
-- 경력 조인
INNER JOIN career c ON a.id = c.applicant_id
-- 스킬 조인
INNER JOIN career_skill cs ON c.id = cs.career_id
INNER JOIN skill s ON cs.skill_id = s.id
WHERE 
    -- 필터 1: 학력 (학사 이상)
    e.level IN ('BACHELOR', 'MASTER', 'DOCTORATE')
    -- 필터 2: 전공
    AND m.name IN ('컴퓨터공학', '소프트웨어공학', '정보보안')
    -- 필터 3: 스킬 (Java, Spring Boot, AWS 모두 보유)
    AND a.id IN (
        SELECT c2.applicant_id
        FROM career c2
        INNER JOIN career_skill cs2 ON c2.id = cs2.career_id
        INNER JOIN skill s2 ON cs2.skill_id = s2.id
        WHERE s2.name IN ('Java', 'Spring Boot', 'AWS')
        GROUP BY c2.applicant_id
        HAVING COUNT(DISTINCT s2.name) = 3
    )
GROUP BY a.id, a.name, e.level, m.name
-- 필터 4: 경력 (5년 이상)
HAVING total_experience >= 5;
```

---

## 4.2 Ontology/Soft Matching (선택 - 가감점)

### 개념
필터를 통과한 지원자 중에서 **유사 기술, 연관 경험**을 고려하여 매칭 점수를 부여

### 온톨로지 구조

```
Programming Language (프로그래밍 언어)
├─ Java
│  ├─ Spring Boot (depends on Java)
│  ├─ Spring Framework (depends on Java)
│  └─ Kotlin (similar to Java, JVM 기반)
├─ Python
│  ├─ Django (depends on Python)
│  ├─ Flask (depends on Python)
│  └─ FastAPI (depends on Python)
└─ JavaScript
   ├─ Node.js (depends on JavaScript)
   ├─ React (depends on JavaScript)
   └─ TypeScript (similar to JavaScript, 상위 집합)

Cloud Platform (클라우드 플랫폼)
├─ AWS
│  ├─ EC2 (related to AWS)
│  ├─ RDS (related to AWS)
│  └─ S3 (related to AWS)
├─ Azure (similar to AWS)
└─ GCP (similar to AWS)

Database (데이터베이스)
├─ Relational
│  ├─ MySQL (similar to PostgreSQL)
│  ├─ PostgreSQL (similar to MySQL)
│  └─ Oracle (similar to MySQL, PostgreSQL)
└─ NoSQL
   ├─ MongoDB
   ├─ Redis
   └─ Cassandra
```

### 관계 타입

| 관계 | 설명 | 점수 | 예시 |
|------|------|------|------|
| **exact** | 정확히 일치 | 100% | Java ↔ Java |
| **depends_on** | 의존 관계 | 80% | Spring Boot → Java |
| **similar** | 유사 기술 | 60% | MySQL ↔ PostgreSQL |
| **related** | 연관 기술 | 40% | AWS → EC2 |
| **category** | 같은 카테고리 | 20% | Java ↔ Python (둘 다 언어) |

### 잘못된 매칭 방지 규칙

#### ❌ 위험한 매칭 (금지)

```yaml
# 언어 간 자동 매칭 금지
Java ≠ Python
Java ≠ JavaScript
Python ≠ JavaScript

# 프레임워크와 언어 혼동 금지
Spring Boot ≠ Java (Spring Boot는 Java에 의존)
Django ≠ Python (Django는 Python에 의존)

# 클라우드 플랫폼 간 자동 매칭 금지
AWS ≠ Azure
AWS ≠ GCP

# 데이터베이스 타입 간 자동 매칭 금지
MySQL ≠ MongoDB
PostgreSQL ≠ Redis
```

#### ✅ 안전한 매칭 (허용)

```yaml
# 같은 언어 생태계
Java → Kotlin (60% 유사, JVM 기반)
JavaScript → TypeScript (80% 유사, 상위 집합)

# 같은 카테고리 내 유사 기술
MySQL ↔ PostgreSQL (60% 유사, 둘 다 RDBMS)
React ↔ Vue.js (60% 유사, 둘 다 프론트엔드 프레임워크)

# 의존 관계 (역방향 가산점)
Spring Boot 경험 → Java 점수 +80%
Django 경험 → Python 점수 +80%

# 연관 기술
AWS 경험 → EC2, RDS, S3 각각 +40%
```

### 온톨로지 데이터 모델

```java
@Entity
public class SkillOntology {
    @Id
    private Long id;
    
    @ManyToOne
    private Skill sourceSkill;  // 출발 스킬
    
    @ManyToOne
    private Skill targetSkill;  // 도착 스킬
    
    @Enumerated(EnumType.STRING)
    private RelationType relationType;  // EXACT, DEPENDS_ON, SIMILAR, RELATED, CATEGORY
    
    private Integer matchingScore;  // 0-100
    
    private String description;  // 관계 설명
}

public enum RelationType {
    EXACT(100),         // 정확히 일치
    DEPENDS_ON(80),     // 의존 관계
    SIMILAR(60),        // 유사 기술
    RELATED(40),        // 연관 기술
    CATEGORY(20);       // 같은 카테고리
    
    private final int defaultScore;
}
```

### Soft Matching 점수 계산

```java
public class SoftMatchingService {
    
    public int calculateMatchingScore(Applicant applicant, JobPosting jobPosting) {
        List<Skill> requiredSkills = jobPosting.getRequiredSkills().stream()
            .map(JobPostingSkill::getSkill)
            .collect(Collectors.toList());
        
        Set<Skill> applicantSkills = applicant.getCareers().stream()
            .flatMap(career -> career.getSkills().stream())
            .map(CareerSkill::getSkill)
            .collect(Collectors.toSet());
        
        int totalScore = 0;
        int maxScore = requiredSkills.size() * 100;
        
        for (Skill requiredSkill : requiredSkills) {
            int skillScore = calculateSkillScore(requiredSkill, applicantSkills);
            totalScore += skillScore;
        }
        
        return (totalScore * 100) / maxScore;  // 0-100 점수
    }
    
    private int calculateSkillScore(Skill requiredSkill, Set<Skill> applicantSkills) {
        // 1. 정확히 일치
        if (applicantSkills.contains(requiredSkill)) {
            return 100;
        }
        
        // 2. 온톨로지 기반 유사도 계산
        return applicantSkills.stream()
            .mapToInt(applicantSkill -> 
                ontologyRepository.findMatchingScore(requiredSkill, applicantSkill)
            )
            .max()
            .orElse(0);
    }
}
```

---

## 4.3 케이스 분석

### Case 1: Filter 통과 + Soft Matching 낮음

**지원자 A:**
- 학력: 학사 (컴퓨터공학) ✅
- 경력: 6년 ✅
- 스킬: Java, Spring Boot, AWS ✅
- **추가 스킬**: 없음

**공고 요구사항:**
- 학력: 학사 이상
- 전공: 컴퓨터공학
- 경력: 5년 이상
- 스킬: Java, Spring Boot, AWS
- **우대사항**: Kubernetes, Docker, MSA 경험

**결과:**
- Filter Matching: ✅ 통과
- Soft Matching: 60점 (우대사항 없음)
- **판정**: 합격 (최소 자격 충족)

---

### Case 2: Filter 불통과 + Soft Matching 높음

**지원자 B:**
- 학력: 학사 (컴퓨터공학) ✅
- 경력: 6년 ✅
- 스킬: Python, Django, PostgreSQL ❌
- **추가 스킬**: Kubernetes, Docker, MSA 경험 풍부

**공고 요구사항:**
- 학력: 학사 이상
- 전공: 컴퓨터공학
- 경력: 5년 이상
- 스킬: Java, Spring Boot, AWS (필수)

**결과:**
- Filter Matching: ❌ 불통과 (Java, Spring Boot, AWS 없음)
- Soft Matching: 85점 (우대사항 충족)
- **판정**: 불합격 (필수 스킬 미충족)

**이유**: 아무리 우대사항이 좋아도 필수 조건을 충족하지 못하면 불합격

---

### Case 3: Filter 통과 + Soft Matching 높음

**지원자 C:**
- 학력: 석사 (소프트웨어공학) ✅
- 경력: 8년 ✅
- 스킬: Java, Spring Boot, AWS ✅
- **추가 스킬**: Kotlin, Kubernetes, Docker, MSA 경험

**공고 요구사항:**
- 학력: 학사 이상
- 전공: 컴퓨터공학 또는 소프트웨어공학
- 경력: 5년 이상
- 스킬: Java, Spring Boot, AWS
- **우대사항**: Kubernetes, Docker, MSA 경험

**결과:**
- Filter Matching: ✅ 통과
- Soft Matching: 95점 (우대사항 모두 충족)
- **판정**: 최우선 합격 후보

---

## 4.4 온톨로지 로직 개선 제안

### 현재 문제점

```
❌ Java 경험 → Python 점수 자동 가산 (위험)
❌ Spring Boot 경험 → Java와 동등하게 취급 (잘못됨)
❌ AWS 경험 → Azure 점수 자동 가산 (부적절)
```

### 개선된 룰

#### 1. 명시적 관계 정의 (화이트리스트 방식)

```sql
-- 안전한 관계만 등록
INSERT INTO skill_ontology (source_skill_id, target_skill_id, relation_type, matching_score) VALUES
-- Java 생태계
((SELECT id FROM skill WHERE name = 'Spring Boot'), (SELECT id FROM skill WHERE name = 'Java'), 'DEPENDS_ON', 80),
((SELECT id FROM skill WHERE name = 'Kotlin'), (SELECT id FROM skill WHERE name = 'Java'), 'SIMILAR', 60),

-- Python 생태계
((SELECT id FROM skill WHERE name = 'Django'), (SELECT id FROM skill WHERE name = 'Python'), 'DEPENDS_ON', 80),
((SELECT id FROM skill WHERE name = 'Flask'), (SELECT id FROM skill WHERE name = 'Python'), 'DEPENDS_ON', 80),

-- JavaScript 생태계
((SELECT id FROM skill WHERE name = 'TypeScript'), (SELECT id FROM skill WHERE name = 'JavaScript'), 'SIMILAR', 80),
((SELECT id FROM skill WHERE name = 'React'), (SELECT id FROM skill WHERE name = 'JavaScript'), 'DEPENDS_ON', 80),

-- RDBMS (같은 카테고리)
((SELECT id FROM skill WHERE name = 'MySQL'), (SELECT id FROM skill WHERE name = 'PostgreSQL'), 'SIMILAR', 60),
((SELECT id FROM skill WHERE name = 'PostgreSQL'), (SELECT id FROM skill WHERE name = 'MySQL'), 'SIMILAR', 60);
```

#### 2. 블랙리스트 (금지 관계)

```sql
CREATE TABLE skill_ontology_blacklist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    skill1_id BIGINT NOT NULL,
    skill2_id BIGINT NOT NULL,
    reason VARCHAR(255),
    FOREIGN KEY (skill1_id) REFERENCES skill(id),
    FOREIGN KEY (skill2_id) REFERENCES skill(id),
    UNIQUE KEY (skill1_id, skill2_id)
);

-- 금지 관계 등록
INSERT INTO skill_ontology_blacklist (skill1_id, skill2_id, reason) VALUES
((SELECT id FROM skill WHERE name = 'Java'), (SELECT id FROM skill WHERE name = 'Python'), '언어 간 자동 매칭 금지'),
((SELECT id FROM skill WHERE name = 'AWS'), (SELECT id FROM skill WHERE name = 'Azure'), '클라우드 플랫폼 간 자동 매칭 금지'),
((SELECT id FROM skill WHERE name = 'MySQL'), (SELECT id FROM skill WHERE name = 'MongoDB'), 'RDBMS vs NoSQL 자동 매칭 금지');
```

#### 3. 의존 관계 검증

```java
public int calculateSkillScore(Skill requiredSkill, Skill applicantSkill) {
    // 1. 블랙리스트 확인
    if (isBlacklisted(requiredSkill, applicantSkill)) {
        return 0;
    }
    
    // 2. 정확히 일치
    if (requiredSkill.equals(applicantSkill)) {
        return 100;
    }
    
    // 3. 온톨로지 관계 확인
    SkillOntology ontology = ontologyRepository.findRelation(requiredSkill, applicantSkill);
    if (ontology != null) {
        return ontology.getMatchingScore();
    }
    
    // 4. 관계 없음
    return 0;
}
```

#### 4. 의존 관계 역방향 처리

```java
// Spring Boot 경험이 있으면 Java 점수 가산
if (requiredSkill.getName().equals("Java")) {
    boolean hasSpringBoot = applicantSkills.stream()
        .anyMatch(s -> s.getName().equals("Spring Boot"));
    
    if (hasSpringBoot) {
        return 80;  // Spring Boot는 Java에 의존하므로 Java 경험으로 인정 (80%)
    }
}

// 하지만 역방향은 불가
if (requiredSkill.getName().equals("Spring Boot")) {
    boolean hasJava = applicantSkills.stream()
        .anyMatch(s -> s.getName().equals("Java"));
    
    if (hasJava) {
        return 0;  // Java만 있고 Spring Boot 경험이 없으면 0점
    }
}
```

#### 5. 카테고리 기반 최소 점수

```java
// 같은 카테고리 내에서만 최소 점수 부여
if (isSameCategory(requiredSkill, applicantSkill)) {
    return 20;  // 예: Java와 Kotlin (둘 다 JVM 언어)
}

// 다른 카테고리는 0점
return 0;  // 예: Java와 Python (다른 언어)
```

---

## 4.5 온톨로지 관리 전략

### Phase 1: 수동 관리
- 관리자가 직접 관계 등록
- 화이트리스트 방식 (안전)
- 초기 데이터: 주요 기술 스택 50-100개

### Phase 2: 반자동 관리
- HR 담당자가 매칭 결과 피드백
- 잘못된 매칭 발견 시 블랙리스트 추가
- 좋은 매칭 발견 시 온톨로지 추가

### Phase 3: AI 기반 자동 관리 (선택)
- 채용 데이터 분석
- 합격자의 스킬 조합 패턴 학습
- 자동 온톨로지 제안 (관리자 승인 필요)

---

## 4.6 최종 매칭 프로세스

```
1. Filter Matching (필수)
   ↓ (통과)
2. Soft Matching (가산점)
   ↓
3. 최종 점수 = Filter 통과 여부 + Soft Matching 점수
   ↓
4. 정렬: Soft Matching 점수 내림차순
   ↓
5. HR 담당자에게 추천 순위 제공
```

**예시:**
```
지원자 A: Filter ✅ + Soft 95점 → 1순위
지원자 C: Filter ✅ + Soft 85점 → 2순위
지원자 D: Filter ✅ + Soft 70점 → 3순위
지원자 B: Filter ❌ + Soft 90점 → 불합격 (필터 미통과)
```
