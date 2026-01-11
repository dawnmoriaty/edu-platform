package application

import (
	"context"
	"errors"

	"github.com/eduplatform/go-services/social-service/internal/domain"
	"github.com/eduplatform/go-services/social-service/internal/infrastructure/repository"
	"github.com/eduplatform/go-services/social-service/internal/mapper"
	"github.com/google/uuid"
)

type LikeService struct {
	likeRepo   *repository.LikeRepository
	postRepo   *repository.PostRepository
	likeMapper *mapper.LikeMapper
}

func NewLikeService(likeRepo *repository.LikeRepository, postRepo *repository.PostRepository) *LikeService {
	return &LikeService{
		likeRepo:   likeRepo,
		postRepo:   postRepo,
		likeMapper: mapper.NewLikeMapper(),
	}
}

func (s *LikeService) LikePost(ctx context.Context, postID, userID uuid.UUID) (*domain.Like, error) {
	pgPostID := mapper.ToPgUUID(postID)
	pgUserID := mapper.ToPgUUID(userID)

	// Verify post exists
	_, err := s.postRepo.GetByID(ctx, pgPostID)
	if err != nil {
		return nil, errors.New("post not found")
	}

	// Check if already liked
	_, err = s.likeRepo.GetByUserIDAndPostID(ctx, pgUserID, pgPostID)
	if err == nil {
		return nil, errors.New("post already liked")
	}

	like, err := s.likeRepo.Create(ctx, pgPostID, pgUserID)
	if err != nil {
		return nil, err
	}
	return s.likeMapper.ToDomain(like), nil
}

func (s *LikeService) UnlikePost(ctx context.Context, postID, userID uuid.UUID) error {
	return s.likeRepo.Delete(ctx, mapper.ToPgUUID(userID), mapper.ToPgUUID(postID))
}

func (s *LikeService) GetPostLikes(ctx context.Context, postID uuid.UUID) ([]*domain.Like, error) {
	likes, err := s.likeRepo.GetByPostID(ctx, mapper.ToPgUUID(postID))
	if err != nil {
		return nil, err
	}
	return s.likeMapper.ToDomainList(likes), nil
}

func (s *LikeService) GetLikeCount(ctx context.Context, postID uuid.UUID) (int64, error) {
	return s.likeRepo.CountByPostID(ctx, mapper.ToPgUUID(postID))
}

func (s *LikeService) IsLiked(ctx context.Context, postID, userID uuid.UUID) bool {
	_, err := s.likeRepo.GetByUserIDAndPostID(ctx, mapper.ToPgUUID(userID), mapper.ToPgUUID(postID))
	return err == nil
}
