-- name: CreateFileUpload :one
INSERT INTO file_uploads (user_id, original_name, stored_name, mime_type, file_size, url, thumbnail_url, is_public)
VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
RETURNING *;

-- name: GetFileUploadByID :one
SELECT * FROM file_uploads WHERE id = $1;

-- name: GetFileUploadsByUserID :many
SELECT * FROM file_uploads WHERE user_id = $1 ORDER BY created_at DESC LIMIT $2 OFFSET $3;

-- name: DeleteFileUpload :exec
DELETE FROM file_uploads WHERE id = $1;
