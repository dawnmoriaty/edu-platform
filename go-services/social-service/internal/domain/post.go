package domain

import (
	"time"

	"github.com/google/uuid"
)

// Post represents a social post in the domain layer
type Post struct {
	ID        uuid.UUID `json:"id"`
	UserID    uuid.UUID `json:"user_id"`
	Content   string    `json:"content"`
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
}

// NewPost creates a new Post
func NewPost(userID uuid.UUID, content string) *Post {
	now := time.Now()
	return &Post{
		ID:        uuid.New(),
		UserID:    userID,
		Content:   content,
		CreatedAt: now,
		UpdatedAt: now,
	}
}
