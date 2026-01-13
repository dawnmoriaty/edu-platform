package websocket

import "time"

// WSMessage represents a WebSocket message
type WSMessage struct {
	Type           string `json:"type"`
	ConversationID string `json:"conversation_id,omitempty"`
	Content        string `json:"content,omitempty"`
	SenderID       string `json:"sender_id,omitempty"`
	MessageID      string `json:"message_id,omitempty"`
}

// OutgoingMessage represents a message sent to clients
type OutgoingMessage struct {
	Type           string    `json:"type"`
	MessageID      string    `json:"message_id,omitempty"`
	ConversationID string    `json:"conversation_id"`
	SenderID       string    `json:"sender_id"`
	Content        string    `json:"content,omitempty"`
	MediaUrl       string    `json:"media_url,omitempty"`
	CreatedAt      time.Time `json:"created_at"`
}

// TypingEvent represents a typing indicator
type TypingEvent struct {
	Type           string `json:"type"`
	ConversationID string `json:"conversation_id"`
	UserID         string `json:"user_id"`
}

// PresenceEvent represents online/offline status
type PresenceEvent struct {
	Type   string `json:"type"`
	UserID string `json:"user_id"`
	Online bool   `json:"online"`
}
