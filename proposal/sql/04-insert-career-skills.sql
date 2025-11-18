-- 경력별 스킬 데이터 (행별로 분리)

-- 백엔드 합격자 스킬 (1-6번: Java, Spring Boot, AWS 모두 보유)
-- 지원자 1 (career_id 1, 2)
INSERT INTO career_skill (career_id, skill_id, proficiency_level) VALUES
(1, (SELECT id FROM skill WHERE name = 'Java'), 5),
(1, (SELECT id FROM skill WHERE name = 'Spring Boot'), 4),
(1, (SELECT id FROM skill WHERE name = 'MySQL'), 4),
(2, (SELECT id FROM skill WHERE name = 'Java'), 5),
(2, (SELECT id FROM skill WHERE name = 'Spring Boot'), 5),
(2, (SELECT id FROM skill WHERE name = 'AWS'), 5),
(2, (SELECT id FROM skill WHERE name = 'Kubernetes'), 4);

-- 지원자 2 (career_id 3, 4)
INSERT INTO career_skill (career_id, skill_id, proficiency_level) VALUES
(3, (SELECT id FROM skill WHERE name = 'Java'), 5),
(3, (SELECT id FROM skill WHERE name = 'Spring Boot'), 4),
(3, (SELECT id FROM skill WHERE name = 'PostgreSQL'), 4),
(4, (SELECT id FROM skill WHERE name = 'Java'), 5),
(4, (SELECT id FROM skill WHERE name = 'Spring Boot'), 5),
(4, (SELECT id FROM skill WHERE name = 'AWS'), 5),
(4, (SELECT id FROM skill WHERE name = 'Docker'), 5);

-- 지원자 3 (career_id 5, 6)
INSERT INTO career_skill (career_id, skill_id, proficiency_level) VALUES
(5, (SELECT id FROM skill WHERE name = 'Java'), 4),
(5, (SELECT id FROM skill WHERE name = 'Spring Boot'), 4),
(5, (SELECT id FROM skill WHERE name = 'AWS'), 3),
(6, (SELECT id FROM skill WHERE name = 'Java'), 5),
(6, (SELECT id FROM skill WHERE name = 'Spring Boot'), 5),
(6, (SELECT id FROM skill WHERE name = 'AWS'), 5),
(6, (SELECT id FROM skill WHERE name = 'PostgreSQL'), 4);

-- 지원자 4 (career_id 7, 8)
INSERT INTO career_skill (career_id, skill_id, proficiency_level) VALUES
(7, (SELECT id FROM skill WHERE name = 'Java'), 4),
(7, (SELECT id FROM skill WHERE name = 'Spring Boot'), 3),
(7, (SELECT id FROM skill WHERE name = 'MySQL'), 3),
(8, (SELECT id FROM skill WHERE name = 'Java'), 5),
(8, (SELECT id FROM skill WHERE name = 'Spring Boot'), 5),
(8, (SELECT id FROM skill WHERE name = 'AWS'), 4),
(8, (SELECT id FROM skill WHERE name = 'Docker'), 4);

-- 지원자 5 (career_id 9, 10)
INSERT INTO career_skill (career_id, skill_id, proficiency_level) VALUES
(9, (SELECT id FROM skill WHERE name = 'Java'), 5),
(9, (SELECT id FROM skill WHERE name = 'Spring Boot'), 4),
(9, (SELECT id FROM skill WHERE name = 'AWS'), 3),
(10, (SELECT id FROM skill WHERE name = 'Java'), 5),
(10, (SELECT id FROM skill WHERE name = 'Spring Boot'), 5),
(10, (SELECT id FROM skill WHERE name = 'AWS'), 5),
(10, (SELECT id FROM skill WHERE name = 'Kubernetes'), 4);

-- 지원자 6 (career_id 11, 12)
INSERT INTO career_skill (career_id, skill_id, proficiency_level) VALUES
(11, (SELECT id FROM skill WHERE name = 'Java'), 5),
(11, (SELECT id FROM skill WHERE name = 'Spring Boot'), 4),
(11, (SELECT id FROM skill WHERE name = 'AWS'), 4),
(12, (SELECT id FROM skill WHERE name = 'Java'), 5),
(12, (SELECT id FROM skill WHERE name = 'Spring Boot'), 5),
(12, (SELECT id FROM skill WHERE name = 'AWS'), 5),
(12, (SELECT id FROM skill WHERE name = 'Docker'), 5);

-- 백엔드 불합격자 스킬 (7-12번: 필수 스킬 미충족)
-- 지원자 7 (Python 개발자)
INSERT INTO career_skill (career_id, skill_id, proficiency_level) VALUES
(13, (SELECT id FROM skill WHERE name = 'Python'), 5),
(13, (SELECT id FROM skill WHERE name = 'Django'), 4),
(14, (SELECT id FROM skill WHERE name = 'Python'), 5),
(14, (SELECT id FROM skill WHERE name = 'Django'), 5),
(14, (SELECT id FROM skill WHERE name = 'PostgreSQL'), 4);

-- 지원자 8 (Java만 있고 Spring Boot, AWS 없음)
INSERT INTO career_skill (career_id, skill_id, proficiency_level) VALUES
(15, (SELECT id FROM skill WHERE name = 'Java'), 3),
(16, (SELECT id FROM skill WHERE name = 'Java'), 4),
(16, (SELECT id FROM skill WHERE name = 'MySQL'), 3);

-- 지원자 9 (ERP 관리자, 개발 스킬 없음)
INSERT INTO career_skill (career_id, skill_id, proficiency_level) VALUES
(17, (SELECT id FROM skill WHERE name = 'MySQL'), 2);

