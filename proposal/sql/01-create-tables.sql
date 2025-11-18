-- AINJOB HR SaaS Platform - Database Schema
-- PostgreSQL 15

-- 1. Company (기업)
CREATE TABLE company (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    business_number VARCHAR(20) UNIQUE NOT NULL,
    industry VARCHAR(100),
    address VARCHAR(500),
    phone VARCHAR(20),
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_company_business_number ON company(business_number);
CREATE INDEX idx_company_name ON company(name);

-- 2. Skill (기술)
CREATE TABLE skill (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    category VARCHAR(50),
    description TEXT
);

CREATE INDEX idx_skill_name ON skill(name);
CREATE INDEX idx_skill_category ON skill(category);

-- 3. JobPosting (채용 공고)
CREATE TABLE job_posting (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    position VARCHAR(50) NOT NULL,
    min_education_level VARCHAR(20) NOT NULL,
    accepted_majors TEXT,  -- JSON 배열
    min_years_of_experience INT DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    open_date DATE,
    close_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES company(id),
    CONSTRAINT chk_min_experience CHECK (min_years_of_experience >= 0)
);

CREATE INDEX idx_jobposting_company ON job_posting(company_id);
CREATE INDEX idx_jobposting_status ON job_posting(status);
CREATE INDEX idx_jobposting_position ON job_posting(position);
CREATE INDEX idx_jobposting_dates ON job_posting(open_date, close_date);

-- 4. JobPostingSkill (공고별 요구 기술)
CREATE TABLE job_posting_skill (
    id BIGSERIAL PRIMARY KEY,
    job_posting_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    is_required BOOLEAN DEFAULT TRUE,
    min_proficiency INT,
    FOREIGN KEY (job_posting_id) REFERENCES job_posting(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skill(id)
);

CREATE INDEX idx_jobposting_skill_posting ON job_posting_skill(job_posting_id);
CREATE INDEX idx_jobposting_skill_skill ON job_posting_skill(skill_id);

-- 5. Applicant (지원자)
CREATE TABLE applicant (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    birth_date DATE,
    city VARCHAR(50),
    district VARCHAR(50),
    address_detail VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_applicant_email ON applicant(email);
CREATE INDEX idx_applicant_name ON applicant(name);

-- 6. Major (전공)
CREATE TABLE major (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    category VARCHAR(50)
);

CREATE INDEX idx_major_name ON major(name);

-- 7. Education (학력)
CREATE TABLE education (
    id BIGSERIAL PRIMARY KEY,
    applicant_id BIGINT NOT NULL,
    major_id BIGINT,
    education_level VARCHAR(20) NOT NULL,
    school_name VARCHAR(200) NOT NULL,
    start_date DATE,
    end_date DATE,
    status VARCHAR(20),
    FOREIGN KEY (applicant_id) REFERENCES applicant(id) ON DELETE CASCADE,
    FOREIGN KEY (major_id) REFERENCES major(id)
);

CREATE INDEX idx_education_applicant ON education(applicant_id);
CREATE INDEX idx_education_level ON education(education_level);
CREATE INDEX idx_education_major ON education(major_id);

-- 8. Career (경력)
CREATE TABLE career (
    id BIGSERIAL PRIMARY KEY,
    applicant_id BIGINT NOT NULL,
    company_name VARCHAR(200) NOT NULL,
    position VARCHAR(100),
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE,
    FOREIGN KEY (applicant_id) REFERENCES applicant(id) ON DELETE CASCADE,
    CONSTRAINT chk_career_dates CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE INDEX idx_career_applicant ON career(applicant_id);
CREATE INDEX idx_career_dates ON career(start_date, end_date);

-- 9. CareerSkill (경력 기술)
CREATE TABLE career_skill (
    id BIGSERIAL PRIMARY KEY,
    career_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    proficiency_level INT,
    FOREIGN KEY (career_id) REFERENCES career(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skill(id),
    CONSTRAINT chk_proficiency_level CHECK (proficiency_level BETWEEN 1 AND 5)
);

CREATE INDEX idx_career_skill_career ON career_skill(career_id);
CREATE INDEX idx_career_skill_skill ON career_skill(skill_id);

-- 10. ResumeTracking (이력서 버전 관리)
CREATE TABLE resume_tracking (
    id BIGSERIAL PRIMARY KEY,
    applicant_id BIGINT NOT NULL,
    title VARCHAR(200),
    s3_key VARCHAR(500) NOT NULL,
    file_name VARCHAR(200) NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(100),
    version INT DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (applicant_id) REFERENCES applicant(id) ON DELETE CASCADE,
    CONSTRAINT chk_file_size CHECK (file_size <= 10485760)
);

CREATE INDEX idx_resume_applicant ON resume_tracking(applicant_id);
CREATE INDEX idx_resume_active ON resume_tracking(applicant_id, is_active);

-- 11. ApplicationTracking (지원 추적)
CREATE TABLE application_tracking (
    id BIGSERIAL PRIMARY KEY,
    applicant_id BIGINT NOT NULL,
    job_posting_id BIGINT NOT NULL,
    resume_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (applicant_id) REFERENCES applicant(id),
    FOREIGN KEY (job_posting_id) REFERENCES job_posting(id),
    FOREIGN KEY (resume_id) REFERENCES resume_tracking(id),
    CONSTRAINT uk_application_applicant_posting UNIQUE (applicant_id, job_posting_id)
);

CREATE INDEX idx_application_applicant ON application_tracking(applicant_id);
CREATE INDEX idx_application_posting ON application_tracking(job_posting_id);
CREATE INDEX idx_application_status ON application_tracking(status);
CREATE INDEX idx_application_applied_at ON application_tracking(applied_at);

-- 12. ApplicationStatusHistory (지원 상태 이력)
CREATE TABLE application_status_history (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL,
    from_status VARCHAR(20),
    to_status VARCHAR(20) NOT NULL,
    reason TEXT,
    changed_by VARCHAR(100),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (application_id) REFERENCES application_tracking(id) ON DELETE CASCADE
);

CREATE INDEX idx_status_history_application ON application_status_history(application_id);
CREATE INDEX idx_status_history_changed_at ON application_status_history(changed_at);

-- Trigger: 활성 이력서 1개만 허용
CREATE OR REPLACE FUNCTION fn_resume_active_check()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.is_active = TRUE THEN
        UPDATE resume_tracking
        SET is_active = FALSE
        WHERE applicant_id = NEW.applicant_id
          AND is_active = TRUE
          AND id != NEW.id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_resume_active_check
BEFORE INSERT OR UPDATE ON resume_tracking
FOR EACH ROW
EXECUTE FUNCTION fn_resume_active_check();

-- Trigger: updated_at 자동 갱신
CREATE OR REPLACE FUNCTION fn_update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_company_updated_at
BEFORE UPDATE ON company
FOR EACH ROW
EXECUTE FUNCTION fn_update_timestamp();

CREATE TRIGGER trg_jobposting_updated_at
BEFORE UPDATE ON job_posting
FOR EACH ROW
EXECUTE FUNCTION fn_update_timestamp();

CREATE TRIGGER trg_applicant_updated_at
BEFORE UPDATE ON applicant
FOR EACH ROW
EXECUTE FUNCTION fn_update_timestamp();

CREATE TRIGGER trg_application_updated_at
BEFORE UPDATE ON application_tracking
FOR EACH ROW
EXECUTE FUNCTION fn_update_timestamp();
