package handlers

import (
	"net/http"

	"github.com/eduplatform/go-services/social-service/internal/application"
	"github.com/eduplatform/go-services/social-service/internal/paging"
	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
)

type FollowHandler struct {
	followService *application.FollowService
}

func NewFollowHandler(followService *application.FollowService) *FollowHandler {
	return &FollowHandler{
		followService: followService,
	}
}

type FollowRequest struct {
	FolloweeID string `json:"followee_id" binding:"required"`
}

func (h *FollowHandler) FollowUser(c *gin.Context) {
	var req FollowRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	followeeID, err := uuid.Parse(req.FolloweeID)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid followee id"})
		return
	}

	// TODO: Get followerID from JWT token
	followerID := uuid.MustParse("00000000-0000-0000-0000-000000000001")

	if _, err := h.followService.FollowUser(c.Request.Context(), followerID, followeeID); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "user followed"})
}

func (h *FollowHandler) UnfollowUser(c *gin.Context) {
	var req FollowRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	followeeID, err := uuid.Parse(req.FolloweeID)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid followee id"})
		return
	}

	// TODO: Get followerID from JWT token
	followerID := uuid.MustParse("00000000-0000-0000-0000-000000000001")

	if err := h.followService.UnfollowUser(c.Request.Context(), followerID, followeeID); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "user unfollowed"})
}

func (h *FollowHandler) GetFollowers(c *gin.Context) {
	userID, err := uuid.Parse(c.Param("user_id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid user id"})
		return
	}

	page, err := h.followService.GetFollowers(c.Request.Context(), userID, paging.FromContext(c))
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, page)
}

func (h *FollowHandler) GetFollowing(c *gin.Context) {
	userID, err := uuid.Parse(c.Param("user_id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid user id"})
		return
	}

	page, err := h.followService.GetFollowing(c.Request.Context(), userID, paging.FromContext(c))
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, page)
}

func (h *FollowHandler) GetFollowerCount(c *gin.Context) {
	userID, err := uuid.Parse(c.Param("user_id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid user id"})
		return
	}

	count, err := h.followService.GetFollowerCount(c.Request.Context(), userID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"count": count})
}

func (h *FollowHandler) GetFollowingCount(c *gin.Context) {
	userID, err := uuid.Parse(c.Param("user_id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid user id"})
		return
	}

	count, err := h.followService.GetFollowingCount(c.Request.Context(), userID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"count": count})
}

func (h *FollowHandler) IsFollowing(c *gin.Context) {
	followerIDStr := c.Query("follower_id")
	followeeIDStr := c.Query("followee_id")

	if followerIDStr == "" || followeeIDStr == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "follower_id and followee_id are required"})
		return
	}

	followerID, err := uuid.Parse(followerIDStr)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid follower id"})
		return
	}

	followeeID, err := uuid.Parse(followeeIDStr)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid followee id"})
		return
	}

	isFollowing := h.followService.IsFollowing(c.Request.Context(), followerID, followeeID)

	c.JSON(http.StatusOK, gin.H{"is_following": isFollowing})
}
