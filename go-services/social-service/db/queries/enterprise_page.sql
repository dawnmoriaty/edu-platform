-- name: CreateEnterprisePage :one
INSERT INTO enterprise_pages (user_id, name, slug, logo_url, cover_url, description, website, industry, location)
VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
RETURNING *;

-- name: GetEnterprisePageByID :one
SELECT * FROM enterprise_pages WHERE id = $1;

-- name: GetEnterprisePageBySlug :one
SELECT * FROM enterprise_pages WHERE slug = $1;

-- name: GetEnterprisePagesByUserID :many
SELECT * FROM enterprise_pages WHERE user_id = $1 ORDER BY created_at DESC;

-- name: UpdateEnterprisePage :exec
UPDATE enterprise_pages SET
    name = COALESCE($2, name),
    logo_url = COALESCE($3, logo_url),
    cover_url = COALESCE($4, cover_url),
    description = COALESCE($5, description),
    website = COALESCE($6, website),
    industry = COALESCE($7, industry),
    location = COALESCE($8, location),
    updated_at = NOW()
WHERE id = $1;

-- name: DeleteEnterprisePage :exec
DELETE FROM enterprise_pages WHERE id = $1;

-- name: SearchEnterprisePages :many
SELECT * FROM enterprise_pages
WHERE name ILIKE '%' || $1 || '%' OR industry ILIKE '%' || $1 || '%'
ORDER BY follower_count DESC
LIMIT $2 OFFSET $3;

-- name: IncrementPageFollowerCount :exec
UPDATE enterprise_pages SET follower_count = follower_count + 1 WHERE id = $1;

-- name: DecrementPageFollowerCount :exec
UPDATE enterprise_pages SET follower_count = GREATEST(follower_count - 1, 0) WHERE id = $1;

-- name: FollowPage :one
INSERT INTO page_followers (page_id, user_id)
VALUES ($1, $2)
RETURNING *;

-- name: UnfollowPage :exec
DELETE FROM page_followers WHERE page_id = $1 AND user_id = $2;

-- name: GetPageFollowers :many
SELECT u.* FROM users u
JOIN page_followers pf ON u.id = pf.user_id
WHERE pf.page_id = $1
ORDER BY pf.created_at DESC
LIMIT $2 OFFSET $3;

-- name: IsFollowingPage :one
SELECT EXISTS(SELECT 1 FROM page_followers WHERE page_id = $1 AND user_id = $2);

-- name: CountPageFollowers :one
SELECT COUNT(*) FROM page_followers WHERE page_id = $1;
