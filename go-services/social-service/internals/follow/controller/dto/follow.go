package dto

type FollowRequest struct {
	UserID string `json:"user_id" binding:"required"`
}

type UnfollowRequest struct {
	UserID string `json:"user_id" binding:"required"`
}

type FollowResponse struct {
	ID         string `json:"id"`
	FollowerID string `json:"follower_id"`
	FolloweeID string `json:"followee_id"`
	CreatedAt  string `json:"created_at"`
}

type FollowStatsResponse struct {
	UserID         string `json:"user_id,omitempty"`
	FollowersCount int64  `json:"followers_count"`
	FollowingCount int64  `json:"following_count"`
	IsFollowing    bool   `json:"is_following,omitempty"`
}

type IsFollowingResponse struct {
	IsFollowing bool `json:"is_following"`
}
