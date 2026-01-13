package repository

import (
	"context"
	"time"

	"github.com/jackc/pgx/v5/pgtype"

	db "social-service/db/sqlc"
	"social-service/internals/like/entity"
	"social-service/utils"
)

type ILikeRepository interface {
	Create(ctx context.Context, postID, userID string) (*entity.Like, error)
	Delete(ctx context.Context, postID, userID string) error
	GetByUserAndPost(ctx context.Context, userID, postID string) (*entity.Like, error)
	CountByPostID(ctx context.Context, postID string) (int64, error)
	IsLiked(ctx context.Context, userID, postID string) (bool, error)
}

type LikeRepository struct {
	queries *db.Queries
}

func NewLikeRepository(queries *db.Queries) *LikeRepository {
	return &LikeRepository{queries: queries}
}

func (r *LikeRepository) Create(ctx context.Context, postID, userID string) (*entity.Like, error) {
	postUUID, err := utils.StringToUUID(postID)
	if err != nil {
		return nil, err
	}
	userUUID, err := utils.StringToUUID(userID)
	if err != nil {
		return nil, err
	}

	like, err := r.queries.CreateLike(ctx, db.CreateLikeParams{
		PostID:    postUUID,
		UserID:    userUUID,
		CreatedAt: pgtype.Timestamp{Time: time.Now(), Valid: true},
	})
	if err != nil {
		return nil, err
	}

	return dbLikeToEntity(like), nil
}

func (r *LikeRepository) Delete(ctx context.Context, postID, userID string) error {
	postUUID, err := utils.StringToUUID(postID)
	if err != nil {
		return err
	}
	userUUID, err := utils.StringToUUID(userID)
	if err != nil {
		return err
	}

	return r.queries.DeleteLike(ctx, db.DeleteLikeParams{
		PostID: postUUID,
		UserID: userUUID,
	})
}

func (r *LikeRepository) GetByUserAndPost(ctx context.Context, userID, postID string) (*entity.Like, error) {
	postUUID, err := utils.StringToUUID(postID)
	if err != nil {
		return nil, err
	}
	userUUID, err := utils.StringToUUID(userID)
	if err != nil {
		return nil, err
	}

	like, err := r.queries.GetLikeByUserIDAndPostID(ctx, db.GetLikeByUserIDAndPostIDParams{
		PostID: postUUID,
		UserID: userUUID,
	})
	if err != nil {
		return nil, err
	}

	return dbLikeToEntity(like), nil
}

func (r *LikeRepository) CountByPostID(ctx context.Context, postID string) (int64, error) {
	postUUID, err := utils.StringToUUID(postID)
	if err != nil {
		return 0, err
	}
	return r.queries.CountLikesByPostID(ctx, postUUID)
}

func (r *LikeRepository) IsLiked(ctx context.Context, userID, postID string) (bool, error) {
	_, err := r.GetByUserAndPost(ctx, userID, postID)
	if err != nil {
		return false, nil // Not liked
	}
	return true, nil
}

func dbLikeToEntity(l db.Like) *entity.Like {
	return &entity.Like{
		ID:        utils.UUIDToString(l.ID),
		PostID:    utils.UUIDToString(l.PostID),
		UserID:    utils.UUIDToString(l.UserID),
		CreatedAt: l.CreatedAt.Time,
	}
}
