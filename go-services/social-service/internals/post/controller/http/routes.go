package http

import (
	"github.com/gin-gonic/gin"
	"github.com/jackc/pgx/v5/pgxpool"

	db "social-service/db/sqlc"
	"social-service/internals/post/repository"
	"social-service/internals/post/usecase"
	"social-service/pkgs/redis"
)

func Routes(r *gin.RouterGroup, pool *pgxpool.Pool, queries *db.Queries, cache redis.IRedis) {
	// Initialize layers
	postRepo := repository.NewPostRepository(pool, queries)
	postUseCase := usecase.NewPostUseCase(postRepo)
	postHandler := NewPostHandler(postUseCase, cache)

	// Routes
	posts := r.Group("/posts")
	{
		posts.GET("", postHandler.GetPosts)
		posts.GET("/:id", postHandler.GetPost)
		posts.POST("", postHandler.CreatePost)
		posts.PUT("/:id", postHandler.UpdatePost)
		posts.DELETE("/:id", postHandler.DeletePost)
	}
}
