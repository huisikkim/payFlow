-- 이력서 및 지원 데이터

-- 이력서 데이터 (모든 지원자)
INSERT INTO resume_tracking (applicant_id, title, s3_key, file_name, file_size, content_type, version, is_active) VALUES
(1, '김철수 이력서', 'resumes/2025/11/applicant-1-v1.pdf', '김철수_이력서.pdf', 524288, 'application/pdf', 1, TRUE),
(2, '이영희 이력서', 'resumes/2025/11/applicant-2-v1.pdf', '이영희_이력서.pdf', 612352, 'application/pdf', 1, TRUE),
(3, '박민수 이력서', 'resumes/2025/1101/applicant-3-v1.pdf', '박민수_이력서.pdf', 498765, 'application/pdf', 1, TRUE),
(4, '정수진 이력서', 'resumes/2025/11/applicant-4-v1.pdf', '정수진_이력서.pdf', 556789, 'application/pdf', 1, TRUE),
(5, '최동욱 이력서', 'resumes/2025/11/applicant-5-v1.pdf', '최동욱_이력서.pdf', 587234, 'application/pdf', 1, TRUE),
(6, '강민지 이력서', 'resumes/2025/11/applicant-6-v1.pdf', '강민지_이력서.pdf', 534567, 'application/pdf', 1, TRUE),
(7, '윤서준 이력서', 'resumes/2025/11/applicant-7-v1.pdf', '윤서준_이력서.pdf', 478901, 'application/pdf', 1, TRUE),
(8, '임하은 이력서', 'resumes/2025/11/applicant-8-v1.pdf', '임하은_이력서.pdf', 445678, 'application/pdf', 1, TRUE),
(9, '한지우 이력서', 'resumes/2025/11/applicant-9-v1.pdf', '한지우_이력서.pdf', 512345, 'application/pdf', 1, TRUE),
(10, '오세훈 이력서', 'resumes/2025/11/applicant-10-v1.pdf', '오세훈_이력서.pdf', 598765, 'application/pdf', 1, TRUE),
(11, '신예린 이력서', 'resumes/2025/11/applicant-11-v1.pdf', '신예린_이력서.pdf', 467890, 'application/pdf', 1, TRUE),
(12, '배준호 이력서', 'resumes/2025/11/applicant-12-v1.pdf', '배준호_이력서.pdf', 523456, 'application/pdf', 1, TRUE),
(13, '서지훈 이력서', 'resumes/2025/11/applicant-13-v1.pdf', '서지훈_이력서.pdf', 545678, 'application/pdf', 1, TRUE),
(14, '권나영 이력서', 'resumes/2025/11/applicant-14-v1.pdf', '권나영_이력서.pdf', 578901, 'application/pdf', 1, TRUE),
(15, '조민석 이력서', 'resumes/2025/11/applicant-15-v1.pdf', '조민석_이력서.pdf', 534567, 'application/pdf', 1, TRUE),
(16, '황수빈 이력서', 'resumes/2025/11/applicant-16-v1.pdf', '황수빈_이력서.pdf', 512345, 'application/pdf', 1, TRUE),
(17, '송태양 이력서', 'resumes/2025/11/applicant-17-v1.pdf', '송태양_이력서.pdf', 498765, 'application/pdf', 1, TRUE),
(18, '안유진 이력서', 'resumes/2025/11/applicant-18-v1.pdf', '안유진_이력서.pdf', 567890, 'application/pdf', 1, TRUE),
(19, '장현우 이력서', 'resumes/2025/11/applicant-19-v1.pdf', '장현우_이력서.pdf', 445678, 'application/pdf', 1, TRUE),
(20, '노서연 이력서', 'resumes/2025/11/applicant-20-v1.pdf', '노서연_이력서.pdf', 423456, 'application/pdf', 1, TRUE),
(21, '문재현 이력서', 'resumes/2025/11/applicant-21-v1.pdf', '문재현_이력서.pdf', 489012, 'application/pdf', 1, TRUE),
(22, '홍다은 이력서', 'resumes/2025/11/applicant-22-v1.pdf', '홍다은_이력서.pdf', 534567, 'application/pdf', 1, TRUE),
(23, '유준서 이력서', 'resumes/2025/11/applicant-23-v1.pdf', '유준서_이력서.pdf', 456789, 'application/pdf', 1, TRUE),
(24, '전소희 이력서', 'resumes/2025/11/applicant-24-v1.pdf', '전소희_이력서.pdf', 478901, 'application/pdf', 1, TRUE),
(25, '고민재 이력서', 'resumes/2025/11/applicant-25-v1.pdf', '고민재_이력서.pdf', 512345, 'application/pdf', 1, TRUE);

