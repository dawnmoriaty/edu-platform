-- name: CreateConversation :one
INSERT INTO conversations (type, name, avatar_url, created_by)
VALUES ($1, $2, $3, $4)
RETURNING *;

-- name: GetConversationByID :one
SELECT * FROM conversations WHERE id = $1;

-- name: GetUserConversations :many
SELECT c.*, cm.last_read_at,
    (SELECT COUNT(*) FROM messages m WHERE m.conversation_id = c.id AND m.created_at > COALESCE(cm.last_read_at, '1970-01-01')) as unread_count
FROM conversations c
JOIN conversation_members cm ON c.id = cm.conversation_id
WHERE cm.user_id = $1
ORDER BY c.last_message_at DESC NULLS LAST
LIMIT $2 OFFSET $3;

-- name: GetDirectConversation :one
SELECT c.* FROM conversations c
JOIN conversation_members cm1 ON c.id = cm1.conversation_id
JOIN conversation_members cm2 ON c.id = cm2.conversation_id
WHERE c.type = 'DIRECT' AND cm1.user_id = $1 AND cm2.user_id = $2;

-- name: UpdateConversationLastMessage :exec
UPDATE conversations SET last_message_at = NOW(), updated_at = NOW() WHERE id = $1;

-- name: AddConversationMember :one
INSERT INTO conversation_members (conversation_id, user_id, role)
VALUES ($1, $2, $3)
RETURNING *;

-- name: RemoveConversationMember :exec
DELETE FROM conversation_members WHERE conversation_id = $1 AND user_id = $2;

-- name: GetConversationMembers :many
SELECT u.*, cm.role, cm.last_read_at FROM users u
JOIN conversation_members cm ON u.id = cm.user_id
WHERE cm.conversation_id = $1;

-- name: UpdateLastReadAt :exec
UPDATE conversation_members SET last_read_at = NOW()
WHERE conversation_id = $1 AND user_id = $2;

-- name: IsConversationMember :one
SELECT EXISTS(SELECT 1 FROM conversation_members WHERE conversation_id = $1 AND user_id = $2);
