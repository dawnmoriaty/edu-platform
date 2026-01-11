package repository

import (
	"context"
	"time"

	db "github.com/eduplatform/go-services/social-service/db/sqlc"
	"github.com/jackc/pgx/v5/pgtype"
	"github.com/jackc/pgx/v5/pgxpool"
)

type FollowRepository struct {
	queries *db.Queries
	pool    *pgxpool.Pool
}

func NewFollowRepository(pool *pgxpool.Pool) *FollowRepository {
	return &FollowRepository{
		queries: db.New(pool),
		pool:    pool,
	}
}

func (r *FollowRepository) Create(ctx context.Context, followerID, followeeID pgtype.UUID) (db.Follow, error) {
	now := pgtype.Timestamp{Time: time.Now(), Valid: true}
	return r.queries.CreateFollow(ctx, db.CreateFollowParams{
		FollowerID: followerID,
		FolloweeID: followeeID,
		CreatedAt:  now,
	})
}

func (r *FollowRepository) GetByFollowerID(ctx context.Context, followerID pgtype.UUID, limit, offset int32) ([]db.Follow, error) {
	return r.queries.GetFollowsByFollowerID(ctx, db.GetFollowsByFollowerIDParams{
		FollowerID: followerID,
		Lim:        limit,
		Off:        offset,
	})
}

func (r *FollowRepository) GetByFolloweeID(ctx context.Context, followeeID pgtype.UUID, limit, offset int32) ([]db.Follow, error) {
	return r.queries.GetFollowsByFolloweeID(ctx, db.GetFollowsByFolloweeIDParams{
		FolloweeID: followeeID,
		Lim:        limit,
		Off:        offset,
	})
}

func (r *FollowRepository) GetByIDs(ctx context.Context, followerID, followeeID pgtype.UUID) (db.Follow, error) {
	return r.queries.GetFollowByIDs(ctx, db.GetFollowByIDsParams{
		FollowerID: followerID,
		FolloweeID: followeeID,
	})
}

func (r *FollowRepository) Delete(ctx context.Context, followerID, followeeID pgtype.UUID) error {
	return r.queries.DeleteFollow(ctx, db.DeleteFollowParams{
		FollowerID: followerID,
		FolloweeID: followeeID,
	})
}

func (r *FollowRepository) CountFollowers(ctx context.Context, followeeID pgtype.UUID) (int64, error) {
	return r.queries.CountFollowers(ctx, followeeID)
}

func (r *FollowRepository) CountFollowing(ctx context.Context, followerID pgtype.UUID) (int64, error) {
	return r.queries.CountFollowing(ctx, followerID)
}
