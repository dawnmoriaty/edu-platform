package repository

import (
	"context"
	"time"

	db "github.com/eduplatform/go-services/social-service/db/sqlc"
	"github.com/jackc/pgx/v5/pgtype"
	"github.com/jackc/pgx/v5/pgxpool"
)

type CommentRepository struct {
	queries *db.Queries
	pool    *pgxpool.Pool
}

func NewCommentRepository(pool *pgxpool.Pool) *CommentRepository {
	return &CommentRepository{
		queries: db.New(pool),
		pool:    pool,
	}
}

func (r *CommentRepository) Create(ctx context.Context, postID, userID pgtype.UUID, content string) (db.Comment, error) {
	now := pgtype.Timestamp{Time: time.Now(), Valid: true}
	return r.queries.CreateComment(ctx, db.CreateCommentParams{
		PostID:    postID,
		UserID:    userID,
		Content:   content,
		CreatedAt: now,
		UpdatedAt: now,
	})
}

func (r *CommentRepository) GetByID(ctx context.Context, id pgtype.UUID) (db.Comment, error) {
	return r.queries.GetCommentByID(ctx, id)
}

func (r *CommentRepository) GetByPostID(ctx context.Context, postID pgtype.UUID, limit, offset int32) ([]db.Comment, error) {
	return r.queries.GetCommentsByPostID(ctx, db.GetCommentsByPostIDParams{
		PostID: postID,
		Limit:  limit,
		Offset: offset,
	})
}

func (r *CommentRepository) Update(ctx context.Context, id pgtype.UUID, content string) error {
	now := pgtype.Timestamp{Time: time.Now(), Valid: true}
	return r.queries.UpdateComment(ctx, db.UpdateCommentParams{
		ID:        id,
		Content:   content,
		UpdatedAt: now,
	})
}

func (r *CommentRepository) Delete(ctx context.Context, id pgtype.UUID) error {
	return r.queries.DeleteComment(ctx, id)
}
