-- Seed data for Social Service
-- Run this after migrations

-- ============================================
-- SAMPLE USERS (synced from CRM)
-- ============================================
INSERT INTO users (id, username, email, full_name, avatar_url, bio, role, is_verified) VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'admin', 'admin@edu.com', 'System Admin', NULL, 'Platform administrator', 'ADMIN', true),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'john_doe', 'john@student.edu', 'John Doe', NULL, 'CS Student | Class of 2025', 'STUDENT', true),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'jane_smith', 'jane@lecturer.edu', 'Dr. Jane Smith', NULL, 'Professor of Computer Science', 'LECTURER', true),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'tech_corp', 'hr@techcorp.com', 'Tech Corporation', NULL, 'Leading technology company', 'ENTERPRISE', true),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'alice_wong', 'alice@student.edu', 'Alice Wong', NULL, 'Software Engineering Student', 'STUDENT', false),
    ('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 'bob_johnson', 'bob@student.edu', 'Bob Johnson', NULL, 'Data Science Enthusiast', 'STUDENT', false)
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- SAMPLE ENTERPRISE PAGES
-- ============================================
INSERT INTO enterprise_pages (id, user_id, name, slug, description, website, industry, location, employee_count, is_verified, follower_count) VALUES
    ('11111111-1111-1111-1111-111111111111', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'Tech Corporation', 'tech-corp', 'Leading technology solutions provider', 'https://techcorp.com', 'Technology', 'Ho Chi Minh City', '500-1000', true, 150)
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- SAMPLE HASHTAGS
-- ============================================
INSERT INTO hashtags (id, name, post_count) VALUES
    ('22222222-2222-2222-2222-222222222221', 'programming', 25),
    ('22222222-2222-2222-2222-222222222222', 'java', 18),
    ('22222222-2222-2222-2222-222222222223', 'python', 22),
    ('22222222-2222-2222-2222-222222222224', 'career', 15),
    ('22222222-2222-2222-2222-222222222225', 'internship', 12),
    ('22222222-2222-2222-2222-222222222226', 'technology', 30)
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- SAMPLE POSTS
-- ============================================
INSERT INTO posts (id, user_id, content, status, visibility, like_count, comment_count) VALUES
    ('33333333-3333-3333-3333-333333333331', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Just finished my first Java project! 🎉 #programming #java', 'APPROVED', 'PUBLIC', 5, 2),
    ('33333333-3333-3333-3333-333333333332', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'Tips for new programmers: 1. Practice daily 2. Read documentation 3. Build projects #programming #career', 'APPROVED', 'PUBLIC', 15, 5),
    ('33333333-3333-3333-3333-333333333333', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'Looking for summer internship opportunities in software development. Any recommendations? #internship #technology', 'APPROVED', 'PUBLIC', 8, 3),
    ('33333333-3333-3333-3333-333333333334', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'This is a draft post', 'DRAFT', 'PRIVATE', 0, 0),
    ('33333333-3333-3333-3333-333333333335', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 'Learning Python data analysis with pandas. Amazing library! #python #programming', 'PENDING', 'PUBLIC', 0, 0)
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- SAMPLE COMMENTS
-- ============================================
INSERT INTO comments (id, post_id, user_id, content, like_count) VALUES
    ('44444444-4444-4444-4444-444444444441', '33333333-3333-3333-3333-333333333331', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'Congratulations! What project did you build?', 2),
    ('44444444-4444-4444-4444-444444444442', '33333333-3333-3333-3333-333333333331', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'Great job! Keep up the good work.', 1),
    ('44444444-4444-4444-4444-444444444443', '33333333-3333-3333-3333-333333333332', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Thanks for the tips, Professor!', 3),
    ('44444444-4444-4444-4444-444444444444', '33333333-3333-3333-3333-333333333333', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'We have internship openings! Check our page.', 5)
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- SAMPLE LIKES
-- ============================================
INSERT INTO likes (id, user_id, likeable_type, likeable_id) VALUES
    ('55555555-5555-5555-5555-555555555551', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'POST', '33333333-3333-3333-3333-333333333331'),
    ('55555555-5555-5555-5555-555555555552', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'POST', '33333333-3333-3333-3333-333333333331'),
    ('55555555-5555-5555-5555-555555555553', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'POST', '33333333-3333-3333-3333-333333333332'),
    ('55555555-5555-5555-5555-555555555554', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'POST', '33333333-3333-3333-3333-333333333332'),
    ('55555555-5555-5555-5555-555555555555', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 'POST', '33333333-3333-3333-3333-333333333332'),
    ('55555555-5555-5555-5555-555555555556', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'COMMENT', '44444444-4444-4444-4444-444444444443')
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- SAMPLE FOLLOWS
-- ============================================
INSERT INTO follows (id, follower_id, followee_id) VALUES
    ('66666666-6666-6666-6666-666666666661', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33'),
    ('66666666-6666-6666-6666-666666666662', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33'),
    ('66666666-6666-6666-6666-666666666663', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33'),
    ('66666666-6666-6666-6666-666666666664', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22'),
    ('66666666-6666-6666-6666-666666666665', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55')
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- SAMPLE PAGE FOLLOWERS
-- ============================================
INSERT INTO page_followers (id, page_id, user_id) VALUES
    ('77777777-7777-7777-7777-777777777771', '11111111-1111-1111-1111-111111111111', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22'),
    ('77777777-7777-7777-7777-777777777772', '11111111-1111-1111-1111-111111111111', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55'),
    ('77777777-7777-7777-7777-777777777773', '11111111-1111-1111-1111-111111111111', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66')
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- SAMPLE CONVERSATIONS
-- ============================================
INSERT INTO conversations (id, type, name, created_by) VALUES
    ('88888888-8888-8888-8888-888888888881', 'DIRECT', NULL, 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22'),
    ('88888888-8888-8888-8888-888888888882', 'GROUP', 'CS Study Group', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33')
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- SAMPLE CONVERSATION MEMBERS
-- ============================================
INSERT INTO conversation_members (id, conversation_id, user_id, role) VALUES
    ('99999999-9999-9999-9999-999999999991', '88888888-8888-8888-8888-888888888881', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'MEMBER'),
    ('99999999-9999-9999-9999-999999999992', '88888888-8888-8888-8888-888888888881', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'MEMBER'),
    ('99999999-9999-9999-9999-999999999993', '88888888-8888-8888-8888-888888888882', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'ADMIN'),
    ('99999999-9999-9999-9999-999999999994', '88888888-8888-8888-8888-888888888882', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'MEMBER'),
    ('99999999-9999-9999-9999-999999999995', '88888888-8888-8888-8888-888888888882', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'MEMBER'),
    ('99999999-9999-9999-9999-999999999996', '88888888-8888-8888-8888-888888888882', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 'MEMBER')
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- SAMPLE MESSAGES
-- ============================================
INSERT INTO messages (id, conversation_id, sender_id, message_type, content) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '88888888-8888-8888-8888-888888888881', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'TEXT', 'Hey Alice! How is your project going?'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaab', '88888888-8888-8888-8888-888888888881', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'TEXT', 'Its going well! Almost done with the frontend.'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaac', '88888888-8888-8888-8888-888888888882', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'TEXT', 'Welcome to the CS Study Group! Feel free to ask questions.'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaad', '88888888-8888-8888-8888-888888888882', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'TEXT', 'Thanks Professor! I have a question about data structures.')
ON CONFLICT (id) DO NOTHING;
