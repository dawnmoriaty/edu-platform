package usecase

import (
	"context"
	"errors"

	"social-service/internals/follow/entity"
	"social-service/internals/follow/repository"
	"social-service/pkgs/paging"
)

var (
	ErrAlreadyFollowing = errors.New("already following this user")
	ErrNotFollowing     = errors.New("not following this user")
	ErrCannotFollowSelf = errors.New("cannot follow yourself")
)

type IFollowUseCase interface {
	Follow(ctx context.Context, followerID, followeeID string) (*entity.Follow, error)
	Unfollow(ctx context.Context, followerID, followeeID string) error
	GetFollowers(ctx context.Context, userID string, pageable *paging.Pageable) (*paging.Page[string], error)
	GetFollowing(ctx context.Context, userID string, pageable *paging.Pageable) (*paging.Page[string], error)
	IsFollowing(ctx context.Context, followerID, followeeID string) (bool, error)
	GetFollowStats(ctx context.Context, userID string) (*entity.FollowStats, error)
}

type FollowUseCase struct {
	followRepo repository.IFollowRepository
}

func NewFollowUseCase(followRepo repository.IFollowRepository) *FollowUseCase {
	return &FollowUseCase{
		followRepo: followRepo,
	}
}

func (uc *FollowUseCase) Follow(ctx context.Context, followerID, followeeID string) (*entity.Follow, error) {
	if followerID == followeeID {
		return nil, ErrCannotFollowSelf
	}

	// Check if already following
	isFollowing, _ := uc.followRepo.IsFollowing(ctx, followerID, followeeID)
	if isFollowing {
		return nil, ErrAlreadyFollowing
	}

	return uc.followRepo.Create(ctx, followerID, followeeID)
}

func (uc *FollowUseCase) Unfollow(ctx context.Context, followerID, followeeID string) error {
	isFollowing, _ := uc.followRepo.IsFollowing(ctx, followerID, followeeID)
	if !isFollowing {
		return ErrNotFollowing
	}

	return uc.followRepo.Delete(ctx, followerID, followeeID)
}

func (uc *FollowUseCase) GetFollowers(ctx context.Context, userID string, pageable *paging.Pageable) (*paging.Page[string], error) {
	return uc.followRepo.GetFollowers(ctx, userID, pageable)
}

func (uc *FollowUseCase) GetFollowing(ctx context.Context, userID string, pageable *paging.Pageable) (*paging.Page[string], error) {
	return uc.followRepo.GetFollowing(ctx, userID, pageable)
}

func (uc *FollowUseCase) IsFollowing(ctx context.Context, followerID, followeeID string) (bool, error) {
	return uc.followRepo.IsFollowing(ctx, followerID, followeeID)
}

func (uc *FollowUseCase) GetFollowStats(ctx context.Context, userID string) (*entity.FollowStats, error) {
	followers, err := uc.followRepo.CountFollowers(ctx, userID)
	if err != nil {
		return nil, err
	}

	following, err := uc.followRepo.CountFollowing(ctx, userID)
	if err != nil {
		return nil, err
	}

	return &entity.FollowStats{
		FollowersCount: followers,
		FollowingCount: following,
	}, nil
}
