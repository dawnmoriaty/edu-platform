package entity

import "time"

// Conversation types
type ConversationType string

const (
	ConversationTypeDirect ConversationType = "direct"
	ConversationTypeGroup  ConversationType = "group"
)

// Message types
type MessageType string

const (
	MessageTypeText   MessageType = "text"
	MessageTypeImage  MessageType = "image"
	MessageTypeFile   MessageType = "file"
	MessageTypeSystem MessageType = "system"
)

// Message status
type MessageStatus string

const (
	MessageStatusSent      MessageStatus = "sent"
	MessageStatusDelivered MessageStatus = "delivered"
	MessageStatusRead      MessageStatus = "read"
)

// Participant roles
type ParticipantRole string

const (
	ParticipantRoleAdmin  ParticipantRole = "admin"
	ParticipantRoleMember ParticipantRole = "member"
)

type Conversation struct {
	ID            string           `json:"id"`
	Type          ConversationType `json:"type"`
	Name          string           `json:"name,omitempty"`
	AvatarUrl     string           `json:"avatar_url,omitempty"`
	LastMessage   string           `json:"last_message,omitempty"`
	LastMessageAt *time.Time       `json:"last_message_at,omitempty"`
	CreatedAt     time.Time        `json:"created_at"`
	UpdatedAt     time.Time        `json:"updated_at"`
	Participants  []Participant    `json:"participants,omitempty"`
}

type Message struct {
	ID             string        `json:"id"`
	ConversationID string        `json:"conversation_id"`
	SenderID       string        `json:"sender_id"`
	MessageType    MessageType   `json:"message_type"`
	Content        string        `json:"content,omitempty"`
	MediaUrl       string        `json:"media_url,omitempty"`
	Status         MessageStatus `json:"status"`
	IsEdited       bool          `json:"is_edited"`
	IsDeleted      bool          `json:"is_deleted"`
	CreatedAt      time.Time     `json:"created_at"`
	UpdatedAt      time.Time     `json:"updated_at"`
}

type Participant struct {
	ConversationID string          `json:"conversation_id"`
	UserID         string          `json:"user_id"`
	Role           ParticipantRole `json:"role"`
	JoinedAt       time.Time       `json:"joined_at"`
}

// ConversationMember is alias for backward compatibility
type ConversationMember = Participant
