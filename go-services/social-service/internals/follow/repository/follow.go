package repository

import (
	"context"
	"time"

	"github.com/jackc/pgx/v5/pgtype"
	"github.com/jackc/pgx/v5/pgxpool"

	db "social-service/db/sqlc"
	"social-service/internals/follow/entity"
	"social-service/pkgs/paging"
	"social-service/utils"
)

type IFollowRepository interface {
	Create(ctx context.Context, followerID, followeeID string) (*entity.Follow, error)
	Delete(ctx context.Context, followerID, followeeID string) error
	GetByIDs(ctx context.Context, followerID, followeeID string) (*entity.Follow, error)
	GetFollowers(ctx context.Context, userID string, pageable *paging.Pageable) (*paging.Page[string], error)
	GetFollowing(ctx context.Context, userID string, pageable *paging.Pageable) (*paging.Page[string], error)
	CountFollowers(ctx context.Context, userID string) (int64, error)
	CountFollowing(ctx context.Context, userID string) (int64, error)
	IsFollowing(ctx context.Context, followerID, followeeID string) (bool, error)
}

type FollowRepository struct {
	pool    *pgxpool.Pool
	queries *db.Queries
}

func NewFollowRepository(pool *pgxpool.Pool, queries *db.Queries) *FollowRepository {
	return &FollowRepository{
		pool:    pool,
		queries: queries,
	}
}

func (r *FollowRepository) Create(ctx context.Context, followerID, followeeID string) (*entity.Follow, error) {
	followerUUID, err := utils.StringToUUID(followerID)
	if err != nil {
		return nil, err
	}
	followeeUUID, err := utils.StringToUUID(followeeID)
	if err != nil {
		return nil, err
	}

	follow, err := r.queries.CreateFollow(ctx, db.CreateFollowParams{
		FollowerID: followerUUID,
		FolloweeID: followeeUUID,
		CreatedAt:  pgtype.Timestamp{Time: time.Now(), Valid: true},
	})
	if err != nil {
		return nil, err
	}

	return dbFollowToEntity(follow), nil
}

func (r *FollowRepository) Delete(ctx context.Context, followerID, followeeID string) error {
	followerUUID, err := utils.StringToUUID(followerID)
	if err != nil {
		return err
	}
	followeeUUID, err := utils.StringToUUID(followeeID)
	if err != nil {
		return err
	}

	return r.queries.DeleteFollow(ctx, db.DeleteFollowParams{
		FollowerID: followerUUID,
		FolloweeID: followeeUUID,
	})
}

func (r *FollowRepository) GetByIDs(ctx context.Context, followerID, followeeID string) (*entity.Follow, error) {
	followerUUID, err := utils.StringToUUID(followerID)
	if err != nil {
		return nil, err
	}
	followeeUUID, err := utils.StringToUUID(followeeID)
	if err != nil {
		return nil, err
	}

	follow, err := r.queries.GetFollowByIDs(ctx, db.GetFollowByIDsParams{
		FollowerID: followerUUID,
		FolloweeID: followeeUUID,
	})
	if err != nil {
		return nil, err
	}

	return dbFollowToEntity(follow), nil
}

func (r *FollowRepository) GetFollowers(ctx context.Context, userID string, pageable *paging.Pageable) (*paging.Page[string], error) {
	userUUID, err := utils.StringToUUID(userID)
	if err != nil {
		return nil, err
	}

	total, err := r.CountFollowers(ctx, userID)
	if err != nil {
		return nil, err
	}
	pageable.SetTotal(total)

	rows, err := r.pool.Query(ctx,
		`SELECT follower_id FROM follows WHERE followee_id = $1 ORDER BY created_at DESC LIMIT $2 OFFSET $3`,
		userUUID, pageable.Limit, pageable.GetOffset())
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var followers []string
	for rows.Next() {
		var followerID pgtype.UUID
		if err := rows.Scan(&followerID); err != nil {
			return nil, err
		}
		followers = append(followers, utils.UUIDToString(followerID))
	}

	return paging.NewPage(pageable, followers), nil
}

func (r *FollowRepository) GetFollowing(ctx context.Context, userID string, pageable *paging.Pageable) (*paging.Page[string], error) {
	userUUID, err := utils.StringToUUID(userID)
	if err != nil {
		return nil, err
	}

	total, err := r.CountFollowing(ctx, userID)
	if err != nil {
		return nil, err
	}
	pageable.SetTotal(total)

	rows, err := r.pool.Query(ctx,
		`SELECT followee_id FROM follows WHERE follower_id = $1 ORDER BY created_at DESC LIMIT $2 OFFSET $3`,
		userUUID, pageable.Limit, pageable.GetOffset())
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var following []string
	for rows.Next() {
		var followeeID pgtype.UUID
		if err := rows.Scan(&followeeID); err != nil {
			return nil, err
		}
		following = append(following, utils.UUIDToString(followeeID))
	}

	return paging.NewPage(pageable, following), nil
}

func (r *FollowRepository) CountFollowers(ctx context.Context, userID string) (int64, error) {
	userUUID, err := utils.StringToUUID(userID)
	if err != nil {
		return 0, err
	}

	count, err := r.queries.CountFollowers(ctx, userUUID)
	return count, err
}

func (r *FollowRepository) CountFollowing(ctx context.Context, userID string) (int64, error) {
	userUUID, err := utils.StringToUUID(userID)
	if err != nil {
		return 0, err
	}

	count, err := r.queries.CountFollowing(ctx, userUUID)
	return count, err
}

func (r *FollowRepository) IsFollowing(ctx context.Context, followerID, followeeID string) (bool, error) {
	_, err := r.GetByIDs(ctx, followerID, followeeID)
	if err != nil {
		return false, nil
	}
	return true, nil
}

func dbFollowToEntity(f db.Follow) *entity.Follow {
	return &entity.Follow{
		ID:         utils.UUIDToString(f.ID),
		FollowerID: utils.UUIDToString(f.FollowerID),
		FolloweeID: utils.UUIDToString(f.FolloweeID),
		CreatedAt:  f.CreatedAt.Time,
	}
}
