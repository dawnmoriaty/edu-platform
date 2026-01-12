-- =====================================================
-- V2: Seed RBAC Data (Roles, Permissions, Users)
-- =====================================================

-- 1. Insert Roles
INSERT INTO roles (id, name, description) VALUES
    ('00000000-0000-0000-0000-000000000001', 'SUPER_ADMIN', 'Super Administrator - Full access'),
    ('00000000-0000-0000-0000-000000000002', 'ADMIN', 'Administrator - Manage users and content'),
    ('00000000-0000-0000-0000-000000000003', 'TEACHER', 'Teacher - Manage courses and students'),
    ('00000000-0000-0000-0000-000000000004', 'STUDENT', 'Student - View courses and submit assignments')
ON CONFLICT (id) DO NOTHING;

-- 2. Insert Permissions (Resource + Action)
INSERT INTO permissions (id, name, resource, action, description) VALUES
    -- User Management
    ('10000000-0000-0000-0000-000000000001', 'user:view', 'user', 'VIEW', 'View users'),
    ('10000000-0000-0000-0000-000000000002', 'user:add', 'user', 'ADD', 'Create users'),
    ('10000000-0000-0000-0000-000000000003', 'user:update', 'user', 'UPDATE', 'Update users'),
    ('10000000-0000-0000-0000-000000000004', 'user:delete', 'user', 'DELETE', 'Delete users'),
    
    -- Role Management
    ('10000000-0000-0000-0000-000000000011', 'role:view', 'role', 'VIEW', 'View roles'),
    ('10000000-0000-0000-0000-000000000012', 'role:add', 'role', 'ADD', 'Create roles'),
    ('10000000-0000-0000-0000-000000000013', 'role:update', 'role', 'UPDATE', 'Update roles'),
    ('10000000-0000-0000-0000-000000000014', 'role:delete', 'role', 'DELETE', 'Delete roles'),
    
    -- Permission Management
    ('10000000-0000-0000-0000-000000000021', 'permission:view', 'permission', 'VIEW', 'View permissions'),
    ('10000000-0000-0000-0000-000000000022', 'permission:add', 'permission', 'ADD', 'Create permissions'),
    ('10000000-0000-0000-0000-000000000023', 'permission:update', 'permission', 'UPDATE', 'Update permissions'),
    ('10000000-0000-0000-0000-000000000024', 'permission:delete', 'permission', 'DELETE', 'Delete permissions'),
    
    -- Student Management
    ('10000000-0000-0000-0000-000000000031', 'student:view', 'student', 'VIEW', 'View students'),
    ('10000000-0000-0000-0000-000000000032', 'student:add', 'student', 'ADD', 'Create students'),
    ('10000000-0000-0000-0000-000000000033', 'student:update', 'student', 'UPDATE', 'Update students'),
    ('10000000-0000-0000-0000-000000000034', 'student:delete', 'student', 'DELETE', 'Delete students'),
    
    -- Course Management
    ('10000000-0000-0000-0000-000000000041', 'course:view', 'course', 'VIEW', 'View courses'),
    ('10000000-0000-0000-0000-000000000042', 'course:add', 'course', 'ADD', 'Create courses'),
    ('10000000-0000-0000-0000-000000000043', 'course:update', 'course', 'UPDATE', 'Update courses'),
    ('10000000-0000-0000-0000-000000000044', 'course:delete', 'course', 'DELETE', 'Delete courses'),
    
    -- Finance Management
    ('10000000-0000-0000-0000-000000000051', 'finance:view', 'finance', 'VIEW', 'View finance'),
    ('10000000-0000-0000-0000-000000000052', 'finance:add', 'finance', 'ADD', 'Create finance records'),
    ('10000000-0000-0000-0000-000000000053', 'finance:update', 'finance', 'UPDATE', 'Update finance records'),
    ('10000000-0000-0000-0000-000000000054', 'finance:delete', 'finance', 'DELETE', 'Delete finance records')
ON CONFLICT (id) DO NOTHING;

-- 3. Assign Permissions to Roles

-- SUPER_ADMIN: All permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000001', id FROM permissions
ON CONFLICT DO NOTHING;

-- ADMIN: User, Student, Course management (no delete)
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001'), -- user:view
    ('00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002'), -- user:add
    ('00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000003'), -- user:update
    ('00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000031'), -- student:view
    ('00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000032'), -- student:add
    ('00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000033'), -- student:update
    ('00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000034'), -- student:delete
    ('00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000041'), -- course:view
    ('00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000042'), -- course:add
    ('00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000043'), -- course:update
    ('00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000044')  -- course:delete
ON CONFLICT DO NOTHING;

-- TEACHER: View students, Manage courses
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000031'), -- student:view
    ('00000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000041'), -- course:view
    ('00000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000043')  -- course:update
ON CONFLICT DO NOTHING;

-- STUDENT: View courses only
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000041')  -- course:view
ON CONFLICT DO NOTHING;

-- 4. Insert Default Users (password: 123456 - bcrypt hash with 12 rounds)
-- Generated using: BCrypt.hashpw("123456", BCrypt.gensalt(12))
INSERT INTO users (id, username, email, password_hash, first_name, last_name, status) VALUES
    ('20000000-0000-0000-0000-000000000001', 'admin', 'admin@eduplatform.com', '$2a$12$DHL1zjygK1Bk0UnTjq79R.NdMzNiOYqaUn9WoMh7drj8RbBs98GOe', 'Super', 'Admin', 'ACTIVE'),
    ('20000000-0000-0000-0000-000000000002', 'manager', 'manager@eduplatform.com', '$2a$12$DHL1zjygK1Bk0UnTjq79R.NdMzNiOYqaUn9WoMh7drj8RbBs98GOe', 'Manager', 'User', 'ACTIVE'),
    ('20000000-0000-0000-0000-000000000003', 'teacher', 'teacher@eduplatform.com', '$2a$12$DHL1zjygK1Bk0UnTjq79R.NdMzNiOYqaUn9WoMh7drj8RbBs98GOe', 'Teacher', 'Demo', 'ACTIVE'),
    ('20000000-0000-0000-0000-000000000004', 'student', 'student@eduplatform.com', '$2a$12$DHL1zjygK1Bk0UnTjq79R.NdMzNiOYqaUn9WoMh7drj8RbBs98GOe', 'Student', 'Demo', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 5. Assign Roles to Users
INSERT INTO user_roles (user_id, role_id) VALUES
    ('20000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001'), -- admin -> SUPER_ADMIN
    ('20000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002'), -- manager -> ADMIN
    ('20000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000003'), -- teacher -> TEACHER
    ('20000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000004')  -- student -> STUDENT
ON CONFLICT DO NOTHING;

-- Done!
-- Test accounts:
-- | Username | Password | Role        |
-- |----------|----------|-------------|
-- | admin    | 123456   | SUPER_ADMIN |
-- | manager  | 123456   | ADMIN       |
-- | teacher  | 123456   | TEACHER     |
-- | student  | 123456   | STUDENT     |
