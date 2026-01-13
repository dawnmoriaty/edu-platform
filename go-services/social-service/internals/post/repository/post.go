package repository

import (
	"context"
	"time"

	"github.com/jackc/pgx/v5/pgtype"
	"github.com/jackc/pgx/v5/pgxpool"

	db "social-service/db/sqlc"
	"social-service/internals/post/entity"
	"social-service/pkgs/paging"
	"social-service/utils"
)

type IPostRepository interface {
	Create(ctx context.Context, userID, content string) (*entity.Post, error)
	GetByID(ctx context.Context, id string) (*entity.Post, error)
	GetByUserID(ctx context.Context, userID string, pageable *paging.Pageable) (*paging.Page[entity.Post], error)
	Update(ctx context.Context, id, content string) error
	Delete(ctx context.Context, id string) error
	CountByUserID(ctx context.Context, userID string) (int64, error)
}

type PostRepository struct {
	pool    *pgxpool.Pool
	queries *db.Queries
}

func NewPostRepository(pool *pgxpool.Pool, queries *db.Queries) *PostRepository {
	return &PostRepository{
		pool:    pool,
		queries: queries,
	}
}

func (r *PostRepository) Create(ctx context.Context, userID, content string) (*entity.Post, error) {
	uid, err := utils.StringToUUID(userID)
	if err != nil {
		return nil, err
	}

	now := time.Now()
	post, err := r.queries.CreatePost(ctx, db.CreatePostParams{
		UserID:    uid,
		Content:   content,
		CreatedAt: pgtype.Timestamp{Time: now, Valid: true},
		UpdatedAt: pgtype.Timestamp{Time: now, Valid: true},
	})
	if err != nil {
		return nil, err
	}

	return dbPostToEntity(post), nil
}

func (r *PostRepository) GetByID(ctx context.Context, id string) (*entity.Post, error) {
	uid, err := utils.StringToUUID(id)
	if err != nil {
		return nil, err
	}

	post, err := r.queries.GetPostByID(ctx, uid)
	if err != nil {
		return nil, err
	}

	return dbPostToEntity(post), nil
}

func (r *PostRepository) GetByUserID(ctx context.Context, userID string, pageable *paging.Pageable) (*paging.Page[entity.Post], error) {
	uid, err := utils.StringToUUID(userID)
	if err != nil {
		return nil, err
	}

	// Get total count first
	total, err := r.CountByUserID(ctx, userID)
	if err != nil {
		return nil, err
	}
	pageable.SetTotal(total)

	posts, err := r.queries.GetPostsByUserID(ctx, db.GetPostsByUserIDParams{
		UserID: uid,
		Limit:  int32(pageable.Limit),
		Offset: int32(pageable.GetOffset()),
	})
	if err != nil {
		return nil, err
	}

	result := make([]entity.Post, len(posts))
	for i, p := range posts {
		result[i] = *dbPostToEntity(p)
	}

	return paging.NewPage(pageable, result), nil
}

func (r *PostRepository) Update(ctx context.Context, id, content string) error {
	uid, err := utils.StringToUUID(id)
	if err != nil {
		return err
	}

	return r.queries.UpdatePost(ctx, db.UpdatePostParams{
		ID:        uid,
		Content:   content,
		UpdatedAt: pgtype.Timestamp{Time: time.Now(), Valid: true},
	})
}

func (r *PostRepository) Delete(ctx context.Context, id string) error {
	uid, err := utils.StringToUUID(id)
	if err != nil {
		return err
	}
	return r.queries.DeletePost(ctx, uid)
}

func (r *PostRepository) CountByUserID(ctx context.Context, userID string) (int64, error) {
	uid, err := utils.StringToUUID(userID)
	if err != nil {
		return 0, err
	}

	// Use raw query for count since sqlc doesn't have this
	var count int64
	err = r.pool.QueryRow(ctx, "SELECT COUNT(*) FROM posts WHERE user_id = $1", uid).Scan(&count)
	return count, err
}

// Helper to convert DB model to entity
func dbPostToEntity(p db.Post) *entity.Post {
	return &entity.Post{
		ID:        utils.UUIDToString(p.ID),
		UserID:    utils.UUIDToString(p.UserID),
		Content:   p.Content,
		CreatedAt: p.CreatedAt.Time,
		UpdatedAt: p.UpdatedAt.Time,
	}
}
