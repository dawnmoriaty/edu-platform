-- +goose Up
-- +goose StatementBegin

-- ============================================
-- USERS & PROFILES (sync từ Java CRM)
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(100),
    avatar_url VARCHAR(500),
    cover_url VARCHAR(500),
    bio TEXT,
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT', -- STUDENT, LECTURER, ADMIN, ENTERPRISE
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================
-- ENTERPRISE PAGES (Trang doanh nghiệp)
-- ============================================
CREATE TABLE IF NOT EXISTS enterprise_pages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE, -- owner
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    logo_url VARCHAR(500),
    cover_url VARCHAR(500),
    description TEXT,
    website VARCHAR(255),
    industry VARCHAR(100),
    location VARCHAR(255),
    employee_count VARCHAR(50),
    is_verified BOOLEAN DEFAULT FALSE,
    follower_count INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_enterprise_pages_slug ON enterprise_pages(slug);
CREATE INDEX idx_enterprise_pages_user_id ON enterprise_pages(user_id);

-- ============================================
-- PAGE FOLLOWERS
-- ============================================
CREATE TABLE IF NOT EXISTS page_followers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    page_id UUID NOT NULL REFERENCES enterprise_pages(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(page_id, user_id)
);

CREATE INDEX idx_page_followers_page_id ON page_followers(page_id);
CREATE INDEX idx_page_followers_user_id ON page_followers(user_id);

-- ============================================
-- HASHTAGS
-- ============================================
CREATE TABLE IF NOT EXISTS hashtags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    post_count INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_hashtags_name ON hashtags(name);

-- ============================================
-- POSTS (Bài viết)
-- ============================================
CREATE TYPE post_status AS ENUM ('DRAFT', 'PENDING', 'APPROVED', 'REJECTED');
CREATE TYPE post_visibility AS ENUM ('PUBLIC', 'FOLLOWERS', 'PRIVATE');

CREATE TABLE IF NOT EXISTS posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    page_id UUID REFERENCES enterprise_pages(id) ON DELETE CASCADE, -- null = personal post
    content TEXT NOT NULL,
    status post_status NOT NULL DEFAULT 'PENDING',
    visibility post_visibility NOT NULL DEFAULT 'PUBLIC',
    reviewed_by UUID REFERENCES users(id),
    reviewed_at TIMESTAMP WITH TIME ZONE,
    review_note TEXT,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    share_count INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_posts_user_id ON posts(user_id);
CREATE INDEX idx_posts_page_id ON posts(page_id);
CREATE INDEX idx_posts_status ON posts(status);
CREATE INDEX idx_posts_created_at ON posts(created_at DESC);

-- ============================================
-- POST MEDIA (Ảnh/Video của bài viết)
-- ============================================
CREATE TYPE media_type AS ENUM ('IMAGE', 'VIDEO', 'DOCUMENT');

CREATE TABLE IF NOT EXISTS post_media (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    media_type media_type NOT NULL DEFAULT 'IMAGE',
    url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    width INT,
    height INT,
    file_size INT,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_post_media_post_id ON post_media(post_id);

-- ============================================
-- POST HASHTAGS (Many-to-Many)
-- ============================================
CREATE TABLE IF NOT EXISTS post_hashtags (
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    hashtag_id UUID NOT NULL REFERENCES hashtags(id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, hashtag_id)
);

-- ============================================
-- COMMENTS
-- ============================================
CREATE TABLE IF NOT EXISTS comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    parent_id UUID REFERENCES comments(id) ON DELETE CASCADE, -- reply
    content TEXT NOT NULL,
    like_count INT DEFAULT 0,
    reply_count INT DEFAULT 0,
    is_edited BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_comments_post_id ON comments(post_id);
CREATE INDEX idx_comments_user_id ON comments(user_id);
CREATE INDEX idx_comments_parent_id ON comments(parent_id);

-- ============================================
-- LIKES (cho cả Post và Comment)
-- ============================================
CREATE TYPE likeable_type AS ENUM ('POST', 'COMMENT');

CREATE TABLE IF NOT EXISTS likes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    likeable_type likeable_type NOT NULL,
    likeable_id UUID NOT NULL, -- post_id hoặc comment_id
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(user_id, likeable_type, likeable_id)
);

CREATE INDEX idx_likes_likeable ON likes(likeable_type, likeable_id);
CREATE INDEX idx_likes_user_id ON likes(user_id);

-- ============================================
-- FOLLOWS (User follows User)
-- ============================================
CREATE TABLE IF NOT EXISTS follows (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    follower_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    followee_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(follower_id, followee_id)
);

