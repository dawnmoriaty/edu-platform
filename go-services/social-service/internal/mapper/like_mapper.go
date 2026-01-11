package mapper

import (
	db "github.com/eduplatform/go-services/social-service/db/sqlc"
	"github.com/eduplatform/go-services/social-service/internal/domain"
	"github.com/google/uuid"
)

// LikeMapper handles mapping between db.Like and domain.Like
type LikeMapper struct{}

func NewLikeMapper() *LikeMapper {
	return &LikeMapper{}
}

// ToDomain converts db.Like to domain.Like
func (m *LikeMapper) ToDomain(l db.Like) *domain.Like {
	return &domain.Like{
		ID:        uuid.UUID(l.ID.Bytes),
		PostID:    uuid.UUID(l.PostID.Bytes),
		UserID:    uuid.UUID(l.UserID.Bytes),
		CreatedAt: l.CreatedAt.Time,
	}
}

// ToDomainList converts a slice of db.Like to domain.Like
func (m *LikeMapper) ToDomainList(likes []db.Like) []*domain.Like {
	result := make([]*domain.Like, len(likes))
	for i, l := range likes {
		result[i] = m.ToDomain(l)
	}
	return result
}
