package paging

import (
	"strconv"

	"github.com/gin-gonic/gin"
)

// FromContext extracts Pageable from gin context query params
// Usage: pageable := paging.FromContext(c)
func FromContext(c *gin.Context) *Pageable {
	page := DefaultPage
	limit := DefaultLimit

	if p := c.Query("page"); p != "" {
		if parsed, err := strconv.Atoi(p); err == nil && parsed > 0 {
			page = parsed
		}
	}
	if l := c.Query("limit"); l != "" {
		if parsed, err := strconv.Atoi(l); err == nil && parsed > 0 {
			limit = parsed
		}
	}

	pageable := NewPageableWithParams(page, limit)

	// Parse sort params: ?sort=name:asc,created_at:desc
	if sortStr := c.Query("sort"); sortStr != "" {
		pageable.Sort = ParseSortString(sortStr)
	}

	return pageable
}

// ParseSortString parses sort string like "name:asc,created_at:desc"
func ParseSortString(s string) []Order {
	if s == "" {
		return nil
	}

	var orders []Order
	// Simple parsing - can be enhanced
	// Format: field:direction,field:direction
	parts := splitAndTrim(s, ",")
	for _, part := range parts {
		fieldDir := splitAndTrim(part, ":")
		if len(fieldDir) >= 1 {
			order := Order{Property: fieldDir[0], Direction: ASC}
			if len(fieldDir) >= 2 && fieldDir[1] == "desc" {
				order.Direction = DESC
			}
			orders = append(orders, order)
		}
	}
	return orders
}

func splitAndTrim(s, sep string) []string {
	var result []string
	start := 0
	for i := 0; i < len(s); i++ {
		if string(s[i]) == sep {
			if i > start {
				result = append(result, s[start:i])
			}
			start = i + 1
		}
	}
	if start < len(s) {
		result = append(result, s[start:])
	}
	return result
}
