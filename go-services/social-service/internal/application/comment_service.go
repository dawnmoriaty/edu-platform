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

type CommentService struct {
	commentRepo   *repository.CommentRepository
	postRepo      *repository.PostRepository
	commentMapper *mapper.CommentMapper
}

func NewCommentService(commentRepo *repository.CommentRepository, postRepo *repository.PostRepository) *CommentService {
	return &CommentService{
		commentRepo:   commentRepo,
		postRepo:      postRepo,
		commentMapper: mapper.NewCommentMapper(),
	}
}

func (s *CommentService) CreateComment(ctx context.Context, postID, userID uuid.UUID, content string) (*domain.Comment, error) {
	if content == "" {
		return nil, errors.New("content cannot be empty")
	}

	pgPostID := mapper.ToPgUUID(postID)

	// Verify post exists
	_, err := s.postRepo.GetByID(ctx, pgPostID)
	if err != nil {
		return nil, errors.New("post not found")
	}

	comment, err := s.commentRepo.Create(ctx, pgPostID, mapper.ToPgUUID(userID), content)
	if err != nil {
		return nil, err
	}
	return s.commentMapper.ToDomain(comment), nil
}

func (s *CommentService) GetComment(ctx context.Context, id uuid.UUID) (*domain.Comment, error) {
	comment, err := s.commentRepo.GetByID(ctx, mapper.ToPgUUID(id))
	if err != nil {
		return nil, err
	}
	return s.commentMapper.ToDomain(comment), nil
}

func (s *CommentService) GetPostComments(ctx context.Context, postID uuid.UUID, pageable *paging.Pageable) (*paging.Page[*domain.Comment], error) {
	comments, err := s.commentRepo.GetByPostID(ctx, mapper.ToPgUUID(postID), int32(pageable.Limit), int32(pageable.GetOffset()))
	if err != nil {
		return nil, err
	}

	items := s.commentMapper.ToDomainList(comments)
	return paging.NewPage(pageable, items), nil
}

func (s *CommentService) UpdateComment(ctx context.Context, id, userID uuid.UUID, content string) error {
	pgID := mapper.ToPgUUID(id)
	pgUserID := mapper.ToPgUUID(userID)

	comment, err := s.commentRepo.GetByID(ctx, pgID)
	if err != nil {
		return err
	}

	if comment.UserID != pgUserID {
		return errors.New("unauthorized: cannot update other user's comment")
	}

	return s.commentRepo.Update(ctx, pgID, content)
}

func (s *CommentService) DeleteComment(ctx context.Context, id, userID uuid.UUID) error {
	pgID := mapper.ToPgUUID(id)
	pgUserID := mapper.ToPgUUID(userID)

	comment, err := s.commentRepo.GetByID(ctx, pgID)
	if err != nil {
		return err
	}

	if comment.UserID != pgUserID {
		return errors.New("unauthorized: cannot delete other user's comment")
	}

	return s.commentRepo.Delete(ctx, pgID)
}
