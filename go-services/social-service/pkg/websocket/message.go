package websocket

import (
	"time"

	"github.com/google/uuid"
)

// MessageType defines the type of WebSocket message
type MessageType string

const (
	// Chat messages
	TypeChatMessage  MessageType = "chat_message"
	TypeChatTyping   MessageType = "chat_typing"
	TypeChatRead     MessageType = "chat_read"
	TypeChatReaction MessageType = "chat_reaction"
	TypeChatDeleted  MessageType = "chat_deleted"
	TypeChatEdited   MessageType = "chat_edited"

	// Presence
	TypeUserOnline  MessageType = "user_online"
	TypeUserOffline MessageType = "user_offline"

	// Notifications
	TypeNotification MessageType = "notification"

	// Room management
	TypeJoinRoom  MessageType = "join_room"
	TypeLeaveRoom MessageType = "leave_room"

	// System
	TypePing  MessageType = "ping"
	TypePong  MessageType = "pong"
	TypeError MessageType = "error"
)

// Message represents a WebSocket message
type Message struct {
	Type      MessageType `json:"type"`
	RoomID    string      `json:"room_id,omitempty"` // conversation ID
	SenderID  uuid.UUID   `json:"sender_id,omitempty"`
	Content   string      `json:"content,omitempty"`
	Data      interface{} `json:"data,omitempty"`    // Additional data
	Payload   interface{} `json:"payload,omitempty"` // Message payload for chat
	Timestamp time.Time   `json:"timestamp"`
}

// ChatMessageData represents chat message payload
type ChatMessageData struct {
	MessageID      uuid.UUID `json:"message_id"`
	ConversationID uuid.UUID `json:"conversation_id"`
	SenderID       uuid.UUID `json:"sender_id"`
	SenderName     string    `json:"sender_name"`
	SenderAvatar   string    `json:"sender_avatar"`
	Content        string    `json:"content"`
	MessageType    string    `json:"message_type"` // TEXT, IMAGE, FILE
	MediaURL       string    `json:"media_url,omitempty"`
	CreatedAt      time.Time `json:"created_at"`
}

// TypingData represents typing indicator payload
type TypingData struct {
	ConversationID uuid.UUID `json:"conversation_id"`
	UserID         uuid.UUID `json:"user_id"`
	Username       string    `json:"username"`
	IsTyping       bool      `json:"is_typing"`
}

// ReadReceiptData represents read receipt payload
type ReadReceiptData struct {
	ConversationID uuid.UUID `json:"conversation_id"`
	UserID         uuid.UUID `json:"user_id"`
	MessageID      uuid.UUID `json:"message_id"`
	ReadAt         time.Time `json:"read_at"`
}

// ReactionData represents message reaction payload
type ReactionData struct {
	MessageID uuid.UUID `json:"message_id"`
	UserID    uuid.UUID `json:"user_id"`
	Username  string    `json:"username"`
	Emoji     string    `json:"emoji"`
	Action    string    `json:"action"` // add, remove
}

// NotificationData represents notification payload
type NotificationData struct {
	ID          uuid.UUID `json:"id"`
	Type        string    `json:"type"`
	ActorID     uuid.UUID `json:"actor_id,omitempty"`
	ActorName   string    `json:"actor_name,omitempty"`
	ActorAvatar string    `json:"actor_avatar,omitempty"`
	ReferenceID uuid.UUID `json:"reference_id,omitempty"`
	Content     string    `json:"content"`
	CreatedAt   time.Time `json:"created_at"`
}

// PresenceData represents user presence payload
type PresenceData struct {
	UserID   uuid.UUID `json:"user_id"`
	Username string    `json:"username"`
	Status   string    `json:"status"` // online, offline, away
}

// ErrorData represents error payload
type ErrorData struct {
	Code    int    `json:"code"`
	Message string `json:"message"`
}

// NewChatMessage creates a new chat message
func NewChatMessage(roomID string, data *ChatMessageData) *Message {
	return &Message{
		Type:      TypeChatMessage,
		RoomID:    roomID,
		SenderID:  data.SenderID,
		Data:      data,
		Timestamp: time.Now(),
	}
}

// NewTypingMessage creates a typing indicator message
func NewTypingMessage(roomID string, data *TypingData) *Message {
	return &Message{
		Type:      TypeChatTyping,
		RoomID:    roomID,
		SenderID:  data.UserID,
		Data:      data,
		Timestamp: time.Now(),
	}
}

// NewNotification creates a notification message
func NewNotification(userID uuid.UUID, data *NotificationData) *Message {
	return &Message{
		Type:      TypeNotification,
		Data:      data,
		Timestamp: time.Now(),
	}
}

// NewError creates an error message
func NewError(code int, message string) *Message {
	return &Message{
		Type:      TypeError,
		Data:      &ErrorData{Code: code, Message: message},
		Timestamp: time.Now(),
	}
}