-- 지원 데이터
-- 기업 1 (테크이노베이션): 지원자 1-12번
-- 백엔드 공고 (job_posting_id = 1): 지원자 1-12번
-- 프런트엔드 공고 (job_posting_id = 2): 지원자 13-25번 중 일부는 기업 2로

-- 기업 1 백엔드 공고 지원 (1-12번)
INSERT INTO application_tracking (applicant_id, job_posting_id, resume_id, status, applied_at) VALUES
(1, 1, 1, 'APPLIED', '2025-11-15 09:00:00'),
(2, 1, 2, 'APPLIED', '2025-11-15 10:30:00'),
(3, 1, 3, 'APPLIED', '2025-11-16 11:00:00'),
(4, 1, 4, 'APPLIED', '2025-11-16 14:20:00'),
(5, 1, 5, 'APPLIED', '2025-11-17 09:45:00'),
(6, 1, 6, 'APPLIED', '2025-11-17 15:10:00'),
(7, 1, 7, 'APPLIED', '2025-11-18 10:00:00'),
(8, 1, 8, 'APPLIED', '2025-11-18 13:30:00'),
(9, 1, 9, 'APPLIED', '2025-11-18 09:15:00'),
(10, 1, 10, 'APPLIED', '2025-11-18 16:00:00'),
(11, 1, 11, 'APPLIED', '2025-11-18 10:30:00'),
(12, 1, 12, 'APPLIED', '2025-11-18 14:45:00');

-- 기업 1 프런트엔드 공고 지원 (13-18번)
INSERT INTO application_tracking (applicant_id, job_posting_id, resume_id, status, applied_at) VALUES
(13, 2, 13, 'APPLIED', '2025-11-15 09:30:00'),
(14, 2, 14, 'APPLIED', '2025-11-15 11:00:00'),
(15, 2, 15, 'APPLIED', '2025-11-16 10:30:00'),
(16, 2, 16, 'APPLIED', '2025-11-16 15:00:00'),
(17, 2, 17, 'APPLIED', '2025-11-17 10:15:00'),
(18, 2, 18, 'APPLIED', '2025-11-17 14:30:00');

-- 기업 2 (디지털솔루션즈): 지원자 13-25번
-- 백엔드 공고 (job_posting_id = 3): 지원자 1-12번 중 일부는 기업 1로
-- 프런트엔드 공고 (job_posting_id = 4): 지원자 19-25번

-- 기업 2 백엔드 공고 지원 (1-12번은 기업 1에 지원했으므로 중복 방지)
-- 실제로는 다른 지원자가 지원하지만, 과제 요구사항에 따라 13-25번이 기업 2에 지원

-- 기업 2 프런트엔드 공고 지원 (19-25번)
INSERT INTO application_tracking (applicant_id, job_posting_id, resume_id, status, applied_at) VALUES
(19, 4, 19, 'APPLIED', '2025-11-15 10:00:00'),
(20, 4, 20, 'APPLIED', '2025-11-15 12:30:00'),
(21, 4, 21, 'APPLIED', '2025-11-16 09:45:00'),
(22, 4, 22, 'APPLIED', '2025-11-16 13:15:00'),
(23, 4, 23, 'APPLIED', '2025-11-17 11:30:00'),
(24, 4, 24, 'APPLIED', '2025-11-17 16:45:00'),
(25, 4, 25, 'APPLIED', '2025-11-18 09:00:00');

-- 지원 상태 이력 (초기 지원 시)
INSERT INTO application_status_history (application_id, from_status, to_status, reason, changed_by, changed_at)
SELECT 
    id,
    NULL,
    'APPLIED',
    '지원 접수',
    'SYSTEM',
    applied_at
FROM application_tracking;
