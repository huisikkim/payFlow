-- 학력 및 경력 데이터

-- 백엔드 지원자 학력 (1-12번)
-- 합격자: 1,2,3,4,5,6 (학사 이상 + 컴공/소공/정보보안 + 5년 이상 + Java/Spring Boot/AWS)
-- 불합격자: 7,8,9,10,11,12 (조건 미충족)

INSERT INTO education (applicant_id, major_id, education_level, school_name, start_date, end_date, status) VALUES
-- 합격자 (1-6)
(1, (SELECT id FROM major WHERE name = '컴퓨터공학'), 'BACHELOR', '서울대학교', '2008-03-01', '2012-02-28', 'GRADUATED'),
(2, (SELECT id FROM major WHERE name = '소프트웨어공학'), 'MASTER', 'KAIST', '2006-03-01', '2013-02-28', 'GRADUATED'),
(3, (SELECT id FROM major WHERE name = '정보보안'), 'BACHELOR', '연세대학교', '2010-03-01', '2014-02-28', 'GRADUATED'),
(4, (SELECT id FROM major WHERE name = '컴퓨터공학'), 'BACHELOR', '고려대학교', '2009-03-01', '2013-02-28', 'GRADUATED'),
(5, (SELECT id FROM major WHERE name = '소프트웨어공학'), 'BACHELOR', '성균관대학교', '2007-03-01', '2011-02-28', 'GRADUATED'),
(6, (SELECT id FROM major WHERE name = '컴퓨터공학'), 'MASTER', '포항공대', '2011-03-01', '2015-02-28', 'GRADUATED'),
-- 불합격자 (7-12)
(7, (SELECT id FROM major WHERE name = '전자공학'), 'BACHELOR', '한양대학교', '2005-03-01', '2009-02-28', 'GRADUATED'),
(8, (SELECT id FROM major WHERE name = '컴퓨터공학'), 'BACHELOR', '중앙대학교', '2012-03-01', '2016-02-28', 'GRADUATED'),
(9, (SELECT id FROM major WHERE name = '산업공학'), 'BACHELOR', '서강대학교', '2008-03-01', '2012-02-28', 'GRADUATED'),
(10, (SELECT id FROM major WHERE name = '컴퓨터공학'), 'BACHELOR', '이화여대', '2004-03-01', '2008-02-28', 'GRADUATED'),
(11, (SELECT id FROM major WHERE name = '경영학'), 'BACHELOR', '건국대학교', '2013-03-01', '2017-02-28', 'GRADUATED'),
(12, (SELECT id FROM major WHERE name = '소프트웨어공학'), 'BACHELOR', '동국대학교', '2009-03-01', '2013-02-28', 'GRADUATED');

-- 프런트엔드 지원자 학력 (13-25번)
-- 합격자: 13,14,15,16,17,18 (학사 이상 + 컴공/소공 + 3년 이상 + React/Next.js/TypeScript)
-- 불합격자: 19,20,21,22,23,24,25 (조건 미충족)

