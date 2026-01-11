package repository

import (
	"context"
	"time"

	db "github.com/eduplatform/go-services/social-service/db/sqlc"
	"github.com/jackc/pgx/v5/pgtype"
	"github.com/jackc/pgx/v5/pgxpool"
)

type PostRepository struct {
	queries *db.Queries
	pool    *pgxpool.Pool
}

func NewPostRepository(pool *pgxpool.Pool) *PostRepository {
	return &PostRepository{
		queries: db.New(pool),
		pool:    pool,
	}
}

func (r *PostRepository) Create(ctx context.Context, userID pgtype.UUID, content string) (db.Post, error) {
	now := pgtype.Timestamp{Time: time.Now(), Valid: true}
	return r.queries.CreatePost(ctx, db.CreatePostParams{
		UserID:    userID,
		Content:   content,
		CreatedAt: now,
		UpdatedAt: now,
	})
}

func (r *PostRepository) GetByID(ctx context.Context, id pgtype.UUID) (db.Post, error) {
	return r.queries.GetPostByID(ctx, id)
}

func (r *PostRepository) GetByUserID(ctx context.Context, userID pgtype.UUID, limit, offset int32) ([]db.Post, error) {
	return r.queries.GetPostsByUserID(ctx, db.GetPostsByUserIDParams{
		UserID: userID,
		Limit:  limit,
		Offset: offset,
	})
}

func (r *PostRepository) Update(ctx context.Context, id pgtype.UUID, content string) error {
	now := pgtype.Timestamp{Time: time.Now(), Valid: true}
	return r.queries.UpdatePost(ctx, db.UpdatePostParams{
		ID:        id,
		Content:   content,
		UpdatedAt: now,
	})
}

func (r *PostRepository) Delete(ctx context.Context, id pgtype.UUID) error {
	return r.queries.DeletePost(ctx, id)
}
