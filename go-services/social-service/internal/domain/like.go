package domain

import (
	"time"

	"github.com/google/uuid"
)

// Like represents a like on a post
type Like struct {
	ID        uuid.UUID `json:"id"`
	PostID    uuid.UUID `json:"post_id"`
	UserID    uuid.UUID `json:"user_id"`
	CreatedAt time.Time `json:"created_at"`
}

// NewLike creates a new Like
func NewLike(postID, userID uuid.UUID) *Like {
	return &Like{
		ID:        uuid.New(),
		PostID:    postID,
		UserID:    userID,
		CreatedAt: time.Now(),
	}
}
