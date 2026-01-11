package application

import (
	"context"
	"errors"

	"github.com/eduplatform/go-services/social-service/internal/domain"
	"github.com/eduplatform/go-services/social-service/internal/infrastructure/repository"
	"github.com/eduplatform/go-services/social-service/internal/mapper"
	"github.com/eduplatform/go-services/social-service/internal/paging"
	"github.com/google/uuid"
)

type FollowService struct {
	followRepo   *repository.FollowRepository
	followMapper *mapper.FollowMapper
}

func NewFollowService(followRepo *repository.FollowRepository) *FollowService {
	return &FollowService{
		followRepo:   followRepo,
		followMapper: mapper.NewFollowMapper(),
	}
}

func (s *FollowService) FollowUser(ctx context.Context, followerID, followeeID uuid.UUID) (*domain.Follow, error) {
	if followerID == followeeID {
		return nil, errors.New("cannot follow yourself")
	}

	pgFollowerID := mapper.ToPgUUID(followerID)
	pgFolloweeID := mapper.ToPgUUID(followeeID)

	// Check if already following
	_, err := s.followRepo.GetByIDs(ctx, pgFollowerID, pgFolloweeID)
	if err == nil {
		return nil, errors.New("already following this user")
	}

	follow, err := s.followRepo.Create(ctx, pgFollowerID, pgFolloweeID)
	if err != nil {
		return nil, err
	}
	return s.followMapper.ToDomain(follow), nil
}

func (s *FollowService) UnfollowUser(ctx context.Context, followerID, followeeID uuid.UUID) error {
	return s.followRepo.Delete(ctx, mapper.ToPgUUID(followerID), mapper.ToPgUUID(followeeID))
}

func (s *FollowService) GetFollowers(ctx context.Context, userID uuid.UUID, pageable *paging.Pageable) (*paging.Page[*domain.Follow], error) {
	follows, err := s.followRepo.GetByFolloweeID(ctx, mapper.ToPgUUID(userID), int32(pageable.Limit), int32(pageable.GetOffset()))
	if err != nil {
		return nil, err
	}

	items := s.followMapper.ToDomainList(follows)
	return paging.NewPage(pageable, items), nil
}

func (s *FollowService) GetFollowing(ctx context.Context, userID uuid.UUID, pageable *paging.Pageable) (*paging.Page[*domain.Follow], error) {
	follows, err := s.followRepo.GetByFollowerID(ctx, mapper.ToPgUUID(userID), int32(pageable.Limit), int32(pageable.GetOffset()))
	if err != nil {
		return nil, err
	}

	items := s.followMapper.ToDomainList(follows)
	return paging.NewPage(pageable, items), nil
}

func (s *FollowService) GetFollowerCount(ctx context.Context, userID uuid.UUID) (int64, error) {
	return s.followRepo.CountFollowers(ctx, mapper.ToPgUUID(userID))
}

func (s *FollowService) GetFollowingCount(ctx context.Context, userID uuid.UUID) (int64, error) {
	return s.followRepo.CountFollowing(ctx, mapper.ToPgUUID(userID))
}

func (s *FollowService) IsFollowing(ctx context.Context, followerID, followeeID uuid.UUID) bool {
	_, err := s.followRepo.GetByIDs(ctx, mapper.ToPgUUID(followerID), mapper.ToPgUUID(followeeID))
	return err == nil
}
