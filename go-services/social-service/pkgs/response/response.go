package response

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

type Response struct {
	Success bool   `json:"success"`
	Data    any    `json:"data,omitempty"`
	Message string `json:"message,omitempty"`
	Error   string `json:"error,omitempty"`
}

type PaginatedResponse struct {
	Success    bool   `json:"success"`
	Data       any    `json:"data,omitempty"`
	Pagination any    `json:"pagination,omitempty"`
	Message    string `json:"message,omitempty"`
}

func JSON(c *gin.Context, statusCode int, data any) {
	c.JSON(statusCode, Response{
		Success: true,
		Data:    data,
	})
}

func JSONWithMessage(c *gin.Context, statusCode int, data any, message string) {
	c.JSON(statusCode, Response{
		Success: true,
		Data:    data,
		Message: message,
	})
}

func JSONPaginated(c *gin.Context, statusCode int, data any, pagination any) {
	c.JSON(statusCode, PaginatedResponse{
		Success:    true,
		Data:       data,
		Pagination: pagination,
	})
}

func Error(c *gin.Context, statusCode int, err error, message string) {
	errMsg := ""
	if err != nil {
		errMsg = err.Error()
	}
	c.JSON(statusCode, Response{
		Success: false,
		Error:   errMsg,
		Message: message,
	})
}

func BadRequest(c *gin.Context, err error, message string) {
	Error(c, http.StatusBadRequest, err, message)
}

func NotFound(c *gin.Context, message string) {
	Error(c, http.StatusNotFound, nil, message)
}

func InternalError(c *gin.Context, err error) {
	Error(c, http.StatusInternalServerError, err, "Internal server error")
}

func Unauthorized(c *gin.Context, message string) {
	Error(c, http.StatusUnauthorized, nil, message)
}

func Forbidden(c *gin.Context, message string) {
	Error(c, http.StatusForbidden, nil, message)
}

func Created(c *gin.Context, data any) {
	JSON(c, http.StatusCreated, data)
}

func NoContent(c *gin.Context) {
	c.Status(http.StatusNoContent)
}
