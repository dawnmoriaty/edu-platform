-- name: CreateNotification :one
INSERT INTO notifications (user_id, actor_id, type, reference_id, content)
VALUES ($1, $2, $3, $4, $5)
RETURNING *;

-- name: GetNotificationsByUserID :many
SELECT n.*, u.username as actor_username, u.avatar_url as actor_avatar
FROM notifications n
LEFT JOIN users u ON n.actor_id = u.id
WHERE n.user_id = $1
ORDER BY n.created_at DESC
LIMIT $2 OFFSET $3;

-- name: MarkNotificationAsRead :exec
UPDATE notifications SET is_read = TRUE WHERE id = $1;

-- name: MarkAllNotificationsAsRead :exec
UPDATE notifications SET is_read = TRUE WHERE user_id = $1;

-- name: CountUnreadNotifications :one
SELECT COUNT(*) FROM notifications WHERE user_id = $1 AND is_read = FALSE;

-- name: DeleteNotification :exec
DELETE FROM notifications WHERE id = $1;
