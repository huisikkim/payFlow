-- AINJOB HR SaaS Platform - Dummy Data
-- 기업 2개, 각 2개 공고 (백엔드/프런트), 지원자 25명

-- 1. 기업 데이터
INSERT INTO company (name, business_number, industry, address, phone, email) VALUES
('테크이노베이션', '123-45-67890', 'IT서비스', '서울시 강남구 테헤란로 123', '02-1234-5678', 'hr@techinno.com'),
('디지털솔루션즈', '987-65-43210', 'IT서비스', '서울시 서초구 서초대로 456', '02-9876-5432', 'recruit@digitalsol.com');

-- 2. 스킬 데이터
INSERT INTO skill (name, category) VALUES
('Java', 'LANGUAGE'),
('Spring Boot', 'FRAMEWORK'),
('AWS', 'CLOUD'),
('React', 'FRAMEWORK'),
('Next.js', 'FRAMEWORK'),
('TypeScript', 'LANGUAGE'),
('Python', 'LANGUAGE'),
('Django', 'FRAMEWORK'),
('PostgreSQL', 'DATABASE'),
('MySQL', 'DATABASE'),
('Docker', 'DEVOPS'),
('Kubernetes', 'DEVOPS');

-- 3. 전공 데이터
INSERT INTO major (name, category) VALUES
('컴퓨터공학', '공학'),
('소프트웨어공학', '공학'),
('정보보안', '공학'),
('전자공학', '공학'),
('산업공학', '공학'),
('경영학', '경영');

-- 4. 채용 공고 (기업 1 - 테크이노베이션)
INSERT INTO job_posting (company_id, title, description, position, min_education_level, accepted_majors, min_years_of_experience, status, open_date, close_date) VALUES
(1, '백엔드 개발자 채용', 'Java/Spring Boot 기반 백엔드 개발자를 모집합니다.', 'BACKEND', 'BACHELOR', '["컴퓨터공학", "소프트웨어공학", "정보보안"]', 5, 'OPEN', '2024-01-01', '2024-12-31'),
(1, '프런트엔드 개발자 채용', 'React/Next.js 기반 프런트엔드 개발자를 모집합니다.', 'FRONTEND', 'BACHELOR', '["컴퓨터공학", "소프트웨어공학"]', 3, 'OPEN', '2024-01-01', '2024-12-31');

-- 5. 채용 공고 (기업 2 - 디지털솔루션즈)
INSERT INTO job_posting (company_id, title, description, position, min_education_level, accepted_majors, min_years_of_experience, status, open_date, close_date) VALUES
(2, '백엔드 개발자 채용', 'Java/Spring Boot 기반 백엔드 개발자를 모집합니다.', 'BACKEND', 'BACHELOR', '["컴퓨터공학", "소프트웨어공학", "정보보안"]', 5, 'OPEN', '2024-01-01', '2024-12-31'),
(2, '프런트엔드 개발자 채용', 'React/Next.js 기반 프런트엔드 개발자를 모집합니다.', 'FRONTEND', 'BACHELOR', '["컴퓨터공학", "소프트웨어공학"]', 3, 'OPEN', '2024-01-01', '2024-12-31');

-- 6. 공고별 요구 스킬 (백엔드 공고 1, 3)
INSERT INTO job_posting_skill (job_posting_id, skill_id, is_required) VALUES
(1, (SELECT id FROM skill WHERE name = 'Java'), TRUE),
(1, (SELECT id FROM skill WHERE name = 'Spring Boot'), TRUE),
(1, (SELECT id FROM skill WHERE name = 'AWS'), TRUE),
(3, (SELECT id FROM skill WHERE name = 'Java'), TRUE),
(3, (SELECT id FROM skill WHERE name = 'Spring Boot'), TRUE),
(3, (SELECT id FROM skill WHERE name = 'AWS'), TRUE);

-- 7. 공고별 요구 스킬 (프런트엔드 공고 2, 4)
INSERT INTO job_posting_skill (job_posting_id, skill_id, is_required) VALUES
(2, (SELECT id FROM skill WHERE name = 'React'), TRUE),
(2, (SELECT id FROM skill WHERE name = 'Next.js'), TRUE),
(2, (SELECT id FROM skill WHERE name = 'TypeScript'), TRUE),
(4, (SELECT id FROM skill WHERE name = 'React'), TRUE),
(4, (SELECT id FROM skill WHERE name = 'Next.js'), TRUE),
(4, (SELECT id FROM skill WHERE name = 'TypeScript'), TRUE);

-- 8. 지원자 데이터 (1-12번: 백엔드 지원자, 13-25번: 프런트엔드 지원자)
INSERT INTO applicant (name, email, phone, birth_date, city, district) VALUES
-- 백엔드 지원자 (1-12번)
('김철수', 'kim.cs@email.com', '010-1111-0001', '1990-01-15', '서울', '강남구'),
('이영희', 'lee.yh@email.com', '010-1111-0002', '1988-03-22', '서울', '서초구'),
('박민수', 'park.ms@email.com', '010-1111-0003', '1992-05-10', '서울', '송파구'),
('정수진', 'jung.sj@email.com', '010-1111-0004', '1991-07-18', '서울', '강동구'),
('최동욱', 'choi.dw@email.com', '010-1111-0005', '1989-09-25', '서울', '마포구'),
('강민지', 'kang.mj@email.com', '010-1111-0006', '1993-11-30', '서울', '용산구'),
('윤서준', 'yoon.sj@email.com', '010-1111-0007', '1987-02-14', '서울', '성동구'),
('임하은', 'lim.he@email.com', '010-1111-0008', '1994-04-20', '서울', '광진구'),
('한지우', 'han.jw@email.com', '010-1111-0009', '1990-06-08', '서울', '강남구'),
('오세훈', 'oh.sh@email.com', '010-1111-0010', '1986-08-12', '서울', '서초구'),
('신예린', 'shin.yr@email.com', '010-1111-0011', '1995-10-05', '서울', '송파구'),
('배준호', 'bae.jh@email.com', '010-1111-0012', '1991-12-28', '서울', '강동구'),
-- 프런트엔드 지원자 (13-25번)
('서지훈', 'seo.jh@email.com', '010-2222-0001', '1992-01-10', '서울', '마포구'),
('권나영', 'kwon.ny@email.com', '010-2222-0002', '1990-03-15', '서울', '용산구'),
('조민석', 'jo.ms@email.com', '010-2222-0003', '1993-05-20', '서울', '성동구'),
('황수빈', 'hwang.sb@email.com', '010-2222-0004', '1989-07-25', '서울', '광진구'),
('송태양', 'song.ty@email.com', '010-2222-0005', '1994-09-30', '서울', '강남구'),
('안유진', 'ahn.yj@email.com', '010-2222-0006', '1988-11-05', '서울', '서초구'),
('장현우', 'jang.hw@email.com', '010-2222-0007', '1995-02-12', '서울', '송파구'),
('노서연', 'noh.sy@email.com', '010-2222-0008', '1991-04-18', '서울', '강동구'),
('문재현', 'moon.jh@email.com', '010-2222-0009', '1987-06-22', '서울', '마포구'),
('홍다은', 'hong.de@email.com', '010-2222-0010', '1996-08-28', '서울', '용산구'),
('유준서', 'yoo.js@email.com', '010-2222-0011', '1990-10-15', '서울', '성동구'),
('전소희', 'jeon.sh@email.com', '010-2222-0012', '1992-12-20', '서울', '광진구'),
('고민재', 'ko.mj@email.com', '010-2222-0013', '1989-03-08', '서울', '강남구');
