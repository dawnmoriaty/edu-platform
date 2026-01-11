package api

import (
	"github.com/eduplatform/go-services/social-service/internal/api/handlers"
	"github.com/gin-gonic/gin"
)

func SetupRoutes(router *gin.Engine, postHandler *handlers.PostHandler, commentHandler *handlers.CommentHandler, likeHandler *handlers.LikeHandler, followHandler *handlers.FollowHandler) {
	v1 := router.Group("/api/v1")
	
	// Post routes
	posts := v1.Group("/posts")
	{
		posts.POST("", postHandler.CreatePost)
		posts.GET("/:id", postHandler.GetPost)
		posts.GET("/user/:user_id", postHandler.GetUserPosts)
		posts.PUT("/:id", postHandler.UpdatePost)
		posts.DELETE("/:id", postHandler.DeletePost)
	}
	
	// Comment routes
	comments := v1.Group("/comments")
	{
		comments.POST("", commentHandler.CreateComment)
		comments.GET("/:id", commentHandler.GetComment)
		comments.GET("/post/:post_id", commentHandler.GetPostComments)
		comments.PUT("/:id", commentHandler.UpdateComment)
		comments.DELETE("/:id", commentHandler.DeleteComment)
	}
	
	// Like routes
	likes := v1.Group("/likes")
	{
		likes.POST("/post/:post_id", likeHandler.LikePost)
		likes.DELETE("/post/:post_id", likeHandler.UnlikePost)
		likes.GET("/post/:post_id", likeHandler.GetPostLikes)
		likes.GET("/post/:post_id/count", likeHandler.GetLikeCount)
		likes.GET("/post/:post_id/check", likeHandler.IsLiked)
	}
	
	// Follow routes
	follows := v1.Group("/follows")
	{
		follows.POST("", followHandler.FollowUser)
		follows.DELETE("", followHandler.UnfollowUser)
		follows.GET("/followers/:user_id", followHandler.GetFollowers)
		follows.GET("/following/:user_id", followHandler.GetFollowing)
		follows.GET("/followers/:user_id/count", followHandler.GetFollowerCount)
		follows.GET("/following/:user_id/count", followHandler.GetFollowingCount)
		follows.GET("/check", followHandler.IsFollowing)
	}
}
