package repository

import (
	"context"
	"time"

	db "github.com/eduplatform/go-services/social-service/db/sqlc"
	"github.com/jackc/pgx/v5/pgtype"
	"github.com/jackc/pgx/v5/pgxpool"
)

type LikeRepository struct {
	queries *db.Queries
	pool    *pgxpool.Pool
}

func NewLikeRepository(pool *pgxpool.Pool) *LikeRepository {
	return &LikeRepository{
		queries: db.New(pool),
		pool:    pool,
	}
}

func (r *LikeRepository) Create(ctx context.Context, postID, userID pgtype.UUID) (db.Like, error) {
	now := pgtype.Timestamp{Time: time.Now(), Valid: true}
	return r.queries.CreateLike(ctx, db.CreateLikeParams{
		PostID:    postID,
		UserID:    userID,
		CreatedAt: now,
	})
}

func (r *LikeRepository) GetByPostID(ctx context.Context, postID pgtype.UUID) ([]db.Like, error) {
	return r.queries.GetLikesByPostID(ctx, postID)
}

func (r *LikeRepository) GetByUserIDAndPostID(ctx context.Context, userID, postID pgtype.UUID) (db.Like, error) {
	return r.queries.GetLikeByUserIDAndPostID(ctx, db.GetLikeByUserIDAndPostIDParams{
		UserID: userID,
		PostID: postID,
	})
}

func (r *LikeRepository) Delete(ctx context.Context, userID, postID pgtype.UUID) error {
	return r.queries.DeleteLike(ctx, db.DeleteLikeParams{
		UserID: userID,
		PostID: postID,
	})
}

func (r *LikeRepository) CountByPostID(ctx context.Context, postID pgtype.UUID) (int64, error) {
	return r.queries.CountLikesByPostID(ctx, postID)
}
