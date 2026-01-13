package http

import (
	"github.com/gin-gonic/gin"

	db "social-service/db/sqlc"
	"social-service/internals/like/repository"
	"social-service/internals/like/usecase"
)

func Routes(r *gin.RouterGroup, queries *db.Queries) {
	likeRepo := repository.NewLikeRepository(queries)
	likeUseCase := usecase.NewLikeUseCase(likeRepo)
	likeHandler := NewLikeHandler(likeUseCase)

	likes := r.Group("/posts/:postId/likes")
	{
		likes.POST("", likeHandler.LikePost)
		likes.DELETE("", likeHandler.UnlikePost)
		likes.GET("", likeHandler.GetLikeStatus)
	}
}
