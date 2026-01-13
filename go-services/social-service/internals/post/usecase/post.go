package usecase

import (
	"context"
	"errors"

	"social-service/internals/post/controller/dto"
	"social-service/internals/post/entity"
	"social-service/internals/post/repository"
	"social-service/pkgs/paging"
)

var (
	ErrPostNotFound   = errors.New("post not found")
	ErrUnauthorized   = errors.New("unauthorized to perform this action")
	ErrInvalidContent = errors.New("invalid content")
)

type IPostUseCase interface {
	CreatePost(ctx context.Context, userID string, req *dto.CreatePostRequest) (*entity.Post, error)
	GetPostByID(ctx context.Context, id string) (*entity.Post, error)
	GetPostsByUserID(ctx context.Context, userID string, pageable *paging.Pageable) (*paging.Page[entity.Post], error)
	UpdatePost(ctx context.Context, userID, postID string, req *dto.UpdatePostRequest) error
	DeletePost(ctx context.Context, userID, postID string) error
}

type PostUseCase struct {
	postRepo repository.IPostRepository
}

func NewPostUseCase(postRepo repository.IPostRepository) *PostUseCase {
	return &PostUseCase{
		postRepo: postRepo,
	}
}

func (uc *PostUseCase) CreatePost(ctx context.Context, userID string, req *dto.CreatePostRequest) (*entity.Post, error) {
	if req.Content == "" {
		return nil, ErrInvalidContent
	}

	return uc.postRepo.Create(ctx, userID, req.Content)
}

func (uc *PostUseCase) GetPostByID(ctx context.Context, id string) (*entity.Post, error) {
	post, err := uc.postRepo.GetByID(ctx, id)
	if err != nil {
		return nil, ErrPostNotFound
	}
	return post, nil
}

func (uc *PostUseCase) GetPostsByUserID(ctx context.Context, userID string, pageable *paging.Pageable) (*paging.Page[entity.Post], error) {
	return uc.postRepo.GetByUserID(ctx, userID, pageable)
}

func (uc *PostUseCase) UpdatePost(ctx context.Context, userID, postID string, req *dto.UpdatePostRequest) error {
	// Check if post exists and belongs to user
	post, err := uc.postRepo.GetByID(ctx, postID)
	if err != nil {
		return ErrPostNotFound
	}

	if post.UserID != userID {
		return ErrUnauthorized
	}

	return uc.postRepo.Update(ctx, postID, req.Content)
}

func (uc *PostUseCase) DeletePost(ctx context.Context, userID, postID string) error {
	// Check if post exists and belongs to user
	post, err := uc.postRepo.GetByID(ctx, postID)
	if err != nil {
		return ErrPostNotFound
	}

	if post.UserID != userID {
		return ErrUnauthorized
	}

	return uc.postRepo.Delete(ctx, postID)
}