INSERT INTO education (applicant_id, major_id, education_level, school_name, start_date, end_date, status) VALUES
-- 합격자 (13-18)
(13, (SELECT id FROM major WHERE name = '컴퓨터공학'), 'BACHELOR', '서울대학교', '2010-03-01', '2014-02-28', 'GRADUATED'),
(14, (SELECT id FROM major WHERE name = '소프트웨어공학'), 'BACHELOR', 'KAIST', '2008-03-01', '2012-02-28', 'GRADUATED'),
(15, (SELECT id FROM major WHERE name = '컴퓨터공학'), 'MASTER', '연세대학교', '2011-03-01', '2015-02-28', 'GRADUATED'),
(16, (SELECT id FROM major WHERE name = '소프트웨어공학'), 'BACHELOR', '고려대학교', '2007-03-01', '2011-02-28', 'GRADUATED'),
(17, (SELECT id FROM major WHERE name = '컴퓨터공학'), 'BACHELOR', '성균관대학교', '2012-03-01', '2016-02-28', 'GRADUATED'),
(18, (SELECT id FROM major WHERE name = '소프트웨어공학'), 'BACHELOR', '포항공대', '2006-03-01', '2010-02-28', 'GRADUATED'),
-- 불합격자 (19-25)
(19, (SELECT id FROM major WHERE name = '전자공학'), 'BACHELOR', '한양대학교', '2013-03-01', '2017-02-28', 'GRADUATED'),
(20, (SELECT id FROM major WHERE name = '컴퓨터공학'), 'BACHELOR', '중앙대학교', '2015-03-01', '2019-02-28', 'GRADUATED'),
(21, (SELECT id FROM major WHERE name = '산업공학'), 'BACHELOR', '서강대학교', '2005-03-01', '2009-02-28', 'GRADUATED'),
(22, (SELECT id FROM major WHERE name = '소프트웨어공학'), 'BACHELOR', '이화여대', '2009-03-01', '2013-02-28', 'GRADUATED'),
(23, (SELECT id FROM major WHERE name = '경영학'), 'BACHELOR', '건국대학교', '2016-03-01', '2020-02-28', 'GRADUATED'),
(24, (SELECT id FROM major WHERE name = '컴퓨터공학'), 'BACHELOR', '동국대학교', '2008-03-01', '2012-02-28', 'GRADUATED'),
(25, (SELECT id FROM major WHERE name = '소프트웨어공학'), 'BACHELOR', '홍익대학교', '2007-03-01', '2011-02-28', 'GRADUATED');

-- 백엔드 지원자 경력 (1-12번)
INSERT INTO career (applicant_id, company_name, position, description, start_date, end_date) VALUES
-- 합격자 (5년 이상 경력)
(1, '네이버', 'Backend Developer', 'Java/Spring Boot 기반 서비스 개발', '2012-03-01', '2017-12-31'),
(1, '카카오', 'Senior Backend Developer', 'MSA 아키텍처 설계 및 구현', '2018-01-01', NULL),
(2, '삼성전자', 'Software Engineer', 'Enterprise 시스템 개발', '2013-03-01', '2019-06-30'),
(2, '쿠팡', 'Backend Lead', '대규모 트래픽 처리 시스템 설계', '2019-07-01', NULL),
(3, 'SK텔레콤', 'Backend Developer', 'API 서버 개발', '2014-03-01', '2020-02-29'),
(3, '토스', 'Backend Engineer', '금융 시스템 개발', '2020-03-01', NULL),
(4, 'LG전자', 'Software Developer', 'IoT 플랫폼 개발', '2013-03-01', '2018-12-31'),
(4, '배달의민족', 'Backend Developer', '주문 시스템 개발', '2019-01-01', NULL),
(5, '현대자동차', 'Backend Engineer', '차량 관제 시스템 개발', '2011-03-01', '2017-06-30'),
(5, '라인', 'Senior Backend Developer', '메시징 플랫폼 개발', '2017-07-01', NULL),
(6, 'NHN', 'Backend Developer', '게임 서버 개발', '2015-03-01', '2021-12-31'),
(6, '넥슨', 'Backend Lead', 'MMORPG 서버 아키텍처 설계', '2022-01-01', NULL),
-- 불합격자 (경력 부족 또는 스킬 미충족)
(7, '스타트업A', 'Developer', 'Python/Django 개발', '2009-03-01', '2012-12-31'),
(7, '스타트업B', 'Backend Developer', 'Python 기반 API 개발', '2013-01-01', NULL),
(8, '중소기업C', 'Junior Developer', 'Java 개발 (경력 2년)', '2016-03-01', '2018-12-31'),
(8, '중소기업D', 'Developer', 'Spring 학습 중', '2019-01-01', NULL),
(9, '제조업E', 'IT 담당', 'ERP 시스템 관리', '2012-03-01', NULL),
(10, '금융권F', 'System Engineer', 'Legacy 시스템 유지보수', '2008-03-01', '2015-12-31'),
(10, '금융권G', 'IT 관리자', '인프라 관리', '2016-01-01', NULL),
(11, '스타트업H', 'Full Stack Developer', 'Node.js 개발 (경력 1년)', '2017-03-01', '2018-12-31'),
(11, '스타트업I', 'Developer', 'React/Node.js 개발', '2019-01-01', NULL),
(12, '중견기업J', 'Backend Developer', 'Java 개발 (경력 4년)', '2013-03-01', '2017-12-31'),
(12, '중견기업K', 'Developer', 'Spring Boot 학습 중', '2018-01-01', NULL);

