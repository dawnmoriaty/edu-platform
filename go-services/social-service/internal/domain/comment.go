package domain

import (
	"time"

	"github.com/google/uuid"
)

// Comment represents a comment on a post
type Comment struct {
	ID        uuid.UUID `json:"id"`
	PostID    uuid.UUID `json:"post_id"`
	UserID    uuid.UUID `json:"user_id"`
	Content   string    `json:"content"`
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
}

// NewComment creates a new Comment
func NewComment(postID, userID uuid.UUID, content string) *Comment {
	now := time.Now()
	return &Comment{
		ID:        uuid.New(),
		PostID:    postID,
		UserID:    userID,
		Content:   content,
		CreatedAt: now,
		UpdatedAt: now,
	}
}
