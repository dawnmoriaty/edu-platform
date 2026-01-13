package entity

import (
	"time"
)

type Post struct {
	ID        string    `json:"id"`
	UserID    string    `json:"user_id"`
	Content   string    `json:"content"`
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`

	// Computed fields (not in DB)
	LikeCount    int  `json:"like_count,omitempty"`
	CommentCount int  `json:"comment_count,omitempty"`
	IsLiked      bool `json:"is_liked,omitempty"`
}

type PostWithStats struct {
	Post
	AuthorName   string `json:"author_name,omitempty"`
	AuthorAvatar string `json:"author_avatar,omitempty"`
}
