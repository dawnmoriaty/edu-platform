-- name: CreatePostMedia :one
INSERT INTO post_media (post_id, media_type, url, thumbnail_url, width, height, file_size, sort_order)
VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
RETURNING *;

-- name: GetMediaByPostID :many
SELECT * FROM post_media WHERE post_id = $1 ORDER BY sort_order;

-- name: DeleteMediaByPostID :exec
DELETE FROM post_media WHERE post_id = $1;
