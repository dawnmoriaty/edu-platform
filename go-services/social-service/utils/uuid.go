package utils

import (
	"github.com/google/uuid"
	"github.com/jackc/pgx/v5/pgtype"
)

// StringToUUID converts a string to pgtype.UUID
func StringToUUID(s string) (pgtype.UUID, error) {
	if s == "" {
		return pgtype.UUID{Valid: false}, nil
	}

	u, err := uuid.Parse(s)
	if err != nil {
		return pgtype.UUID{}, err
	}

	return pgtype.UUID{
		Bytes: u,
		Valid: true,
	}, nil
}

// UUIDToString converts pgtype.UUID to string
func UUIDToString(u pgtype.UUID) string {
	if !u.Valid {
		return ""
	}
	return uuid.UUID(u.Bytes).String()
}

// NewUUID generates a new UUID string
func NewUUID() string {
	return uuid.New().String()
}

// NewPgUUID generates a new pgtype.UUID
func NewPgUUID() pgtype.UUID {
	u := uuid.New()
	return pgtype.UUID{
		Bytes: u,
		Valid: true,
	}
}
