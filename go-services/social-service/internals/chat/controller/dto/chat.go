package dto

import (
	"social-service/internals/chat/entity"
	"time"
)

// Conversation DTOs
type CreateDirectConversationRequest struct {
	RecipientID string `json:"recipient_id" binding:"required"`
}

type CreateGroupConversationRequest struct {
	Name           string   `json:"name" binding:"required"`
	ParticipantIDs []string `json:"participant_ids" binding:"required,min=1"`
}

type ConversationResponse struct {
	ID            string                `json:"id"`
	Type          string                `json:"type"`
	Name          string                `json:"name,omitempty"`
	AvatarUrl     string                `json:"avatar_url,omitempty"`
	LastMessage   string                `json:"last_message,omitempty"`
	LastMessageAt *time.Time            `json:"last_message_at,omitempty"`
	CreatedAt     time.Time             `json:"created_at"`
	UpdatedAt     time.Time             `json:"updated_at"`
	Participants  []ParticipantResponse `json:"participants,omitempty"`
}

// Message DTOs
type SendMessageRequest struct {
	Content     string `json:"content" binding:"required"`
	MessageType string `json:"message_type"`
	MediaUrl    string `json:"media_url"`
}

type MessageResponse struct {
	ID             string    `json:"id"`
	ConversationID string    `json:"conversation_id"`
	SenderID       string    `json:"sender_id"`
	MessageType    string    `json:"message_type"`
	Content        string    `json:"content,omitempty"`
	MediaUrl       string    `json:"media_url,omitempty"`
	Status         string    `json:"status"`
	IsEdited       bool      `json:"is_edited"`
	CreatedAt      time.Time `json:"created_at"`
}

// Participant DTOs
type AddParticipantRequest struct {
	UserID string `json:"user_id" binding:"required"`
}

type ParticipantResponse struct {
	UserID   string    `json:"user_id"`
	Role     string    `json:"role"`
	JoinedAt time.Time `json:"joined_at"`
}

// Conversion functions
func ToConversationResponse(c *entity.Conversation) *ConversationResponse {
	resp := &ConversationResponse{
		ID:            c.ID,
		Type:          string(c.Type),
		Name:          c.Name,
		AvatarUrl:     c.AvatarUrl,
		LastMessage:   c.LastMessage,
		LastMessageAt: c.LastMessageAt,
		CreatedAt:     c.CreatedAt,
		UpdatedAt:     c.UpdatedAt,
	}

	if len(c.Participants) > 0 {
		resp.Participants = make([]ParticipantResponse, len(c.Participants))
		for i, p := range c.Participants {
			resp.Participants[i] = *ToParticipantResponse(&p)
		}
	}

	return resp
}

func ToMessageResponse(m *entity.Message) *MessageResponse {
	return &MessageResponse{
		ID:             m.ID,
		ConversationID: m.ConversationID,
		SenderID:       m.SenderID,
		MessageType:    string(m.MessageType),
		Content:        m.Content,
		MediaUrl:       m.MediaUrl,
		Status:         string(m.Status),
		IsEdited:       m.IsEdited,
		CreatedAt:      m.CreatedAt,
	}
}

func ToParticipantResponse(p *entity.Participant) *ParticipantResponse {
	return &ParticipantResponse{
		UserID:   p.UserID,
		Role:     string(p.Role),
		JoinedAt: p.JoinedAt,
	}
}
