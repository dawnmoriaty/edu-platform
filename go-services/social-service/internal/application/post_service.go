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

type PostService struct {
	postRepo   *repository.PostRepository
	postMapper *mapper.PostMapper
}

func NewPostService(postRepo *repository.PostRepository) *PostService {
	return &PostService{
		postRepo:   postRepo,
		postMapper: mapper.NewPostMapper(),
	}
}

func (s *PostService) CreatePost(ctx context.Context, userID uuid.UUID, content string) (*domain.Post, error) {
	if content == "" {
		return nil, errors.New("content cannot be empty")
	}
	post, err := s.postRepo.Create(ctx, mapper.ToPgUUID(userID), content)
	if err != nil {
		return nil, err
	}
	return s.postMapper.ToDomain(post), nil
}

func (s *PostService) GetPost(ctx context.Context, id uuid.UUID) (*domain.Post, error) {
	post, err := s.postRepo.GetByID(ctx, mapper.ToPgUUID(id))
	if err != nil {
		return nil, err
	}
	return s.postMapper.ToDomain(post), nil
}

func (s *PostService) GetUserPosts(ctx context.Context, userID uuid.UUID, pageable *paging.Pageable) (*paging.Page[*domain.Post], error) {
	posts, err := s.postRepo.GetByUserID(ctx, mapper.ToPgUUID(userID), int32(pageable.Limit), int32(pageable.GetOffset()))
	if err != nil {
		return nil, err
	}

	// TODO: Get total count from repository
	items := s.postMapper.ToDomainList(posts)
	return paging.NewPage(pageable, items), nil
}

func (s *PostService) UpdatePost(ctx context.Context, id, userID uuid.UUID, content string) error {
	pgID := mapper.ToPgUUID(id)
	pgUserID := mapper.ToPgUUID(userID)

	post, err := s.postRepo.GetByID(ctx, pgID)
	if err != nil {
		return err
	}

	if post.UserID != pgUserID {
		return errors.New("unauthorized: cannot update other user's post")
	}

	return s.postRepo.Update(ctx, pgID, content)
}

func (s *PostService) DeletePost(ctx context.Context, id, userID uuid.UUID) error {
	pgID := mapper.ToPgUUID(id)
	pgUserID := mapper.ToPgUUID(userID)

	post, err := s.postRepo.GetByID(ctx, pgID)
	if err != nil {
		return err
	}

	if post.UserID != pgUserID {
		return errors.New("unauthorized: cannot delete other user's post")
	}

	return s.postRepo.Delete(ctx, pgID)
}
