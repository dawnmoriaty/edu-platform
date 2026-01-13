package http

import (
	"net/http"

	"github.com/gin-gonic/gin"

	"social-service/internals/post/controller/dto"
	"social-service/internals/post/usecase"
	"social-service/pkgs/logger"
	"social-service/pkgs/paging"
	"social-service/pkgs/redis"
	"social-service/pkgs/response"
)

type PostHandler struct {
	usecase usecase.IPostUseCase
	cache   redis.IRedis
}

func NewPostHandler(usecase usecase.IPostUseCase, cache redis.IRedis) *PostHandler {
	return &PostHandler{
		usecase: usecase,
		cache:   cache,
	}
}

// GetPosts godoc
// @Summary Get posts by user
// @Description Get paginated posts for a specific user
// @Tags Posts
// @Accept json
// @Produce json
// @Param user_id query string true "User ID"
// @Param page query int false "Page number"
// @Param limit query int false "Items per page"
// @Success 200 {object} paging.Page[dto.PostResponse]
// @Router /posts [get]
func (h *PostHandler) GetPosts(c *gin.Context) {
	var req dto.ListPostsRequest
	if err := c.ShouldBindQuery(&req); err != nil {
		response.BadRequest(c, err, "Invalid query parameters")
		return
	}

	if req.UserID == "" {
		response.BadRequest(c, nil, "user_id is required")
		return
	}

	pageable := paging.FromContext(c)
	page, err := h.usecase.GetPostsByUserID(c.Request.Context(), req.UserID, pageable)
	if err != nil {
		logger.Errorf("Failed to get posts: %v", err)
		response.InternalError(c, err)
		return
	}

	// Convert to response DTOs
	postResponses := make([]dto.PostResponse, len(page.Items))
	for i, p := range page.Items {
		postResponses[i] = dto.PostResponse{
			ID:        p.ID,
			UserID:    p.UserID,
			Content:   p.Content,
			CreatedAt: p.CreatedAt.Format("2006-01-02T15:04:05Z07:00"),
			UpdatedAt: p.UpdatedAt.Format("2006-01-02T15:04:05Z07:00"),
		}
	}

	response.JSON(c, http.StatusOK, paging.Of(postResponses, page.Total, page.Page))
}

// GetPost godoc
// @Summary Get post by ID
// @Description Get a specific post by its ID
// @Tags Posts
// @Produce json
// @Param id path string true "Post ID"
// @Success 200 {object} dto.PostResponse
// @Router /posts/{id} [get]
func (h *PostHandler) GetPost(c *gin.Context) {
	postID := c.Param("id")

	// Try cache first
	var cached dto.PostResponse
	cacheKey := "post:" + postID
	if err := h.cache.Get(c.Request.Context(), cacheKey, &cached); err == nil {
		response.JSON(c, http.StatusOK, cached)
		return
	}

	post, err := h.usecase.GetPostByID(c.Request.Context(), postID)
	if err != nil {
		if err == usecase.ErrPostNotFound {
			response.NotFound(c, "Post not found")
			return
		}
		logger.Errorf("Failed to get post: %v", err)
		response.InternalError(c, err)
		return
	}

	res := dto.PostResponse{
		ID:        post.ID,
		UserID:    post.UserID,
		Content:   post.Content,
		CreatedAt: post.CreatedAt.Format("2006-01-02T15:04:05Z07:00"),
		UpdatedAt: post.UpdatedAt.Format("2006-01-02T15:04:05Z07:00"),
	}

	// Cache for 5 minutes
	_ = h.cache.SetWithExpiration(c.Request.Context(), cacheKey, res, 5*60)

	response.JSON(c, http.StatusOK, res)
}

// CreatePost godoc
// @Summary Create a new post
// @Description Create a new post for the authenticated user
// @Tags Posts
// @Accept json
// @Produce json
// @Param request body dto.CreatePostRequest true "Post content"
// @Success 201 {object} dto.PostResponse
// @Router /posts [post]
func (h *PostHandler) CreatePost(c *gin.Context) {
	userID := c.GetString("user_id") // From auth middleware
	if userID == "" {
		response.Unauthorized(c, "User not authenticated")
		return
	}

	var req dto.CreatePostRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.BadRequest(c, err, "Invalid request body")
		return
	}

	post, err := h.usecase.CreatePost(c.Request.Context(), userID, &req)
	if err != nil {
		logger.Errorf("Failed to create post: %v", err)
		response.InternalError(c, err)
		return
	}

	res := dto.PostResponse{
		ID:        post.ID,
		UserID:    post.UserID,
		Content:   post.Content,
		CreatedAt: post.CreatedAt.Format("2006-01-02T15:04:05Z07:00"),
		UpdatedAt: post.UpdatedAt.Format("2006-01-02T15:04:05Z07:00"),
	}

	response.Created(c, res)
}

// UpdatePost godoc
// @Summary Update a post
// @Description Update an existing post
// @Tags Posts
// @Accept json
// @Produce json
// @Param id path string true "Post ID"
// @Param request body dto.UpdatePostRequest true "Updated content"
// @Success 200 {object} response.Response
// @Router /posts/{id} [put]
func (h *PostHandler) UpdatePost(c *gin.Context) {
	userID := c.GetString("user_id")
	if userID == "" {
		response.Unauthorized(c, "User not authenticated")
		return
	}

	postID := c.Param("id")

	var req dto.UpdatePostRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.BadRequest(c, err, "Invalid request body")
		return
	}

	err := h.usecase.UpdatePost(c.Request.Context(), userID, postID, &req)
	if err != nil {
		switch err {
		case usecase.ErrPostNotFound:
			response.NotFound(c, "Post not found")
		case usecase.ErrUnauthorized:
			response.Forbidden(c, "You can only update your own posts")
		default:
			logger.Errorf("Failed to update post: %v", err)
			response.InternalError(c, err)
		}
		return
	}

	// Invalidate cache
	_ = h.cache.Remove(c.Request.Context(), "post:"+postID)

	response.JSONWithMessage(c, http.StatusOK, nil, "Post updated successfully")
}

// DeletePost godoc
// @Summary Delete a post
// @Description Delete an existing post
// @Tags Posts
// @Param id path string true "Post ID"
// @Success 204
// @Router /posts/{id} [delete]
func (h *PostHandler) DeletePost(c *gin.Context) {
	userID := c.GetString("user_id")
	if userID == "" {
		response.Unauthorized(c, "User not authenticated")
		return
	}

	postID := c.Param("id")

	err := h.usecase.DeletePost(c.Request.Context(), userID, postID)
	if err != nil {
		switch err {
		case usecase.ErrPostNotFound:
			response.NotFound(c, "Post not found")
		case usecase.ErrUnauthorized:
			response.Forbidden(c, "You can only delete your own posts")
		default:
			logger.Errorf("Failed to delete post: %v", err)
			response.InternalError(c, err)
		}
		return
	}

	// Invalidate cache
	_ = h.cache.Remove(c.Request.Context(), "post:"+postID)

	response.NoContent(c)
}
