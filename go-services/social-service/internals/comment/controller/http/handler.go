package http

import (
	"net/http"

	"github.com/gin-gonic/gin"

	"social-service/internals/comment/controller/dto"
	"social-service/internals/comment/usecase"
	"social-service/pkgs/logger"
	"social-service/pkgs/paging"
	"social-service/pkgs/response"
)

type CommentHandler struct {
	usecase usecase.ICommentUseCase
}

func NewCommentHandler(usecase usecase.ICommentUseCase) *CommentHandler {
	return &CommentHandler{usecase: usecase}
}

// GetComments godoc
// @Summary Get comments by post
// @Tags Comments
// @Param post_id path string true "Post ID"
// @Success 200 {object} paging.Page[dto.CommentResponse]
// @Router /posts/{post_id}/comments [get]
func (h *CommentHandler) GetComments(c *gin.Context) {
	postID := c.Param("post_id")
	if postID == "" {
		response.BadRequest(c, nil, "post_id is required")
		return
	}

	pageable := paging.FromContext(c)
	page, err := h.usecase.GetCommentsByPostID(c.Request.Context(), postID, pageable)
	if err != nil {
		logger.Errorf("Failed to get comments: %v", err)
		response.InternalError(c, err)
		return
	}

	// Convert to response DTOs
	commentResponses := make([]dto.CommentResponse, len(page.Items))
	for i, cm := range page.Items {
		commentResponses[i] = dto.CommentResponse{
			ID:        cm.ID,
			PostID:    cm.PostID,
			UserID:    cm.UserID,
			Content:   cm.Content,
			CreatedAt: cm.CreatedAt.Format("2006-01-02T15:04:05Z07:00"),
			UpdatedAt: cm.UpdatedAt.Format("2006-01-02T15:04:05Z07:00"),
		}
	}

	response.JSON(c, http.StatusOK, paging.Of(commentResponses, page.Total, page.Page))
}

// CreateComment godoc
// @Summary Create a comment on a post
// @Tags Comments
// @Param post_id path string true "Post ID"
// @Param body body dto.CreateCommentRequest true "Comment data"
// @Success 201 {object} dto.CommentResponse
// @Router /posts/{post_id}/comments [post]
func (h *CommentHandler) CreateComment(c *gin.Context) {
	postID := c.Param("post_id")
	userID := c.GetString("user_id")

	var req dto.CreateCommentRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.BadRequest(c, err, "Invalid request body")
		return
	}

	comment, err := h.usecase.CreateComment(c.Request.Context(), postID, userID, &req)
	if err != nil {
		logger.Errorf("Failed to create comment: %v", err)
		response.InternalError(c, err)
		return
	}

	res := dto.CommentResponse{
		ID:        comment.ID,
		PostID:    comment.PostID,
		UserID:    comment.UserID,
		Content:   comment.Content,
		CreatedAt: comment.CreatedAt.Format("2006-01-02T15:04:05Z07:00"),
		UpdatedAt: comment.UpdatedAt.Format("2006-01-02T15:04:05Z07:00"),
	}

	response.Created(c, res)
}

// UpdateComment godoc
// @Summary Update a comment
// @Tags Comments
// @Param id path string true "Comment ID"
// @Param body body dto.UpdateCommentRequest true "Comment data"
// @Success 200 {object} response.Response
// @Router /comments/{id} [put]
func (h *CommentHandler) UpdateComment(c *gin.Context) {
	commentID := c.Param("id")
	userID := c.GetString("user_id")

	var req dto.UpdateCommentRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.BadRequest(c, err, "Invalid request body")
		return
	}

	if err := h.usecase.UpdateComment(c.Request.Context(), userID, commentID, &req); err != nil {
		switch err {
		case usecase.ErrCommentNotFound:
			response.NotFound(c, "Comment not found")
		case usecase.ErrUnauthorized:
			response.Forbidden(c, "Not authorized to update this comment")
		default:
			response.InternalError(c, err)
		}
		return
	}

	response.JSONWithMessage(c, http.StatusOK, nil, "Comment updated successfully")
}

// DeleteComment godoc
// @Summary Delete a comment
// @Tags Comments
// @Param id path string true "Comment ID"
// @Success 204
// @Router /comments/{id} [delete]
func (h *CommentHandler) DeleteComment(c *gin.Context) {
	commentID := c.Param("id")
	userID := c.GetString("user_id")

	if err := h.usecase.DeleteComment(c.Request.Context(), userID, commentID); err != nil {
		switch err {
		case usecase.ErrCommentNotFound:
			response.NotFound(c, "Comment not found")
		case usecase.ErrUnauthorized:
			response.Forbidden(c, "Not authorized to delete this comment")
		default:
			response.InternalError(c, err)
		}
		return
	}

	response.NoContent(c)
}