-- 프런트엔드 지원자 경력 (13-25번)
INSERT INTO career (applicant_id, company_name, position, description, start_date, end_date) VALUES
-- 합격자 (3년 이상 경력)
(13, '네이버', 'Frontend Developer', 'React 기반 웹 개발', '2014-03-01', '2018-12-31'),
(13, '카카오', 'Senior Frontend Developer', 'Next.js 기반 SSR 구현', '2019-01-01', NULL),
(14, '삼성전자', 'UI Developer', 'TypeScript 기반 컴포넌트 개발', '2012-03-01', '2017-06-30'),
(14, '쿠팡', 'Frontend Lead', '대규모 프런트엔드 아키텍처 설계', '2017-07-01', NULL),
(15, 'SK텔레콤', 'Frontend Developer', 'React 웹앱 개발', '2015-03-01', '2020-02-29'),
(15, '토스', 'Frontend Engineer', '금융 UI/UX 개발', '2020-03-01', NULL),
(16, 'LG전자', 'Web Developer', 'Vue.js 기반 개발', '2011-03-01', '2015-12-31'),
(16, '배달의민족', 'Frontend Developer', 'React/TypeScript 전환', '2016-01-01', NULL),
(17, '현대자동차', 'Frontend Engineer', 'Angular 기반 개발', '2016-03-01', '2020-06-30'),
(17, '라인', 'Frontend Developer', 'React/Next.js 마이그레이션', '2020-07-01', NULL),
(18, 'NHN', 'Frontend Developer', 'React 게임 UI 개발', '2010-03-01', '2016-12-31'),
(18, '넥슨', 'Senior Frontend Developer', 'Next.js 기반 웹 플랫폼', '2017-01-01', NULL),
-- 불합격자 (경력 부족 또는 스킬 미충족)
(19, '스타트업L', 'Frontend Developer', 'Vue.js 개발 (경력 2년)', '2017-03-01', '2019-12-31'),
(19, '스타트업M', 'Developer', 'Vue.js 계속 사용', '2020-01-01', NULL),
(20, '중소기업N', 'Junior Developer', 'HTML/CSS/jQuery (경력 1년)', '2019-03-01', '2020-12-31'),
(20, '중소기업O', 'Developer', 'React 학습 중', '2021-01-01', NULL),
(21, '제조업P', 'Web Designer', 'UI 디자인 및 퍼블리싱', '2009-03-01', NULL),
(22, '금융권Q', 'Frontend Developer', 'Angular 개발 (경력 2년)', '2013-03-01', '2015-12-31'),
(22, '금융권R', 'Developer', 'Angular 유지보수', '2016-01-01', NULL),
(23, '스타트업S', 'Full Stack Developer', 'Node.js/React (경력 1년)', '2020-03-01', '2021-12-31'),
(23, '스타트업T', 'Developer', 'React 개발 중', '2022-01-01', NULL),
(24, '중견기업U', 'Frontend Developer', 'jQuery 기반 개발 (경력 10년)', '2012-03-01', NULL),
(25, '중견기업V', 'Web Developer', 'React 개발 (경력 2년)', '2011-03-01', '2013-12-31'),
(25, '중견기업W', 'Developer', 'Vue.js 전환', '2014-01-01', NULL);
