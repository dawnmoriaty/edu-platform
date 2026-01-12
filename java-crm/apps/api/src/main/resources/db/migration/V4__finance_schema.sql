-- =============================================
-- V4: FINANCE MODULE SCHEMA
-- Wallets, Tuition, Payroll
-- =============================================

-- Enterprise Wallets
CREATE TABLE enterprise_wallets (
    id SERIAL PRIMARY KEY,
    enterprise_id INTEGER NOT NULL UNIQUE REFERENCES enterprise_profiles(id),
    balance DECIMAL(15,2) DEFAULT 0,
    total_deposited DECIMAL(15,2) DEFAULT 0,
    total_spent DECIMAL(15,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by INTEGER,
    updated_by INTEGER
);

-- Wallet Transactions
CREATE TABLE wallet_transactions (
    id SERIAL PRIMARY KEY,
    wallet_id INTEGER NOT NULL REFERENCES enterprise_wallets(id),
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    balance_before DECIMAL(15,2),
    balance_after DECIMAL(15,2),
    description TEXT,
    reference_id VARCHAR(100),
    reference_type VARCHAR(50),
    transaction_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by INTEGER,
    updated_by INTEGER
);

-- Student Tuition
CREATE TABLE student_tuitions (
    id SERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL REFERENCES student_profiles(id),
    semester VARCHAR(20) NOT NULL,
    academic_year INTEGER NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    paid_amount DECIMAL(15,2) DEFAULT 0,
    discount_amount DECIMAL(15,2) DEFAULT 0,
    debt_amount DECIMAL(15,2) GENERATED ALWAYS AS (total_amount - paid_amount - discount_amount) STORED,
    status VARCHAR(20) DEFAULT 'UNPAID',
    due_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by INTEGER,
    updated_by INTEGER,
    UNIQUE(student_id, semester)
);

-- Teacher Payroll
CREATE TABLE teacher_payrolls (
    id SERIAL PRIMARY KEY,
    teacher_id INTEGER NOT NULL REFERENCES teacher_profiles(id),
    period VARCHAR(10) NOT NULL,
    month INTEGER NOT NULL,
    year INTEGER NOT NULL,
    base_salary DECIMAL(15,2),
    position_allowance DECIMAL(15,2) DEFAULT 0,
    degree_allowance DECIMAL(15,2) DEFAULT 0,
    rank_allowance DECIMAL(15,2) DEFAULT 0,
    teaching_hours DECIMAL(5,2) DEFAULT 0,
    overtime_pay DECIMAL(15,2) DEFAULT 0,
    social_insurance DECIMAL(15,2) DEFAULT 0,
    health_insurance DECIMAL(15,2) DEFAULT 0,
    unemployment_insurance DECIMAL(15,2) DEFAULT 0,
    personal_income_tax DECIMAL(15,2) DEFAULT 0,
    other_deductions DECIMAL(15,2) DEFAULT 0,
    gross_salary DECIMAL(15,2),
    net_salary DECIMAL(15,2),
    status VARCHAR(20) DEFAULT 'PENDING',
    payment_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by INTEGER,
    updated_by INTEGER,
    UNIQUE(teacher_id, period)
);

-- Create indexes
CREATE INDEX idx_enterprise_wallets_enterprise_id ON enterprise_wallets(enterprise_id);
CREATE INDEX idx_wallet_transactions_wallet_id ON wallet_transactions(wallet_id);
CREATE INDEX idx_wallet_transactions_type ON wallet_transactions(type);
CREATE INDEX idx_student_tuitions_student_id ON student_tuitions(student_id);
CREATE INDEX idx_student_tuitions_status ON student_tuitions(status);
CREATE INDEX idx_teacher_payrolls_teacher_id ON teacher_payrolls(teacher_id);
CREATE INDEX idx_teacher_payrolls_period ON teacher_payrolls(period);
