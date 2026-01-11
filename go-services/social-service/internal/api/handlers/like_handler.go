package handlers

import (
	"net/http"

	"github.com/eduplatform/go-services/social-service/internal/application"
	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
)

type LikeHandler struct {
	likeService *application.LikeService
}

func NewLikeHandler(likeService *application.LikeService) *LikeHandler {
	return &LikeHandler{
		likeService: likeService,
	}
}

func (h *LikeHandler) LikePost(c *gin.Context) {
	postID, err := uuid.Parse(c.Param("post_id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid post id"})
		return
	}

	// TODO: Get userID from JWT token
	userID := uuid.MustParse("00000000-0000-0000-0000-000000000001")

	if _, err := h.likeService.LikePost(c.Request.Context(), postID, userID); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "post liked"})
}

func (h *LikeHandler) UnlikePost(c *gin.Context) {
	postID, err := uuid.Parse(c.Param("post_id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid post id"})
		return
	}

	// TODO: Get userID from JWT token
	userID := uuid.MustParse("00000000-0000-0000-0000-000000000001")

	if err := h.likeService.UnlikePost(c.Request.Context(), postID, userID); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "post unliked"})
}

func (h *LikeHandler) GetPostLikes(c *gin.Context) {
	postID, err := uuid.Parse(c.Param("post_id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid post id"})
		return
	}

	likes, err := h.likeService.GetPostLikes(c.Request.Context(), postID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, likes)
}

func (h *LikeHandler) GetLikeCount(c *gin.Context) {
	postID, err := uuid.Parse(c.Param("post_id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid post id"})
		return
	}

	count, err := h.likeService.GetLikeCount(c.Request.Context(), postID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"count": count})
}

func (h *LikeHandler) IsLiked(c *gin.Context) {
	postID, err := uuid.Parse(c.Param("post_id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid post id"})
		return
	}

	// TODO: Get userID from JWT token
	userID := uuid.MustParse("00000000-0000-0000-0000-000000000001")

	isLiked := h.likeService.IsLiked(c.Request.Context(), postID, userID)

	c.JSON(http.StatusOK, gin.H{"is_liked": isLiked})
}
