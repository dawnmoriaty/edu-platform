package http

import (
	"net/http"

	"github.com/gin-gonic/gin"

	"social-service/internals/like/controller/dto"
	"social-service/internals/like/usecase"
	"social-service/pkgs/logger"
	"social-service/pkgs/response"
)

type LikeHandler struct {
	usecase usecase.ILikeUseCase
}

func NewLikeHandler(usecase usecase.ILikeUseCase) *LikeHandler {
	return &LikeHandler{usecase: usecase}
}

func (h *LikeHandler) LikePost(c *gin.Context) {
	userID := c.GetString("user_id")
	if userID == "" {
		response.Unauthorized(c, "User not authenticated")
		return
	}

	postID := c.Param("postId")
	if postID == "" {
		response.BadRequest(c, nil, "post_id is required")
		return
	}

	like, err := h.usecase.LikePost(c.Request.Context(), userID, postID)
	if err != nil {
		if err == usecase.ErrAlreadyLiked {
			response.BadRequest(c, err, "Already liked this post")
			return
		}
		logger.Errorf("Failed to like post: %v", err)
		response.InternalError(c, err)
		return
	}

	res := dto.LikeResponse{
		ID:        like.ID,
		PostID:    like.PostID,
		UserID:    like.UserID,
		CreatedAt: like.CreatedAt.Format("2006-01-02T15:04:05Z07:00"),
	}

	response.Created(c, res)
}

func (h *LikeHandler) UnlikePost(c *gin.Context) {
	userID := c.GetString("user_id")
	if userID == "" {
		response.Unauthorized(c, "User not authenticated")
		return
	}

	postID := c.Param("postId")
	if postID == "" {
		response.BadRequest(c, nil, "post_id is required")
		return
	}

	err := h.usecase.UnlikePost(c.Request.Context(), userID, postID)
	if err != nil {
		if err == usecase.ErrNotLiked {
			response.BadRequest(c, err, "Haven't liked this post")
			return
		}
		logger.Errorf("Failed to unlike post: %v", err)
		response.InternalError(c, err)
		return
	}

	response.NoContent(c)
}

func (h *LikeHandler) GetLikeStatus(c *gin.Context) {
	userID := c.GetString("user_id")
	postID := c.Param("postId")

	isLiked, count, err := h.usecase.GetLikeStatus(c.Request.Context(), userID, postID)
	if err != nil {
		logger.Errorf("Failed to get like status: %v", err)
		response.InternalError(c, err)
		return
	}

	res := dto.LikeStatusResponse{
		PostID:  postID,
		IsLiked: isLiked,
		Count:   count,
	}

	response.JSON(c, http.StatusOK, res)
}
