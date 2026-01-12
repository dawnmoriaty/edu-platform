-- =============================================
-- V3: CAREER MODULE SCHEMA
-- Enterprises, Job Postings, CVs, Applications
-- =============================================

-- Enterprise Profiles
CREATE TABLE enterprise_profiles (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL UNIQUE REFERENCES users(id),
    tax_code VARCHAR(20) NOT NULL UNIQUE,
    legal_name VARCHAR(200) NOT NULL,
    verification_status VARCHAR(20) DEFAULT 'PENDING',
    rejection_reason TEXT,
    display_name VARCHAR(150),
    short_name VARCHAR(50),
    logo VARCHAR(500),
    website VARCHAR(200),
    industry VARCHAR(100),
    company_size VARCHAR(20),
    address TEXT,
    description TEXT,
    hr_contact_name VARCHAR(100),
    hr_contact_email VARCHAR(100),
    hr_contact_phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by INTEGER,
    updated_by INTEGER
);

-- Job Postings
CREATE TABLE job_postings (
    id SERIAL PRIMARY KEY,
    enterprise_id INTEGER NOT NULL REFERENCES enterprise_profiles(id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    requirements TEXT,
    benefits TEXT,
    location VARCHAR(200),
    job_type VARCHAR(20),
    experience_level VARCHAR(20),
    salary_min DECIMAL(15,2),
    salary_max DECIMAL(15,2),
    salary_currency VARCHAR(3) DEFAULT 'VND',
    is_salary_negotiable BOOLEAN DEFAULT FALSE,
    start_date DATE,
    end_date DATE,
    max_applications INTEGER,
    is_vip BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by INTEGER,
    updated_by INTEGER
);

-- Student CVs
CREATE TABLE student_cvs (
    id SERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL REFERENCES student_profiles(id),
    title VARCHAR(200),
    summary TEXT,
    skills TEXT,
    education TEXT,
    experience TEXT,
    projects TEXT,
    certifications TEXT,
    languages TEXT,
    file_url VARCHAR(500),
    is_primary BOOLEAN DEFAULT FALSE,
    is_public BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by INTEGER,
    updated_by INTEGER
);

-- Job Applications
CREATE TABLE job_applications (
    id SERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL REFERENCES student_profiles(id),
    job_posting_id INTEGER NOT NULL REFERENCES job_postings(id),
    cv_id INTEGER REFERENCES student_cvs(id),
    cover_letter TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    note TEXT,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by INTEGER,
    updated_by INTEGER,
    UNIQUE(student_id, job_posting_id)
);

-- Create indexes
CREATE INDEX idx_enterprise_profiles_user_id ON enterprise_profiles(user_id);
CREATE INDEX idx_enterprise_profiles_verification_status ON enterprise_profiles(verification_status);
CREATE INDEX idx_job_postings_enterprise_id ON job_postings(enterprise_id);
CREATE INDEX idx_job_postings_is_active ON job_postings(is_active);
CREATE INDEX idx_student_cvs_student_id ON student_cvs(student_id);
CREATE INDEX idx_job_applications_student_id ON job_applications(student_id);
CREATE INDEX idx_job_applications_job_posting_id ON job_applications(job_posting_id);
