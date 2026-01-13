-- name: GetUserByID :one
SELECT * FROM users WHERE id = $1;

-- name: GetUserByUsername :one
SELECT * FROM users WHERE username = $1;

-- name: GetUserByEmail :one
SELECT * FROM users WHERE email = $1;

-- name: CreateUser :one
INSERT INTO users (id, username, email, full_name, avatar_url, role)
VALUES ($1, $2, $3, $4, $5, $6)
RETURNING *;

-- name: UpdateUser :exec
UPDATE users SET
    full_name = COALESCE($2, full_name),
    avatar_url = COALESCE($3, avatar_url),
    cover_url = COALESCE($4, cover_url),
    bio = COALESCE($5, bio),
    updated_at = NOW()
WHERE id = $1;

-- name: SearchUsers :many
SELECT * FROM users
WHERE (full_name ILIKE '%' || $1 || '%' OR username ILIKE '%' || $1 || '%')
ORDER BY created_at DESC
LIMIT $2 OFFSET $3;