-- 지원자 10 (Legacy 시스템, 최신 기술 없음)
INSERT INTO career_skill (career_id, skill_id, proficiency_level) VALUES
(18, (SELECT id FROM skill WHERE name = 'Java'), 3),
(19, (SELECT id FROM skill WHERE name = 'Java'), 3);

-- 지원자 11 (Node.js 개발자)
INSERT INTO career_skill (career_id, skill_id, proficiency_level) VALUES
(20, (SELECT id FROM skill WHERE name = 'React'), 3),
(21, (SELECT id FROM skill WHERE name = 'React'), 4),
(21, (SELECT id FROM skill WHERE name = 'TypeScript'), 3);

-- 지원자 12 (경력 4년, 5년 미만)
INSERT INTO career_skill (career_id, skill_id, proficiency_level) VALUES
(22, (SELECT id FROM skill WHERE name = 'Java'), 4),
(22, (SELECT id FROM skill WHERE name = 'Spring Boot'), 3),
(23, (SELECT id FROM skill WHERE name = 'Java'), 4),
(23, (SELECT id FROM skill WHERE name = 'Spring Boot'), 4),
(23, (SELECT id FROM skill WHERE name = 'AWS'), 2);

-- 프런트엔드 합격자 스킬 (13-18번: React, Next.js, TypeScript 모두 보유)
-- 지원자 13 (career_id 24, 25)
INSERT INTO career_skill (career_id, skill_id, proficiency_level) VALUES
(24, (SELECT id FROM skill WHERE name = 'React'), 5),
(24, (SELECT id FROM skill WHERE name = 'TypeScript'), 4),
(25, (SELECT id FROM skill WHERE name = 'React'), 5),
(25, (SELECT id FROM skill WHERE name = 'Next.js'), 5),
(25, (SELECT id FROM skill WHERE name = 'TypeScript'), 5);

-- 지원자 14 (career_id 26, 27)
INSERT INTO career_skill (career_id, skill_id, proficiency_level) VALUES
(26, (SELECT id FROM skill WHERE name = 'React'), 4),
(26, (SELECT id FROM skill WHERE name = 'TypeScript'), 5),
(27, (SELECT id FROM skill WHERE name = 'React'), 5),
(27, (SELECT id FROM skill WHERE name = 'Next.js'), 5),
(27, (SELECT id FROM skill WHERE name = 'TypeScript'), 5);

-- 지원자 15 (career_id 28, 29)
INSERT INTO career_skill (career_id, skill_id, proficiency_level) VALUES
(28, (SELECT id FROM skill WHERE name = 'React'), 5),
(28, (SELECT id FROM skill WHERE name = 'TypeScript'), 4),
(29, (SELECT id FROM skill WHERE name = 'React'), 5),
(29, (SELECT id FROM skill WHERE name = 'Next.js'), 5),
(29, (SELECT id FROM skill WHERE name = 'TypeScript'), 5);

-- 지원자 16 (career_id 30, 31)
INSERT INTO career_skill (career_id, skill_id, proficiency_level) VALUES
(31, (SELECT id FROM skill WHERE name = 'React'), 5),
(31, (SELECT id FROM skill WHERE name = 'Next.js'), 4),
(31, (SELECT id FROM skill WHERE name = 'TypeScript'), 5);

-- 지원자 17 (career_id 32, 33)
INSERT INTO career_skill (career_id, skill_id, proficiency_level) VALUES
(33, (SELECT id FROM skill WHERE name = 'React'), 5),
(33, (SELECT id FROM skill WHERE name = 'Next.js'), 5),
(33, (SELECT id FROM skill WHERE name = 'TypeScript'), 5);

-- 지원자 18 (career_id 34, 35)
INSERT INTO career_skill (career_id, skill_id, proficiency_level) VALUES
(34, (SELECT id FROM skill WHERE name = 'React'), 5),
(35, (SELECT id FROM skill WHERE name = 'React'), 5),
(35, (SELECT id FROM skill WHERE name = 'Next.js'), 5),
(35, (SELECT id FROM skill WHERE name = 'TypeScript'), 5);

-- 프런트엔드 불합격자 스킬 (19-25번: 필수 스킬 미충족)
-- 지원자 19 (Vue.js 개발자)
INSERT INTO career_skill (career_id, skill_id, proficiency_level) VALUES
(36, (SELECT id FROM skill WHERE name = 'TypeScript'), 3),
(37, (SELECT id FROM skill WHERE name = 'TypeScript'), 4);

-- 지원자 20 (경력 부족)
INSERT INTO career_skill (career_id, skill_id, proficiency_level) VALUES
(39, (SELECT id FROM skill WHERE name = 'React'), 2);

-- 지원자 21 (디자이너, 개발 스킬 없음)
-- 스킬 없음

-- 지원자 22 (Angular 개발자)
INSERT INTO career_skill (career_id, skill_id, proficiency_level) VALUES
(40, (SELECT id FROM skill WHERE name = 'TypeScript'), 4),
(41, (SELECT id FROM skill WHERE name = 'TypeScript'), 4);

-- 지원자 23 (경력 부족)
INSERT INTO career_skill (career_id, skill_id, proficiency_level) VALUES
(42, (SELECT id FROM skill WHERE name = 'React'), 3),
(42, (SELECT id FROM skill WHERE name = 'TypeScript'), 3),
(43, (SELECT id FROM skill WHERE name = 'React'), 4),
(43, (SELECT id FROM skill WHERE name = 'TypeScript'), 4);

-- 지원자 24 (jQuery 개발자, 최신 기술 없음)
-- 스킬 없음 (jQuery는 skill 테이블에 없음)

-- 지원자 25 (React만 있고 Next.js, TypeScript 없음)
INSERT INTO career_skill (career_id, skill_id, proficiency_level) VALUES
(45, (SELECT id FROM skill WHERE name = 'React'), 3);
