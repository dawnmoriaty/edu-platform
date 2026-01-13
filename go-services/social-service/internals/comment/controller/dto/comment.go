package dto

type CreateCommentRequest struct {
	Content string `json:"content" binding:"required,min=1,max=2000"`
}

type UpdateCommentRequest struct {
	Content string `json:"content" binding:"required,min=1,max=2000"`
}

type CommentResponse struct {
	ID        string `json:"id"`
	PostID    string `json:"post_id"`
	UserID    string `json:"user_id"`
	Content   string `json:"content"`
	CreatedAt string `json:"created_at"`
	UpdatedAt string `json:"updated_at"`
}
