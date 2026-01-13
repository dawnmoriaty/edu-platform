package http

import (
	"net/http"

	"github.com/gin-gonic/gin"

	"social-service/internals/follow/controller/dto"
	"social-service/internals/follow/usecase"
	"social-service/pkgs/logger"
	"social-service/pkgs/paging"
	"social-service/pkgs/response"
)

type FollowHandler struct {
	usecase usecase.IFollowUseCase
}

func NewFollowHandler(usecase usecase.IFollowUseCase) *FollowHandler {
	return &FollowHandler{usecase: usecase}
}

// Follow godoc
// @Summary Follow a user
// @Tags Follow
// @Param user_id path string true "User ID to follow"
// @Success 201 {object} dto.FollowResponse
// @Router /users/{user_id}/follow [post]
func (h *FollowHandler) Follow(c *gin.Context) {
	followerID := c.GetString("user_id")
	followeeID := c.Param("user_id")

	follow, err := h.usecase.Follow(c.Request.Context(), followerID, followeeID)
	if err != nil {
		switch err {
		case usecase.ErrCannotFollowSelf:
			response.BadRequest(c, err, "Cannot follow yourself")
		case usecase.ErrAlreadyFollowing:
			response.BadRequest(c, err, "Already following this user")
		default:
			logger.Errorf("Failed to follow: %v", err)
			response.InternalError(c, err)
		}
		return
	}

	res := dto.FollowResponse{
		ID:         follow.ID,
		FollowerID: follow.FollowerID,
		FolloweeID: follow.FolloweeID,
		CreatedAt:  follow.CreatedAt.Format("2006-01-02T15:04:05Z07:00"),
	}

	response.Created(c, res)
}

// Unfollow godoc
// @Summary Unfollow a user
// @Tags Follow
// @Param user_id path string true "User ID to unfollow"
// @Success 204
// @Router /users/{user_id}/unfollow [delete]
func (h *FollowHandler) Unfollow(c *gin.Context) {
	followerID := c.GetString("user_id")
	followeeID := c.Param("user_id")

	if err := h.usecase.Unfollow(c.Request.Context(), followerID, followeeID); err != nil {
		switch err {
		case usecase.ErrNotFollowing:
			response.BadRequest(c, err, "Not following this user")
		default:
			logger.Errorf("Failed to unfollow: %v", err)
			response.InternalError(c, err)
		}
		return
	}

	response.NoContent(c)
}

// GetFollowers godoc
// @Summary Get user's followers
// @Tags Follow
// @Param user_id path string true "User ID"
// @Success 200 {object} paging.Page[string]
// @Router /users/{user_id}/followers [get]
func (h *FollowHandler) GetFollowers(c *gin.Context) {
	userID := c.Param("user_id")
	pageable := paging.FromContext(c)

	page, err := h.usecase.GetFollowers(c.Request.Context(), userID, pageable)
	if err != nil {
		logger.Errorf("Failed to get followers: %v", err)
		response.InternalError(c, err)
		return
	}

	response.JSON(c, http.StatusOK, page)
}

// GetFollowing godoc
// @Summary Get users that user is following
// @Tags Follow
// @Param user_id path string true "User ID"
// @Success 200 {object} paging.Page[string]
// @Router /users/{user_id}/following [get]
func (h *FollowHandler) GetFollowing(c *gin.Context) {
	userID := c.Param("user_id")
	pageable := paging.FromContext(c)

	page, err := h.usecase.GetFollowing(c.Request.Context(), userID, pageable)
	if err != nil {
		logger.Errorf("Failed to get following: %v", err)
		response.InternalError(c, err)
		return
	}

	response.JSON(c, http.StatusOK, page)
}

// GetFollowStats godoc
// @Summary Get follow statistics for a user
// @Tags Follow
// @Param user_id path string true "User ID"
// @Success 200 {object} dto.FollowStatsResponse
// @Router /users/{user_id}/follow-stats [get]
func (h *FollowHandler) GetFollowStats(c *gin.Context) {
	userID := c.Param("user_id")

	stats, err := h.usecase.GetFollowStats(c.Request.Context(), userID)
	if err != nil {
		logger.Errorf("Failed to get follow stats: %v", err)
		response.InternalError(c, err)
		return
	}

	res := dto.FollowStatsResponse{
		FollowersCount: stats.FollowersCount,
		FollowingCount: stats.FollowingCount,
	}

	response.JSON(c, http.StatusOK, res)
}

// CheckFollowing godoc
// @Summary Check if current user is following another user
// @Tags Follow
// @Param user_id path string true "User ID to check"
// @Success 200 {object} dto.IsFollowingResponse
// @Router /users/{user_id}/is-following [get]
func (h *FollowHandler) CheckFollowing(c *gin.Context) {
	followerID := c.GetString("user_id")
	followeeID := c.Param("user_id")

	isFollowing, err := h.usecase.IsFollowing(c.Request.Context(), followerID, followeeID)
	if err != nil {
		logger.Errorf("Failed to check following: %v", err)
		response.InternalError(c, err)
		return
	}

	response.JSON(c, http.StatusOK, dto.IsFollowingResponse{IsFollowing: isFollowing})
}
