package mapper

import (
	db "github.com/eduplatform/go-services/social-service/db/sqlc"
	"github.com/eduplatform/go-services/social-service/internal/domain"
	"github.com/google/uuid"
)

// FollowMapper handles mapping between db.Follow and domain.Follow
type FollowMapper struct{}

func NewFollowMapper() *FollowMapper {
	return &FollowMapper{}
}

// ToDomain converts db.Follow to domain.Follow
func (m *FollowMapper) ToDomain(f db.Follow) *domain.Follow {
	return &domain.Follow{
		ID:         uuid.UUID(f.ID.Bytes),
		FollowerID: uuid.UUID(f.FollowerID.Bytes),
		FolloweeID: uuid.UUID(f.FolloweeID.Bytes),
		CreatedAt:  f.CreatedAt.Time,
	}
}

// ToDomainList converts a slice of db.Follow to domain.Follow
func (m *FollowMapper) ToDomainList(follows []db.Follow) []*domain.Follow {
	result := make([]*domain.Follow, len(follows))
	for i, f := range follows {
		result[i] = m.ToDomain(f)
	}
	return result
}
