package mapper

import (
	db "github.com/eduplatform/go-services/social-service/db/sqlc"
	"github.com/eduplatform/go-services/social-service/internal/domain"
	"github.com/google/uuid"
)

// CommentMapper handles mapping between db.Comment and domain.Comment
type CommentMapper struct{}

func NewCommentMapper() *CommentMapper {
	return &CommentMapper{}
}

// ToDomain converts db.Comment to domain.Comment
func (m *CommentMapper) ToDomain(c db.Comment) *domain.Comment {
	return &domain.Comment{
		ID:        uuid.UUID(c.ID.Bytes),
		PostID:    uuid.UUID(c.PostID.Bytes),
		UserID:    uuid.UUID(c.UserID.Bytes),
		Content:   c.Content,
		CreatedAt: c.CreatedAt.Time,
		UpdatedAt: c.UpdatedAt.Time,
	}
}

// ToDomainList converts a slice of db.Comment to domain.Comment
func (m *CommentMapper) ToDomainList(comments []db.Comment) []*domain.Comment {
	result := make([]*domain.Comment, len(comments))
	for i, c := range comments {
		result[i] = m.ToDomain(c)
	}
	return result
}
