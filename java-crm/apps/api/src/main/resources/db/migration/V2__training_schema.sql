-- =============================================
-- V2: TRAINING MODULE SCHEMA
-- Faculties, Majors, Classes, Students, Teachers
-- =============================================

-- Faculties (Khoa)
CREATE TABLE faculties (
    id SERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by INTEGER,
    updated_by INTEGER
);

-- Majors (Ngành)
CREATE TABLE majors (
    id SERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    faculty_id INTEGER NOT NULL REFERENCES faculties(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by INTEGER,
    updated_by INTEGER
);

-- Academic Classes (Lớp hành chính)
CREATE TABLE academic_classes (
    id SERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    cohort_year INTEGER NOT NULL,
    major_id INTEGER NOT NULL REFERENCES majors(id),
    advisor_id INTEGER REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by INTEGER,
    updated_by INTEGER
);

-- Student Profiles
CREATE TABLE student_profiles (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL UNIQUE REFERENCES users(id),
    student_code VARCHAR(20) NOT NULL UNIQUE,
    class_id INTEGER REFERENCES academic_classes(id),
    status VARCHAR(20) DEFAULT 'STUDYING',
    gpa DECIMAL(3,2),
    total_credits INTEGER DEFAULT 0,
    national_id VARCHAR(20),
    date_of_birth DATE,
    gender VARCHAR(10),
    address TEXT,
    phone VARCHAR(20),
    bank_account VARCHAR(50),
    bank_name VARCHAR(100),
    tax_code VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by INTEGER,
    updated_by INTEGER
);

-- Teacher Profiles
CREATE TABLE teacher_profiles (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL UNIQUE REFERENCES users(id),
    teacher_code VARCHAR(20) NOT NULL UNIQUE,
    faculty_id INTEGER REFERENCES faculties(id),
    degree VARCHAR(20),
    rank VARCHAR(20),
    contract_type VARCHAR(20),
    national_id VARCHAR(20),
    date_of_birth DATE,
    gender VARCHAR(10),
    address TEXT,
    phone VARCHAR(20),
    salary_coefficient DECIMAL(4,2),
    bank_account VARCHAR(50),
    bank_name VARCHAR(100),
    tax_code VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by INTEGER,
    updated_by INTEGER
);

-- Create indexes
CREATE INDEX idx_student_profiles_user_id ON student_profiles(user_id);
CREATE INDEX idx_student_profiles_class_id ON student_profiles(class_id);
CREATE INDEX idx_teacher_profiles_user_id ON teacher_profiles(user_id);
CREATE INDEX idx_teacher_profiles_faculty_id ON teacher_profiles(faculty_id);
CREATE INDEX idx_academic_classes_major_id ON academic_classes(major_id);
CREATE INDEX idx_majors_faculty_id ON majors(faculty_id);
