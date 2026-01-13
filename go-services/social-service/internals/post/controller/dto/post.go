package dto

// Request DTOs
type CreatePostRequest struct {
	Content string `json:"content" binding:"required,min=1,max=5000"`
}

type UpdatePostRequest struct {
	Content string `json:"content" binding:"required,min=1,max=5000"`
}

type ListPostsRequest struct {
	UserID  string `form:"user_id"`
	Search  string `form:"search"`
	OrderBy string `form:"order_by"`
}

// Response DTOs
type PostResponse struct {
	ID           string `json:"id"`
	UserID       string `json:"user_id"`
	Content      string `json:"content"`
	CreatedAt    string `json:"created_at"`
	UpdatedAt    string `json:"updated_at"`
	LikeCount    int    `json:"like_count"`
	CommentCount int    `json:"comment_count"`
	IsLiked      bool   `json:"is_liked"`
}
