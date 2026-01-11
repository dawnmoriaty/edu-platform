-- name: CreateLike :one
INSERT INTO likes (post_id, user_id, created_at)
VALUES ($1, $2, $3)
RETURNING *;

-- name: GetLikesByPostID :many
SELECT * FROM likes
WHERE post_id = sqlc.arg(post_id)::uuid
ORDER BY created_at DESC;

-- name: GetLikeByUserIDAndPostID :one
SELECT * FROM likes
WHERE user_id = sqlc.arg(user_id)::uuid AND post_id = sqlc.arg(post_id)::uuid;

-- name: DeleteLike :exec
DELETE FROM likes WHERE user_id = sqlc.arg(user_id)::uuid AND post_id = sqlc.arg(post_id)::uuid;

-- name: CountLikesByPostID :one
SELECT COUNT(*) FROM likes WHERE post_id = sqlc.arg(post_id)::uuid;
