-- 합격자 조회 쿼리

-- ============================================================
-- 출력물 9: 백엔드 개발자 합격자 (기업 1, 2)
-- ============================================================
-- 자격 요건:
-- 1) 학력: 학사 이상
-- 2) 전공: 컴퓨터공학 OR 소프트웨어공학 OR 정보보안
-- 3) 스킬: Java AND Spring Boot AND AWS (모두 보유)
-- 4) 경력: 5년 이상

-- 기업 1 백엔드 합격자
WITH applicant_skills AS (
    SELECT 
        c.applicant_id,
        STRING_AGG(DISTINCT s.name, ', ' ORDER BY s.name) AS skills
    FROM career c
    INNER JOIN career_skill cs ON c.id = cs.career_id
    INNER JOIN skill s ON cs.skill_id = s.id
    GROUP BY c.applicant_id
),
applicant_experience AS (
    SELECT 
        applicant_id,
        -- 개선: 일 단위 계산 후 년으로 환산
        ROUND(
            SUM(
                COALESCE(end_date, CURRENT_DATE) - start_date
            ) / 365.25, 1
        ) AS total_years
    FROM career
    GROUP BY applicant_id
),
highest_education AS (
    -- 최고 학력만 선택
    SELECT DISTINCT ON (applicant_id)
        applicant_id,
        education_level,
        major_id
    FROM education
    ORDER BY applicant_id, 
             CASE education_level
                 WHEN 'DOCTORATE' THEN 1
                 WHEN 'MASTER' THEN 2
                 WHEN 'BACHELOR' THEN 3
                 ELSE 4
             END
),
required_skills AS (
    -- 필수 스킬 보유자만 필터링
    SELECT c.applicant_id
    FROM career c
    INNER JOIN career_skill cs ON c.id = cs.career_id
    INNER JOIN skill s ON cs.skill_id = s.id
    WHERE s.name IN ('Java', 'Spring Boot', 'AWS')
    GROUP BY c.applicant_id
    HAVING COUNT(DISTINCT s.name) = 3
)
SELECT 
    a.id AS "지원자ID",
    a.name AS "지원자명",
    he.education_level AS "학위",
    m.name AS "전공명",
    ask.skills AS "스킬명",
    STRING_AGG(DISTINCT c.company_name || ' (' || c.position || ')', ', ') AS "경력명칭",
    aexp.total_years AS "경력기간(년)"
FROM application_tracking at
INNER JOIN applicant a ON at.applicant_id = a.id
INNER JOIN highest_education he ON a.id = he.applicant_id  -- 최고학력만
INNER JOIN major m ON he.major_id = m.id
INNER JOIN career c ON a.id = c.applicant_id
INNER JOIN applicant_skills ask ON a.id = ask.applicant_id  -- INNER로 변경
INNER JOIN applicant_experience aexp ON a.id = aexp.applicant_id  -- INNER로 변경
INNER JOIN required_skills rs ON a.id = rs.applicant_id  -- 필수 스킬 조인
WHERE at.job_posting_id = 1
  AND he.education_level IN ('BACHELOR', 'MASTER', 'DOCTORATE')
  AND m.name IN ('컴퓨터공학', '소프트웨어공학', '정보보안')
  AND aexp.total_years >= 5
GROUP BY a.id, a.name, he.education_level, m.name, ask.skills, aexp.total_years
ORDER BY a.id;

-- 기업 2 백엔드 합격자 (동일 조건, job_posting_id = 3)
-- 실제로는 기업 2에 백엔드 지원자가 없으므로 결과 없음
-- 과제 요구사항에 따라 13-25번이 기업 2에 지원했으므로 백엔드 합격자 없음


-- ============================================================
-- 출력물 10: 프런트엔드 개발자 합격자 (기업 1, 2)
-- ============================================================
-- 자격 요건:
-- 1) 학력: 학사 이상
-- 2) 전공: 컴퓨터공학 OR 소프트웨어공학
-- 3) 스킬: React AND Next.js AND TypeScript (모두 보유)
-- 4) 경력: 3년 이상

-- 기업 1 프런트엔드 합격자
WITH applicant_skills AS (
    SELECT 
        c.applicant_id,
        STRING_AGG(DISTINCT s.name, ', ' ORDER BY s.name) AS skills
    FROM career c
    INNER JOIN career_skill cs ON c.id = cs.career_id
    INNER JOIN skill s ON cs.skill_id = s.id
    GROUP BY c.applicant_id
),
applicant_experience AS (
    SELECT 
        applicant_id,
        -- 개선: 일 단위로 정확하게 계산
        ROUND(
            SUM(COALESCE(end_date, CURRENT_DATE) - start_date) / 365.25,
            1
        ) AS total_years
    FROM career
    GROUP BY applicant_id
),
highest_education AS (
    -- 최고 학력만 선택 (중복 방지)
    SELECT DISTINCT ON (applicant_id)
        applicant_id,
        education_level,
        major_id
    FROM education
    ORDER BY applicant_id,
             CASE education_level
                 WHEN 'DOCTORATE' THEN 1
                 WHEN 'MASTER' THEN 2
                 WHEN 'BACHELOR' THEN 3
                 ELSE 4
             END
),
required_skills AS (
    -- 필수 스킬(React, Next.js, TypeScript) 보유자
    SELECT c.applicant_id
    FROM career c
    INNER JOIN career_skill cs ON c.id = cs.career_id
    INNER JOIN skill s ON cs.skill_id = s.id
    WHERE s.name IN ('React', 'Next.js', 'TypeScript')
    GROUP BY c.applicant_id
    HAVING COUNT(DISTINCT s.name) = 3
)
SELECT 
    a.id AS "지원자ID",
    a.name AS "지원자명",
    he.education_level AS "학위",
    m.name AS "전공명",
    ask.skills AS "스킬명",
    STRING_AGG(DISTINCT c.company_name || ' (' || c.position || ')', ', ') AS "경력명칭",
    aexp.total_years AS "경력기간(년)"
FROM application_tracking at
INNER JOIN applicant a ON at.applicant_id = a.id
INNER JOIN highest_education he ON a.id = he.applicant_id  -- 최고학력만
INNER JOIN major m ON he.major_id = m.id
INNER JOIN career c ON a.id = c.applicant_id
INNER JOIN applicant_skills ask ON a.id = ask.applicant_id  -- INNER로 변경
INNER JOIN applicant_experience aexp ON a.id = aexp.applicant_id  -- INNER로 변경
INNER JOIN required_skills rs ON a.id = rs.applicant_id  -- 필수 스킬 체크
WHERE 
    at.job_posting_id = 2  -- 기업 1 프런트엔드 공고
    AND he.education_level IN ('BACHELOR', 'MASTER', 'DOCTORATE')
    AND m.name IN ('컴퓨터공학', '소프트웨어공학')
    AND aexp.total_years >= 3
GROUP BY a.id, a.name, he.education_level, m.name, ask.skills, aexp.total_years
ORDER BY a.id;

-- 기업 2 프런트엔드 합격자 (동일 조건, job_posting_id = 4)
-- 지원자 19-25번 중 조건 충족자 없음 (스킬 미충족 또는 경력 부족)
