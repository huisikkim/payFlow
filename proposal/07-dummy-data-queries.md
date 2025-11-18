# 7. 더미 데이터 & 쿼리 결과

## 7.1 데이터 구조 요약

### 기업 (2개)
1. 테크이노베이션 (company_id = 1)
2. 디지털솔루션즈 (company_id = 2)

### 채용 공고 (4개)
1. 테크이노베이션 - 백엔드 개발자 (job_posting_id = 1)
2. 테크이노베이션 - 프런트엔드 개발자 (job_posting_id = 2)
3. 디지털솔루션즈 - 백엔드 개발자 (job_posting_id = 3)
4. 디지털솔루션즈 - 프런트엔드 개발자 (job_posting_id = 4)

### 지원자 (25명)
- **1-12번**: 백엔드 지원자 (기업 1 백엔드 공고에 지원)
- **13-18번**: 프런트엔드 지원자 (기업 1 프런트엔드 공고에 지원)
- **19-25번**: 프런트엔드 지원자 (기업 2 프런트엔드 공고에 지원)

---

## 7.2 백엔드 공고 자격 요건

### 필수 조건 (4개 필터)
1. **학력**: 학사 이상
2. **전공**: 컴퓨터공학 OR 소프트웨어공학 OR 정보보안
3. **스킬**: Java AND Spring Boot AND AWS (모두 보유)
4. **경력**: 5년 이상

---

## 7.3 백엔드 지원자 분석 (1-12번)

### 합격자 (6명)

| ID | 이름 | 학력 | 전공 | 경력(년) | 스킬 | 합격 여부 |
|----|------|------|------|---------|------|----------|
| 1 | 김철수 | 학사 | 컴퓨터공학 | 11 | Java, Spring Boot, AWS, Kubernetes | ✅ 합격 |
| 2 | 이영희 | 석사 | 소프트웨어공학 | 11 | Java, Spring Boot, AWS, Docker | ✅ 합격 |
| 3 | 박민수 | 학사 | 정보보안 | 9 | Java, Spring Boot, AWS, PostgreSQL | ✅ 합격 |
| 4 | 정수진 | 학사 | 컴퓨터공학 | 10 | Java, Spring Boot, AWS, Docker | ✅ 합격 |
| 5 | 최동욱 | 학사 | 소프트웨어공학 | 13 | Java, Spring Boot, AWS, Kubernetes | ✅ 합격 |
| 6 | 강민지 | 석사 | 컴퓨터공학 | 8 | Java, Spring Boot, AWS, Docker | ✅ 합격 |

### 불합격자 (6명)

| ID | 이름 | 학력 | 전공 | 경력(년) | 스킬 | 불합격 사유 |
|----|------|------|------|---------|------|------------|
| 7 | 윤서준 | 학사 | 전자공학 | 14 | Python, Django | ❌ 전공 미충족, 스킬 미충족 |
| 8 | 임하은 | 학사 | 컴퓨터공학 | 7 | Java, MySQL | ❌ Spring Boot, AWS 없음 |
| 9 | 한지우 | 학사 | 산업공학 | 11 | MySQL | ❌ 전공 미충족, 스킬 미충족 |
| 10 | 오세훈 | 학사 | 컴퓨터공학 | 16 | Java | ❌ Spring Boot, AWS 없음 |
| 11 | 신예린 | 학사 | 경영학 | 6 | React, TypeScript | ❌ 전공 미충족, 스킬 미충족 |
| 12 | 배준호 | 학사 | 소프트웨어공학 | 9 | Java, Spring Boot, AWS | ❌ 경력 4년 (5년 미만) |

---

## 7.4 프런트엔드 공고 자격 요건

### 필수 조건 (4개 필터)
1. **학력**: 학사 이상
2. **전공**: 컴퓨터공학 OR 소프트웨어공학
3. **스킬**: React AND Next.js AND TypeScript (모두 보유)
4. **경력**: 3년 이상

---

## 7.5 프런트엔드 지원자 분석 (13-25번)

