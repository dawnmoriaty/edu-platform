package mapper

import (
	"time"

	db "github.com/eduplatform/go-services/social-service/db/sqlc"
	"github.com/eduplatform/go-services/social-service/internal/domain"
	"github.com/google/uuid"
	"github.com/jackc/pgx/v5/pgtype"
)

// PostMapper handles mapping between db.Post and domain.Post
type PostMapper struct{}

func NewPostMapper() *PostMapper {
	return &PostMapper{}
}

// ToDomain converts db.Post to domain.Post
func (m *PostMapper) ToDomain(p db.Post) *domain.Post {
	return &domain.Post{
		ID:        uuid.UUID(p.ID.Bytes),
		UserID:    uuid.UUID(p.UserID.Bytes),
		Content:   p.Content,
		CreatedAt: p.CreatedAt.Time,
		UpdatedAt: p.UpdatedAt.Time,
	}
}

// ToDomainList converts a slice of db.Post to domain.Post
func (m *PostMapper) ToDomainList(posts []db.Post) []*domain.Post {
	result := make([]*domain.Post, len(posts))
	for i, p := range posts {
		result[i] = m.ToDomain(p)
	}
	return result
}

// ToPgUUID converts uuid.UUID to pgtype.UUID
func ToPgUUID(id uuid.UUID) pgtype.UUID {
	return pgtype.UUID{Bytes: id, Valid: true}
}

// FromPgUUID converts pgtype.UUID to uuid.UUID
func FromPgUUID(id pgtype.UUID) uuid.UUID {
	return uuid.UUID(id.Bytes)
}

// ToPgTimestamptz converts time.Time to pgtype.Timestamptz
func ToPgTimestamptz(t time.Time) pgtype.Timestamptz {
	return pgtype.Timestamptz{Time: t, Valid: true}
}

// FromPgTimestamptz converts pgtype.Timestamptz to time.Time
func FromPgTimestamptz(t pgtype.Timestamptz) time.Time {
	if !t.Valid {
		return time.Time{}
	}
	return t.Time
}

// ToNullablePgTimestamptz converts *time.Time to pgtype.Timestamptz
func ToNullablePgTimestamptz(t *time.Time) pgtype.Timestamptz {
	if t == nil {
		return pgtype.Timestamptz{Valid: false}
	}
	return pgtype.Timestamptz{Time: *t, Valid: true}
}
