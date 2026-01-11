-- name: CreateFollow :one
INSERT INTO follows (follower_id, followee_id, created_at)
VALUES ($1, $2, $3)
RETURNING *;

-- name: GetFollowsByFollowerID :many
SELECT * FROM follows
WHERE follower_id = sqlc.arg(follower_id)::uuid
ORDER BY created_at DESC
LIMIT sqlc.arg(lim) OFFSET sqlc.arg(off);

-- name: GetFollowsByFolloweeID :many
SELECT * FROM follows
WHERE followee_id = sqlc.arg(followee_id)::uuid
ORDER BY created_at DESC
LIMIT sqlc.arg(lim) OFFSET sqlc.arg(off);

-- name: GetFollowByIDs :one
SELECT * FROM follows
WHERE follower_id = sqlc.arg(follower_id)::uuid AND followee_id = sqlc.arg(followee_id)::uuid;

-- name: DeleteFollow :exec
DELETE FROM follows WHERE follower_id = sqlc.arg(follower_id)::uuid AND followee_id = sqlc.arg(followee_id)::uuid;

-- name: CountFollowers :one
SELECT COUNT(*) FROM follows WHERE followee_id = sqlc.arg(followee_id)::uuid;

-- name: CountFollowing :one
SELECT COUNT(*) FROM follows WHERE follower_id = sqlc.arg(follower_id)::uuid;
