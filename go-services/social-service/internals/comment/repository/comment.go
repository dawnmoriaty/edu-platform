package repository

import (
	"context"
	"time"

	"github.com/jackc/pgx/v5/pgtype"
	"github.com/jackc/pgx/v5/pgxpool"

	db "social-service/db/sqlc"
	"social-service/internals/comment/entity"
	"social-service/pkgs/paging"
	"social-service/utils"
)

type ICommentRepository interface {
	Create(ctx context.Context, postID, userID, content string) (*entity.Comment, error)
	GetByID(ctx context.Context, id string) (*entity.Comment, error)
	GetByPostID(ctx context.Context, postID string, pageable *paging.Pageable) (*paging.Page[entity.Comment], error)
	Update(ctx context.Context, id, content string) error
	Delete(ctx context.Context, id string) error
	CountByPostID(ctx context.Context, postID string) (int64, error)
}

type CommentRepository struct {
	pool    *pgxpool.Pool
	queries *db.Queries
}

func NewCommentRepository(pool *pgxpool.Pool, queries *db.Queries) *CommentRepository {
	return &CommentRepository{
		pool:    pool,
		queries: queries,
	}
}

func (r *CommentRepository) Create(ctx context.Context, postID, userID, content string) (*entity.Comment, error) {
	postUUID, err := utils.StringToUUID(postID)
	if err != nil {
		return nil, err
	}
	userUUID, err := utils.StringToUUID(userID)
	if err != nil {
		return nil, err
	}

	now := time.Now()
	comment, err := r.queries.CreateComment(ctx, db.CreateCommentParams{
		PostID:    postUUID,
		UserID:    userUUID,
		Content:   content,
		CreatedAt: pgtype.Timestamp{Time: now, Valid: true},
		UpdatedAt: pgtype.Timestamp{Time: now, Valid: true},
	})
	if err != nil {
		return nil, err
	}

	return dbCommentToEntity(comment), nil
}

func (r *CommentRepository) GetByID(ctx context.Context, id string) (*entity.Comment, error) {
	uid, err := utils.StringToUUID(id)
	if err != nil {
		return nil, err
	}

	comment, err := r.queries.GetCommentByID(ctx, uid)
	if err != nil {
		return nil, err
	}

	return dbCommentToEntity(comment), nil
}

func (r *CommentRepository) GetByPostID(ctx context.Context, postID string, pageable *paging.Pageable) (*paging.Page[entity.Comment], error) {
	postUUID, err := utils.StringToUUID(postID)
	if err != nil {
		return nil, err
	}

	total, err := r.CountByPostID(ctx, postID)
	if err != nil {
		return nil, err
	}
	pageable.SetTotal(total)

	comments, err := r.queries.GetCommentsByPostID(ctx, db.GetCommentsByPostIDParams{
		PostID: postUUID,
		Limit:  int32(pageable.Limit),
		Offset: int32(pageable.GetOffset()),
	})
	if err != nil {
		return nil, err
	}

	result := make([]entity.Comment, len(comments))
	for i, c := range comments {
		result[i] = *dbCommentToEntity(c)
	}

	return paging.NewPage(pageable, result), nil
}

func (r *CommentRepository) Update(ctx context.Context, id, content string) error {
	uid, err := utils.StringToUUID(id)
	if err != nil {
		return err
	}

	return r.queries.UpdateComment(ctx, db.UpdateCommentParams{
		ID:        uid,
		Content:   content,
		UpdatedAt: pgtype.Timestamp{Time: time.Now(), Valid: true},
	})
}

func (r *CommentRepository) Delete(ctx context.Context, id string) error {
	uid, err := utils.StringToUUID(id)
	if err != nil {
		return err
	}
	return r.queries.DeleteComment(ctx, uid)
}

func (r *CommentRepository) CountByPostID(ctx context.Context, postID string) (int64, error) {
	postUUID, err := utils.StringToUUID(postID)
	if err != nil {
		return 0, err
	}

	var count int64
	err = r.pool.QueryRow(ctx, "SELECT COUNT(*) FROM comments WHERE post_id = $1", postUUID).Scan(&count)
	return count, err
}

func dbCommentToEntity(c db.Comment) *entity.Comment {
	return &entity.Comment{
		ID:        utils.UUIDToString(c.ID),
		PostID:    utils.UUIDToString(c.PostID),
		UserID:    utils.UUIDToString(c.UserID),
		Content:   c.Content,
		CreatedAt: c.CreatedAt.Time,
		UpdatedAt: c.UpdatedAt.Time,
	}
}