### 합격자 (6명) - 기업 1

| ID | 이름 | 학력 | 전공 | 경력(년) | 스킬 | 합격 여부 |
|----|------|------|------|---------|------|----------|
| 13 | 서지훈 | 학사 | 컴퓨터공학 | 9 | React, Next.js, TypeScript | ✅ 합격 |
| 14 | 권나영 | 학사 | 소프트웨어공학 | 11 | React, Next.js, TypeScript | ✅ 합격 |
| 15 | 조민석 | 석사 | 컴퓨터공학 | 8 | React, Next.js, TypeScript | ✅ 합격 |
| 16 | 황수빈 | 학사 | 소프트웨어공학 | 13 | React, Next.js, TypeScript | ✅ 합격 |
| 17 | 송태양 | 학사 | 컴퓨터공학 | 8 | React, Next.js, TypeScript | ✅ 합격 |
| 18 | 안유진 | 학사 | 소프트웨어공학 | 14 | React, Next.js, TypeScript | ✅ 합격 |

### 불합격자 (7명) - 기업 2

| ID | 이름 | 학력 | 전공 | 경력(년) | 스킬 | 불합격 사유 |
|----|------|------|------|---------|------|------------|
| 19 | 장현우 | 학사 | 전자공학 | 6 | TypeScript | ❌ 전공 미충족, React/Next.js 없음 |
| 20 | 노서연 | 학사 | 컴퓨터공학 | 4 | React | ❌ Next.js, TypeScript 없음 |
| 21 | 문재현 | 학사 | 산업공학 | 15 | 없음 | ❌ 전공 미충족, 스킬 미충족 |
| 22 | 홍다은 | 학사 | 소프트웨어공학 | 11 | TypeScript | ❌ React, Next.js 없음 |
| 23 | 유준서 | 학사 | 경영학 | 4 | React, TypeScript | ❌ 전공 미충족, Next.js 없음 |
| 24 | 전소희 | 학사 | 컴퓨터공학 | 11 | 없음 | ❌ 스킬 미충족 (jQuery 사용) |
| 25 | 고민재 | 학사 | 소프트웨어공학 | 13 | React | ❌ Next.js, TypeScript 없음 |

---

## 7.6 출력물 1: 기업 회원

```sql
SELECT * FROM company;
```

| id | name | business_number | industry | address |
|----|------|----------------|----------|---------|
| 1 | 테크이노베이션 | 123-45-67890 | IT서비스 | 서울시 강남구 테헤란로 123 |
| 2 | 디지털솔루션즈 | 987-65-43210 | IT서비스 | 서울시 서초구 서초대로 456 |

---

## 7.7 출력물 2: 구인 공고

```sql
SELECT 
    jp.id,
    c.name AS company_name,
    jp.title,
    jp.position,
    jp.min_education_level,
    jp.accepted_majors,
    jp.min_years_of_experience,
    STRING_AGG(s.name, ', ') AS required_skills
FROM job_posting jp
INNER JOIN company c ON jp.company_id = c.id
LEFT JOIN job_posting_skill jps ON jp.id = jps.job_posting_id
LEFT JOIN skill s ON jps.skill_id = s.id
WHERE jps.is_required = TRUE
GROUP BY jp.id, c.name, jp.title, jp.position, jp.min_education_level, jp.accepted_majors, jp.min_years_of_experience;
```

| id | company_name | title | position | min_education_level | accepted_majors | min_years | required_skills |
|----|--------------|-------|----------|---------------------|-----------------|-----------|-----------------|
| 1 | 테크이노베이션 | 백엔드 개발자 채용 | BACKEND | BACHELOR | ["컴퓨터공학", "소프트웨어공학", "정보보안"] | 5 | Java, Spring Boot, AWS |
| 2 | 테크이노베이션 | 프런트엔드 개발자 채용 | FRONTEND | BACHELOR | ["컴퓨터공학", "소프트웨어공학"] | 3 | React, Next.js, TypeScript |
| 3 | 디지털솔루션즈 | 백엔드 개발자 채용 | BACKEND | BACHELOR | ["컴퓨터공학", "소프트웨어공학", "정보보안"] | 5 | Java, Spring Boot, AWS |
| 4 | 디지털솔루션즈 | 프런트엔드 개발자 채용 | FRONTEND | BACHELOR | ["컴퓨터공학", "소프트웨어공학"] | 3 | React, Next.js, TypeScript |

