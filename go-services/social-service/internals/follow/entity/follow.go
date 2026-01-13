package entity

import "time"

type Follow struct {
	ID         string    `json:"id"`
	FollowerID string    `json:"follower_id"`
	FolloweeID string    `json:"followee_id"`
	CreatedAt  time.Time `json:"created_at"`
}

type FollowStats struct {
	UserID         string `json:"user_id"`
	FollowersCount int64  `json:"followers_count"`
	FollowingCount int64  `json:"following_count"`
}
