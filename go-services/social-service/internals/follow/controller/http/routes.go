package http

import (
	"github.com/gin-gonic/gin"
	"github.com/jackc/pgx/v5/pgxpool"

	sqlc "social-service/db/sqlc"
	"social-service/internals/follow/repository"
	"social-service/internals/follow/usecase"
)

func Routes(r *gin.RouterGroup, pool *pgxpool.Pool, queries *sqlc.Queries) {
	followRepo := repository.NewFollowRepository(pool, queries)
	followUseCase := usecase.NewFollowUseCase(followRepo)
	followHandler := NewFollowHandler(followUseCase)

	follows := r.Group("/follows")
	{
		follows.POST("", followHandler.Follow)
		follows.DELETE("", followHandler.Unfollow)
		follows.GET("/check", followHandler.CheckFollowing)
	}

	// User followers and following
	r.GET("/users/:userId/followers", followHandler.GetFollowers)
	r.GET("/users/:userId/following", followHandler.GetFollowing)
	r.GET("/users/:userId/follow-stats", followHandler.GetFollowStats)
}