---

## 7.8 출력물 3: Applicant Info (샘플)

```sql
SELECT 
    a.id,
    a.name,
    a.email,
    a.phone,
    e.education_level,
    m.name AS major_name,
    SUM(EXTRACT(YEAR FROM AGE(COALESCE(c.end_date, CURRENT_DATE), c.start_date))) AS total_experience
FROM applicant a
LEFT JOIN education e ON a.id = e.applicant_id
LEFT JOIN major m ON e.major_id = m.id
LEFT JOIN career c ON a.id = c.applicant_id
GROUP BY a.id, a.name, a.email, a.phone, e.education_level, m.name
ORDER BY a.id
LIMIT 5;
```

| id | name | email | phone | education_level | major_name | total_experience |
|----|------|-------|-------|-----------------|------------|------------------|
| 1 | 김철수 | kim.cs@email.com | 010-1111-0001 | BACHELOR | 컴퓨터공학 | 11 |
| 2 | 이영희 | lee.yh@email.com | 010-1111-0002 | MASTER | 소프트웨어공학 | 11 |
| 3 | 박민수 | park.ms@email.com | 010-1111-0003 | BACHELOR | 정보보안 | 9 |
| 4 | 정수진 | jung.sj@email.com | 010-1111-0004 | BACHELOR | 컴퓨터공학 | 10 |
| 5 | 최동욱 | choi.dw@email.com | 010-1111-0005 | BACHELOR | 소프트웨어공학 | 13 |

---

## 7.9 출력물 4: Applicant Career 테이블 (샘플)

```sql
SELECT 
    c.id,
    c.applicant_id,
    a.name AS applicant_name,
    c.company_name,
    c.position,
    c.start_date,
    c.end_date,
    EXTRACT(YEAR FROM AGE(COALESCE(c.end_date, CURRENT_DATE), c.start_date)) AS years
FROM career c
INNER JOIN applicant a ON c.applicant_id = a.id
WHERE c.applicant_id <= 3
ORDER BY c.applicant_id, c.start_date;
```

| id | applicant_id | applicant_name | company_name | position | start_date | end_date | years |
|----|--------------|----------------|--------------|----------|------------|----------|-------|
| 1 | 1 | 김철수 | 네이버 | Backend Developer | 2012-03-01 | 2017-12-31 | 5 |
| 2 | 1 | 김철수 | 카카오 | Senior Backend Developer | 2018-01-01 | NULL | 6 |
| 3 | 2 | 이영희 | 삼성전자 | Software Engineer | 2013-03-01 | 2019-06-30 | 6 |
| 4 | 2 | 이영희 | 쿠팡 | Backend Lead | 2019-07-01 | NULL | 5 |
| 5 | 3 | 박민수 | SK텔레콤 | Backend Developer | 2014-03-01 | 2020-02-29 | 5 |
| 6 | 3 | 박민수 | 토스 | Backend Engineer | 2020-03-01 | NULL | 4 |

---

## 7.10 출력물 5: Applicant Career Skill 테이블 (행별 분리)

```sql
SELECT 
    cs.id,
    cs.career_id,
    c.applicant_id,
    a.name AS applicant_name,
    c.company_name,
    s.name AS skill_name,
    cs.proficiency_level
FROM career_skill cs
INNER JOIN career c ON cs.career_id = c.id
INNER JOIN applicant a ON c.applicant_id = a.id
INNER JOIN skill s ON cs.skill_id = s.id
WHERE c.applicant_id = 1
ORDER BY cs.career_id, s.name;
```