CREATE INDEX idx_follows_follower_id ON follows(follower_id);
CREATE INDEX idx_follows_followee_id ON follows(followee_id);

-- ============================================
-- CONVERSATIONS (Chat)
-- ============================================
CREATE TYPE conversation_type AS ENUM ('DIRECT', 'GROUP');

CREATE TABLE IF NOT EXISTS conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type conversation_type NOT NULL DEFAULT 'DIRECT',
    name VARCHAR(100), -- for group chat
    avatar_url VARCHAR(500), -- for group chat
    created_by UUID REFERENCES users(id),
    last_message_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================
-- CONVERSATION MEMBERS
-- ============================================
CREATE TABLE IF NOT EXISTS conversation_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(20) DEFAULT 'MEMBER', -- ADMIN, MEMBER
    last_read_at TIMESTAMP WITH TIME ZONE,
    is_muted BOOLEAN DEFAULT FALSE,
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(conversation_id, user_id)
);

CREATE INDEX idx_conversation_members_user_id ON conversation_members(user_id);
CREATE INDEX idx_conversation_members_conversation_id ON conversation_members(conversation_id);

-- ============================================
-- MESSAGES
-- ============================================
CREATE TYPE message_type AS ENUM ('TEXT', 'IMAGE', 'FILE', 'SYSTEM');

CREATE TABLE IF NOT EXISTS messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    message_type message_type NOT NULL DEFAULT 'TEXT',
    content TEXT,
    media_url VARCHAR(500),
    is_edited BOOLEAN DEFAULT FALSE,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX idx_messages_sender_id ON messages(sender_id);
CREATE INDEX idx_messages_created_at ON messages(created_at DESC);

-- ============================================
-- MESSAGE REACTIONS
-- ============================================
CREATE TABLE IF NOT EXISTS message_reactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    emoji VARCHAR(10) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(message_id, user_id, emoji)
);

-- ============================================
-- NOTIFICATIONS
-- ============================================
CREATE TYPE notification_type AS ENUM (
    'LIKE_POST', 'LIKE_COMMENT', 'COMMENT', 'REPLY', 
    'FOLLOW', 'MENTION', 'POST_APPROVED', 'POST_REJECTED',
    'PAGE_FOLLOW', 'NEW_MESSAGE'
);

CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    actor_id UUID REFERENCES users(id) ON DELETE SET NULL,
    type notification_type NOT NULL,
    reference_id UUID, -- post_id, comment_id, etc.
    content TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(user_id, is_read);

-- ============================================
-- STUDENT CV
-- ============================================
CREATE TABLE IF NOT EXISTS student_cvs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE UNIQUE,
    title VARCHAR(200),
    summary TEXT,
    skills JSONB DEFAULT '[]',
    education JSONB DEFAULT '[]',
    experience JSONB DEFAULT '[]',
    projects JSONB DEFAULT '[]',
    certifications JSONB DEFAULT '[]',
    languages JSONB DEFAULT '[]',
    contact_info JSONB DEFAULT '{}',
    is_public BOOLEAN DEFAULT TRUE,
    pdf_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_student_cvs_user_id ON student_cvs(user_id);

-- ============================================
-- FILE UPLOADS
-- ============================================
CREATE TABLE IF NOT EXISTS file_uploads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100),
    file_size BIGINT,
    url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    is_public BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_file_uploads_user_id ON file_uploads(user_id);

-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
DROP TABLE IF EXISTS file_uploads;
DROP TABLE IF EXISTS student_cvs;
DROP TABLE IF EXISTS notifications;
DROP TYPE IF EXISTS notification_type;
DROP TABLE IF EXISTS message_reactions;
DROP TABLE IF EXISTS messages;
DROP TYPE IF EXISTS message_type;
DROP TABLE IF EXISTS conversation_members;
DROP TABLE IF EXISTS conversations;
DROP TYPE IF EXISTS conversation_type;
DROP TABLE IF EXISTS follows;
DROP TABLE IF EXISTS likes;
DROP TYPE IF EXISTS likeable_type;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS post_hashtags;
DROP TABLE IF EXISTS post_media;
DROP TYPE IF EXISTS media_type;
DROP TABLE IF EXISTS posts;
DROP TYPE IF EXISTS post_visibility;
DROP TYPE IF EXISTS post_status;
DROP TABLE IF EXISTS hashtags;
DROP TABLE IF EXISTS page_followers;
DROP TABLE IF EXISTS enterprise_pages;
DROP TABLE IF EXISTS users;
-- +goose StatementEnd
