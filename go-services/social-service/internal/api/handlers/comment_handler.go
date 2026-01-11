package handlers

import (
	"net/http"

	"github.com/eduplatform/go-services/social-service/internal/application"
	"github.com/eduplatform/go-services/social-service/internal/paging"
	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
)

type CommentHandler struct {
	commentService *application.CommentService
}

func NewCommentHandler(commentService *application.CommentService) *CommentHandler {
	return &CommentHandler{
		commentService: commentService,
	}
}

type CreateCommentRequest struct {
	PostID  string `json:"post_id" binding:"required"`
	Content string `json:"content" binding:"required"`
}

func (h *CommentHandler) CreateComment(c *gin.Context) {
	var req CreateCommentRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	postID, err := uuid.Parse(req.PostID)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid post id"})
		return
	}

	// TODO: Get userID from JWT token
	userID := uuid.MustParse("00000000-0000-0000-0000-000000000001")

	comment, err := h.commentService.CreateComment(c.Request.Context(), postID, userID, req.Content)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusCreated, comment)
}

func (h *CommentHandler) GetComment(c *gin.Context) {
	id, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid comment id"})
		return
	}

	comment, err := h.commentService.GetComment(c.Request.Context(), id)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "comment not found"})
		return
	}

	c.JSON(http.StatusOK, comment)
}

func (h *CommentHandler) GetPostComments(c *gin.Context) {
	postID, err := uuid.Parse(c.Param("post_id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid post id"})
		return
	}

	page, err := h.commentService.GetPostComments(c.Request.Context(), postID, paging.FromContext(c))
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, page)
}

func (h *CommentHandler) UpdateComment(c *gin.Context) {
	id, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid comment id"})
		return
	}

	var req CreateCommentRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	// TODO: Get userID from JWT token
	userID := uuid.MustParse("00000000-0000-0000-0000-000000000001")

	if err := h.commentService.UpdateComment(c.Request.Context(), id, userID, req.Content); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "comment updated"})
}

func (h *CommentHandler) DeleteComment(c *gin.Context) {
	id, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid comment id"})
		return
	}

	// TODO: Get userID from JWT token
	userID := uuid.MustParse("00000000-0000-0000-0000-000000000001")

	if err := h.commentService.DeleteComment(c.Request.Context(), id, userID); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "comment deleted"})
}