| id | career_id | applicant_id | applicant_name | company_name | skill_name | proficiency_level |
|----|-----------|--------------|----------------|--------------|------------|-------------------|
| 1 | 1 | 1 | 김철수 | 네이버 | Java | 5 |
| 2 | 1 | 1 | 김철수 | 네이버 | MySQL | 4 |
| 3 | 1 | 1 | 김철수 | 네이버 | Spring Boot | 4 |
| 4 | 2 | 1 | 김철수 | 카카오 | AWS | 5 |
| 5 | 2 | 1 | 김철수 | 카카오 | Java | 5 |
| 6 | 2 | 1 | 김철수 | 카카오 | Kubernetes | 4 |
| 7 | 2 | 1 | 김철수 | 카카오 | Spring Boot | 5 |

---

## 7.11 출력물 6: Applicant Education, 전공 테이블

```sql
SELECT 
    e.id,
    e.applicant_id,
    a.name AS applicant_name,
    e.education_level,
    m.name AS major_name,
    e.school_name,
    e.start_date,
    e.end_date,
    e.status
FROM education e
INNER JOIN applicant a ON e.applicant_id = a.id
INNER JOIN major m ON e.major_id = m.id
WHERE e.applicant_id <= 5
ORDER BY e.applicant_id;
```

| id | applicant_id | applicant_name | education_level | major_name | school_name | start_date | end_date | status |
|----|--------------|----------------|-----------------|------------|-------------|------------|----------|--------|
| 1 | 1 | 김철수 | BACHELOR | 컴퓨터공학 | 서울대학교 | 2008-03-01 | 2012-02-28 | GRADUATED |
| 2 | 2 | 이영희 | MASTER | 소프트웨어공학 | KAIST | 2006-03-01 | 2013-02-28 | GRADUATED |
| 3 | 3 | 박민수 | BACHELOR | 정보보안 | 연세대학교 | 2010-03-01 | 2014-02-28 | GRADUATED |
| 4 | 4 | 정수진 | BACHELOR | 컴퓨터공학 | 고려대학교 | 2009-03-01 | 2013-02-28 | GRADUATED |
| 5 | 5 | 최동욱 | BACHELOR | 소프트웨어공학 | 성균관대학교 | 2007-03-01 | 2011-02-28 | GRADUATED |

---

## 7.12 출력물 7: Application Tracking Table

```sql
SELECT 
    at.id,
    at.applicant_id,
    a.name AS applicant_name,
    at.job_posting_id,
    jp.title AS job_title,
    at.status,
    at.applied_at
FROM application_tracking at
INNER JOIN applicant a ON at.applicant_id = a.id
INNER JOIN job_posting jp ON at.job_posting_id = jp.id
WHERE at.job_posting_id = 1
ORDER BY at.applied_at
LIMIT 5;
```

| id | applicant_id | applicant_name | job_posting_id | job_title | status | applied_at |
|----|--------------|----------------|----------------|-----------|--------|------------|
| 1 | 1 | 김철수 | 1 | 백엔드 개발자 채용 | APPLIED | 2024-01-15 09:00:00 |
| 2 | 2 | 이영희 | 1 | 백엔드 개발자 채용 | APPLIED | 2024-01-15 10:30:00 |
| 3 | 3 | 박민수 | 1 | 백엔드 개발자 채용 | APPLIED | 2024-01-16 11:00:00 |
| 4 | 4 | 정수진 | 1 | 백엔드 개발자 채용 | APPLIED | 2024-01-16 14:20:00 |
| 5 | 5 | 최동욱 | 1 | 백엔드 개발자 채용 | APPLIED | 2024-01-17 09:45:00 |

---

## 7.13 출력물 8: Resume Tracking Table

```sql
SELECT 
    rt.id,
    rt.applicant_id,
    a.name AS applicant_name,
    rt.title,
    rt.file_name,
    rt.file_size,
    rt.version,
    rt.is_active,
    rt.uploaded_at
FROM resume_tracking rt
INNER JOIN applicant a ON rt.applicant_id = a.id
WHERE rt.applicant_id <= 5
ORDER BY rt.applicant_id;
```

