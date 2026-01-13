-- name: CreateHashtag :one
INSERT INTO hashtags (name) VALUES ($1)
ON CONFLICT (name) DO UPDATE SET post_count = hashtags.post_count
RETURNING *;

-- name: GetHashtagByName :one
SELECT * FROM hashtags WHERE name = $1;

-- name: GetTrendingHashtags :many
SELECT * FROM hashtags ORDER BY post_count DESC LIMIT $1;

-- name: LinkPostHashtag :exec
INSERT INTO post_hashtags (post_id, hashtag_id) VALUES ($1, $2)
ON CONFLICT DO NOTHING;

-- name: GetHashtagsByPostID :many
SELECT h.* FROM hashtags h
JOIN post_hashtags ph ON h.id = ph.hashtag_id
WHERE ph.post_id = $1;

-- name: IncrementHashtagCount :exec
UPDATE hashtags SET post_count = post_count + 1 WHERE id = $1;

-- name: DecrementHashtagCount :exec
UPDATE hashtags SET post_count = GREATEST(post_count - 1, 0) WHERE id = $1;

-- name: SearchHashtags :many
SELECT * FROM hashtags WHERE name ILIKE '%' || $1 || '%' ORDER BY post_count DESC LIMIT $2;
