package usecase

import (
	"context"
	"errors"

	"social-service/internals/like/entity"
	"social-service/internals/like/repository"
)

var (
	ErrAlreadyLiked = errors.New("already liked this post")
	ErrNotLiked     = errors.New("haven't liked this post")
)

type ILikeUseCase interface {
	LikePost(ctx context.Context, userID, postID string) (*entity.Like, error)
	UnlikePost(ctx context.Context, userID, postID string) error
	GetLikeStatus(ctx context.Context, userID, postID string) (bool, int64, error)
	GetLikeCount(ctx context.Context, postID string) (int64, error)
}

type LikeUseCase struct {
	likeRepo repository.ILikeRepository
}

func NewLikeUseCase(likeRepo repository.ILikeRepository) *LikeUseCase {
	return &LikeUseCase{likeRepo: likeRepo}
}

func (uc *LikeUseCase) LikePost(ctx context.Context, userID, postID string) (*entity.Like, error) {
	// Check if already liked
	isLiked, err := uc.likeRepo.IsLiked(ctx, userID, postID)
	if err != nil {
		return nil, err
	}
	if isLiked {
		return nil, ErrAlreadyLiked
	}

	return uc.likeRepo.Create(ctx, postID, userID)
}

func (uc *LikeUseCase) UnlikePost(ctx context.Context, userID, postID string) error {
	isLiked, err := uc.likeRepo.IsLiked(ctx, userID, postID)
	if err != nil {
		return err
	}
	if !isLiked {
		return ErrNotLiked
	}

	return uc.likeRepo.Delete(ctx, postID, userID)
}

func (uc *LikeUseCase) GetLikeStatus(ctx context.Context, userID, postID string) (bool, int64, error) {
	isLiked, _ := uc.likeRepo.IsLiked(ctx, userID, postID)
	count, err := uc.likeRepo.CountByPostID(ctx, postID)
	if err != nil {
		return false, 0, err
	}
	return isLiked, count, nil
}

func (uc *LikeUseCase) GetLikeCount(ctx context.Context, postID string) (int64, error) {
	return uc.likeRepo.CountByPostID(ctx, postID)
}
