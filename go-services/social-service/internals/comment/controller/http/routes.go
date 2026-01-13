package http

import (
	"github.com/gin-gonic/gin"
	"github.com/jackc/pgx/v5/pgxpool"

	sqlc "social-service/db/sqlc"
	"social-service/internals/comment/repository"
	"social-service/internals/comment/usecase"
)

func Routes(r *gin.RouterGroup, pool *pgxpool.Pool, queries *sqlc.Queries) {
	commentRepo := repository.NewCommentRepository(pool, queries)
	commentUseCase := usecase.NewCommentUseCase(commentRepo)
	commentHandler := NewCommentHandler(commentUseCase)

	comments := r.Group("/comments")
	{
		comments.POST("", commentHandler.CreateComment)
		comments.PUT("/:id", commentHandler.UpdateComment)
		comments.DELETE("/:id", commentHandler.DeleteComment)
	}

	// Post comments
	r.GET("/posts/:postId/comments", commentHandler.GetComments)
}