| id | applicant_id | applicant_name | title | file_name | file_size | version | is_active | uploaded_at |
|----|--------------|----------------|-------|-----------|-----------|---------|-----------|-------------|
| 1 | 1 | 김철수 | 김철수 이력서 | 김철수_이력서.pdf | 524288 | 1 | TRUE | 2024-01-10 15:30:00 |
| 2 | 2 | 이영희 | 이영희 이력서 | 이영희_이력서.pdf | 612352 | 1 | TRUE | 2024-01-10 15:30:00 |
| 3 | 3 | 박민수 | 박민수 이력서 | 박민수_이력서.pdf | 498765 | 1 | TRUE | 2024-01-10 15:30:00 |
| 4 | 4 | 정수진 | 정수진 이력서 | 정수진_이력서.pdf | 556789 | 1 | TRUE | 2024-01-10 15:30:00 |
| 5 | 5 | 최동욱 | 최동욱 이력서 | 최동욱_이력서.pdf | 587234 | 1 | TRUE | 2024-01-10 15:30:00 |

---

## 7.14 출력물 9: 백엔드 개발자 합격자 (기업 1, 2)

**SQL 쿼리는 `proposal/sql/06-qualified-applicants-query.sql` 참조**

### 기업 1 백엔드 합격자 (6명)

| 지원자ID | 지원자명 | 학위 | 전공명 | 스킬명 | 경력명칭 | 경력기간(년) |
|---------|---------|------|--------|--------|----------|-------------|
| 1 | 김철수 | BACHELOR | 컴퓨터공학 | AWS, Java, Kubernetes, MySQL, Spring Boot | 네이버 (Backend Developer), 카카오 (Senior Backend Developer) | 11 |
| 2 | 이영희 | MASTER | 소프트웨어공학 | AWS, Docker, Java, PostgreSQL, Spring Boot | 삼성전자 (Software Engineer), 쿠팡 (Backend Lead) | 11 |
| 3 | 박민수 | BACHELOR | 정보보안 | AWS, Java, PostgreSQL, Spring Boot | SK텔레콤 (Backend Developer), 토스 (Backend Engineer) | 9 |
| 4 | 정수진 | BACHELOR | 컴퓨터공학 | AWS, Docker, Java, MySQL, Spring Boot | LG전자 (Software Developer), 배달의민족 (Backend Developer) | 10 |
| 5 | 최동욱 | BACHELOR | 소프트웨어공학 | AWS, Java, Kubernetes, Spring Boot | 현대자동차 (Backend Engineer), 라인 (Senior Backend Developer) | 13 |
| 6 | 강민지 | MASTER | 컴퓨터공학 | AWS, Docker, Java, Spring Boot | NHN (Backend Developer), 넥슨 (Backend Lead) | 8 |

### 기업 2 백엔드 합격자

**결과 없음** (기업 2 백엔드 공고에는 백엔드 지원자가 지원하지 않음)

---

## 7.15 출력물 10: 프런트엔드 개발자 합격자 (기업 1, 2)

### 기업 1 프런트엔드 합격자 (6명)

| 지원자ID | 지원자명 | 학위 | 전공명 | 스킬명 | 경력명칭 | 경력기간(년) |
|---------|---------|------|--------|--------|----------|-------------|
| 13 | 서지훈 | BACHELOR | 컴퓨터공학 | Next.js, React, TypeScript | 네이버 (Frontend Developer), 카카오 (Senior Frontend Developer) | 9 |
| 14 | 권나영 | BACHELOR | 소프트웨어공학 | Next.js, React, TypeScript | 삼성전자 (UI Developer), 쿠팡 (Frontend Lead) | 11 |
| 15 | 조민석 | MASTER | 컴퓨터공학 | Next.js, React, TypeScript | SK텔레콤 (Frontend Developer), 토스 (Frontend Engineer) | 8 |
| 16 | 황수빈 | BACHELOR | 소프트웨어공학 | Next.js, React, TypeScript | LG전자 (Web Developer), 배달의민족 (Frontend Developer) | 13 |
| 17 | 송태양 | BACHELOR | 컴퓨터공학 | Next.js, React, TypeScript | 현대자동차 (Frontend Engineer), 라인 (Frontend Developer) | 8 |
| 18 | 안유진 | BACHELOR | 소프트웨어공학 | Next.js, React, TypeScript | NHN (Frontend Developer), 넥슨 (Senior Frontend Developer) | 14 |

