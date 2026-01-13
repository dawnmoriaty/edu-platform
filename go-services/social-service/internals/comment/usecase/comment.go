package usecase

import (
	"context"
	"errors"

	"social-service/internals/comment/controller/dto"
	"social-service/internals/comment/entity"
	"social-service/internals/comment/repository"
	"social-service/pkgs/paging"
)

var (
	ErrCommentNotFound = errors.New("comment not found")
	ErrUnauthorized    = errors.New("unauthorized to perform this action")
	ErrInvalidContent  = errors.New("invalid content")
)

type ICommentUseCase interface {
	CreateComment(ctx context.Context, postID, userID string, req *dto.CreateCommentRequest) (*entity.Comment, error)
	GetCommentByID(ctx context.Context, id string) (*entity.Comment, error)
	GetCommentsByPostID(ctx context.Context, postID string, pageable *paging.Pageable) (*paging.Page[entity.Comment], error)
	UpdateComment(ctx context.Context, userID, commentID string, req *dto.UpdateCommentRequest) error
	DeleteComment(ctx context.Context, userID, commentID string) error
}

type CommentUseCase struct {
	commentRepo repository.ICommentRepository
}

func NewCommentUseCase(commentRepo repository.ICommentRepository) *CommentUseCase {
	return &CommentUseCase{
		commentRepo: commentRepo,
	}
}

func (uc *CommentUseCase) CreateComment(ctx context.Context, postID, userID string, req *dto.CreateCommentRequest) (*entity.Comment, error) {
	if req.Content == "" {
		return nil, ErrInvalidContent
	}

	return uc.commentRepo.Create(ctx, postID, userID, req.Content)
}

func (uc *CommentUseCase) GetCommentByID(ctx context.Context, id string) (*entity.Comment, error) {
	comment, err := uc.commentRepo.GetByID(ctx, id)
	if err != nil {
		return nil, ErrCommentNotFound
	}
	return comment, nil
}

func (uc *CommentUseCase) GetCommentsByPostID(ctx context.Context, postID string, pageable *paging.Pageable) (*paging.Page[entity.Comment], error) {
	return uc.commentRepo.GetByPostID(ctx, postID, pageable)
}

func (uc *CommentUseCase) UpdateComment(ctx context.Context, userID, commentID string, req *dto.UpdateCommentRequest) error {
	comment, err := uc.commentRepo.GetByID(ctx, commentID)
	if err != nil {
		return ErrCommentNotFound
	}

	if comment.UserID != userID {
		return ErrUnauthorized
	}

	return uc.commentRepo.Update(ctx, commentID, req.Content)
}

func (uc *CommentUseCase) DeleteComment(ctx context.Context, userID, commentID string) error {
	comment, err := uc.commentRepo.GetByID(ctx, commentID)
	if err != nil {
		return ErrCommentNotFound
	}

	if comment.UserID != userID {
		return ErrUnauthorized
	}

	return uc.commentRepo.Delete(ctx, commentID)
}
