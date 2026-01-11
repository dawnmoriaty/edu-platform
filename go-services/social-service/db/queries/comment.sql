-- name: CreateComment :one
INSERT INTO comments (post_id, user_id, content, created_at, updated_at)
VALUES ($1, $2, $3, $4, $5)
RETURNING *;

-- name: GetCommentByID :one
SELECT * FROM comments WHERE id = sqlc.arg(id)::uuid;

-- name: GetCommentsByPostID :many
SELECT * FROM comments
WHERE post_id = $1
ORDER BY created_at ASC
LIMIT $2 OFFSET $3;

-- name: UpdateComment :exec
UPDATE comments
SET content = sqlc.arg(content), updated_at = sqlc.arg(updated_at)
WHERE id = sqlc.arg(id)::uuid;

-- name: DeleteComment :exec
DELETE FROM comments WHERE id = sqlc.arg(id)::uuid;
