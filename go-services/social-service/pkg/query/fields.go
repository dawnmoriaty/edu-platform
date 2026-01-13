package query

// Field helpers for common fields - similar to jOOQ generated fields
type Field string

// Common post fields
const (
	PostID           Field = "posts.id"
	PostUserID       Field = "posts.user_id"
	PostPageID       Field = "posts.page_id"
	PostContent      Field = "posts.content"
	PostStatus       Field = "posts.status"
	PostVisibility   Field = "posts.visibility"
	PostLikeCount    Field = "posts.like_count"
	PostCommentCount Field = "posts.comment_count"
	PostCreatedAt    Field = "posts.created_at"
)

// Common user fields
const (
	UserID        Field = "users.id"
	UserUsername  Field = "users.username"
	UserEmail     Field = "users.email"
	UserFullName  Field = "users.full_name"
	UserAvatarURL Field = "users.avatar_url"
	UserRole      Field = "users.role"
)

// Common comment fields
const (
	CommentID        Field = "comments.id"
	CommentPostID    Field = "comments.post_id"
	CommentUserID    Field = "comments.user_id"
	CommentParentID  Field = "comments.parent_id"
	CommentContent   Field = "comments.content"
	CommentLikeCount Field = "comments.like_count"
)

// String returns the field as string
func (f Field) String() string {
	return string(f)
}

// Eq creates an equals condition
func (f Field) Eq(value interface{}) Condition {
	return Eq(string(f), value)
}

// NotEq creates a not equals condition
func (f Field) NotEq(value interface{}) Condition {
	return NotEq(string(f), value)
}

// Gt creates a greater than condition
func (f Field) Gt(value interface{}) Condition {
	return Gt(string(f), value)
}

// Lt creates a less than condition
func (f Field) Lt(value interface{}) Condition {
	return Lt(string(f), value)
}

// Contains creates a contains condition
func (f Field) Contains(value string) Condition {
	return Contains(string(f), value)
}

// IsNull creates an IS NULL condition
func (f Field) IsNull() Condition {
	return IsNull(string(f))
}

// IsNotNull creates an IS NOT NULL condition
func (f Field) IsNotNull() Condition {
	return IsNotNull(string(f))
}

// In creates an IN condition
func (f Field) In(values ...interface{}) Condition {
	return In(string(f), values...)
}
