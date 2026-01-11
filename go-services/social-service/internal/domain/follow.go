package domain

import (
	"time"

	"github.com/google/uuid"
)

// Follow represents a follow relationship between users
type Follow struct {
	ID         uuid.UUID `json:"id"`
	FollowerID uuid.UUID `json:"follower_id"`
	FolloweeID uuid.UUID `json:"followee_id"`
	CreatedAt  time.Time `json:"created_at"`
}

// NewFollow creates a new Follow relationship
func NewFollow(followerID, followeeID uuid.UUID) *Follow {
	return &Follow{
		ID:         uuid.New(),
		FollowerID: followerID,
		FolloweeID: followeeID,
		CreatedAt:  time.Now(),
	}
}
