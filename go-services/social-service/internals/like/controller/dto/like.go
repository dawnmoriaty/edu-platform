package dto

type LikePostRequest struct {
	PostID string `json:"post_id" binding:"required"`
}

type UnlikePostRequest struct {
	PostID string `json:"post_id" binding:"required"`
}

type LikeResponse struct {
	ID        string `json:"id"`
	PostID    string `json:"post_id"`
	UserID    string `json:"user_id"`
	CreatedAt string `json:"created_at"`
}

type LikeCountResponse struct {
	PostID string `json:"post_id"`
	Count  int64  `json:"count"`
}

type LikeStatusResponse struct {
	PostID  string `json:"post_id"`
	IsLiked bool   `json:"is_liked"`
	Count   int64  `json:"count"`
}
