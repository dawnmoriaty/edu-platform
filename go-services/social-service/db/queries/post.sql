-- name: CreatePost :one
INSERT INTO posts (user_id, content, created_at, updated_at)
VALUES ($1, $2, $3, $4)
RETURNING *;

-- name: GetPostByID :one
SELECT * FROM posts WHERE id = sqlc.arg(id)::uuid;

-- name: GetPostsByUserID :many
SELECT * FROM posts
WHERE user_id = $1
ORDER BY created_at DESC
LIMIT $2 OFFSET $3;

-- name: UpdatePost :exec
UPDATE posts
SET content = sqlc.arg(content), updated_at = sqlc.arg(updated_at)
WHERE id = sqlc.arg(id)::uuid;

-- name: DeletePost :exec
DELETE FROM posts WHERE id = sqlc.arg(id)::uuid;