### 기업 2 프런트엔드 합격자

**결과 없음** (기업 2 프런트엔드 공고 지원자 19-25번은 모두 조건 미충족)

---

## 7.16 SQL 실행 순서

```bash
# 1. 테이블 생성
psql -U postgres -d ainjob -f proposal/sql/01-create-tables.sql

# 2. 기본 데이터 삽입 (기업, 스킬, 전공, 공고)
psql -U postgres -d ainjob -f proposal/sql/02-insert-dummy-data.sql

# 3. 학력 및 경력 데이터 삽입
psql -U postgres -d ainjob -f proposal/sql/03-insert-education-career.sql

# 4. 경력별 스킬 데이터 삽입
psql -U postgres -d ainjob -f proposal/sql/04-insert-career-skills.sql

# 5. 이력서 및 지원 데이터 삽입
psql -U postgres -d ainjob -f proposal/sql/05-insert-resume-application.sql

# 6. 합격자 조회 쿼리 실행
psql -U postgres -d ainjob -f proposal/sql/06-qualified-applicants-query.sql
```

---

## 7.17 데이터 검증

### 총 레코드 수

```sql
SELECT 
    'company' AS table_name, COUNT(*) AS count FROM company
UNION ALL
SELECT 'job_posting', COUNT(*) FROM job_posting
UNION ALL
SELECT 'skill', COUNT(*) FROM skill
UNION ALL
SELECT 'applicant', COUNT(*) FROM applicant
UNION ALL
SELECT 'education', COUNT(*) FROM education
UNION ALL
SELECT 'career', COUNT(*) FROM career
UNION ALL
SELECT 'career_skill', COUNT(*) FROM career_skill
UNION ALL
SELECT 'resume_tracking', COUNT(*) FROM resume_tracking
UNION ALL
SELECT 'application_tracking', COUNT(*) FROM application_tracking;
```

| table_name | count |
|------------|-------|
| company | 2 |
| job_posting | 4 |
| skill | 12 |
| applicant | 25 |
| education | 25 |
| career | 46 |
| career_skill | 100+ |
| resume_tracking | 25 |
| application_tracking | 25 |

---

## 7.18 필터링 로직 검증

### 백엔드 합격자 검증 (지원자 1번)

```sql
-- 1. 학력 확인
SELECT education_level FROM education WHERE applicant_id = 1;
-- 결과: BACHELOR ✅

-- 2. 전공 확인
SELECT m.name FROM education e JOIN major m ON e.major_id = m.id WHERE e.applicant_id = 1;
-- 결과: 컴퓨터공학 ✅

-- 3. 스킬 확인
SELECT DISTINCT s.name 
FROM career c 
JOIN career_skill cs ON c.id = cs.career_id 
JOIN skill s ON cs.skill_id = s.id 
WHERE c.applicant_id = 1 AND s.name IN ('Java', 'Spring Boot', 'AWS');
-- 결과: Java, Spring Boot, AWS ✅ (3개 모두 보유)

-- 4. 경력 확인
SELECT SUM(EXTRACT(YEAR FROM AGE(COALESCE(end_date, CURRENT_DATE), start_date))) 
FROM career WHERE applicant_id = 1;
-- 결과: 11년 ✅ (5년 이상)
```

**결론**: 지원자 1번(김철수)은 모든 조건을 충족하여 합격 ✅
