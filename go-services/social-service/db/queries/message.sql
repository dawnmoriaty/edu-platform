-- name: CreateMessage :one
INSERT INTO messages (conversation_id, sender_id, message_type, content, media_url)
VALUES ($1, $2, $3, $4, $5)
RETURNING *;

-- name: GetMessageByID :one
SELECT m.*, u.username, u.full_name, u.avatar_url as sender_avatar
FROM messages m
JOIN users u ON m.sender_id = u.id
WHERE m.id = $1;

-- name: GetMessagesByConversationID :many
SELECT m.*, u.username, u.full_name, u.avatar_url as sender_avatar
FROM messages m
JOIN users u ON m.sender_id = u.id
WHERE m.conversation_id = $1 AND m.is_deleted = FALSE
ORDER BY m.created_at DESC
LIMIT $2 OFFSET $3;

-- name: UpdateMessage :exec
UPDATE messages SET
    content = $2,
    is_edited = TRUE,
    updated_at = NOW()
WHERE id = $1;

-- name: DeleteMessage :exec
UPDATE messages SET is_deleted = TRUE, updated_at = NOW() WHERE id = $1;

-- name: AddMessageReaction :one
INSERT INTO message_reactions (message_id, user_id, emoji)
VALUES ($1, $2, $3)
ON CONFLICT (message_id, user_id, emoji) DO NOTHING
RETURNING *;

-- name: RemoveMessageReaction :exec
DELETE FROM message_reactions WHERE message_id = $1 AND user_id = $2 AND emoji = $3;

-- name: GetMessageReactions :many
SELECT mr.*, u.username, u.avatar_url FROM message_reactions mr
JOIN users u ON mr.user_id = u.id
WHERE mr.message_id = $1;
