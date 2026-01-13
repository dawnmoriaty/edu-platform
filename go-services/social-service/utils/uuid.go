package utils

import (
	"github.com/google/uuid"
	"github.com/jackc/pgx/v5/pgtype"
)

// StringToUUID converts string to pgtype.UUID
func StringToUUID(s string) (pgtype.UUID, error) {
	parsed, err := uuid.Parse(s)
	if err != nil {
		return pgtype.UUID{}, err
	}
	return pgtype.UUID{Bytes: parsed, Valid: true}, nil
}

// UUIDToString converts pgtype.UUID to string
func UUIDToString(u pgtype.UUID) string {
	if !u.Valid {
		return ""
	}
	return uuid.UUID(u.Bytes).String()
}

// NewUUID generates a new UUID
func NewUUID() pgtype.UUID {
	newUUID := uuid.New()
	return pgtype.UUID{Bytes: newUUID, Valid: true}
}

// MustStringToUUID converts string to pgtype.UUID, panics on error
func MustStringToUUID(s string) pgtype.UUID {
	u, err := StringToUUID(s)
	if err != nil {
		panic(err)
	}
	return u
}

// Int32Ptr returns pointer to int32
func Int32Ptr(v int32) *int32 {
	return &v
}

// StringPtr returns pointer to string
func StringPtr(s string) *string {
	return &s
}
