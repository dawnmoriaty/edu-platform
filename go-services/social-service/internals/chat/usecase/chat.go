package usecase

import (
	"context"
	"errors"
	"time"

	"social-service/internals/chat/entity"
	"social-service/internals/chat/repository"
	"social-service/pkgs/paging"
)

var (
	ErrConversationNotFound = errors.New("conversation not found")
	ErrMessageNotFound      = errors.New("message not found")
	ErrUnauthorized         = errors.New("unauthorized")
)

type ChatUseCase interface {
	// Conversation operations
	CreateDirectConversation(ctx context.Context, userID1, userID2 string) (*entity.Conversation, error)
	CreateGroupConversation(ctx context.Context, name string, creatorID string, participantIDs []string) (*entity.Conversation, error)
	GetConversation(ctx context.Context, conversationID, userID string) (*entity.Conversation, error)
	GetConversations(ctx context.Context, userID string, pageable *paging.Pageable) (*paging.Page[entity.Conversation], error)

	// Message operations
	SendMessage(ctx context.Context, conversationID, senderID, content string, messageType entity.MessageType) (*entity.Message, error)
	GetMessages(ctx context.Context, conversationID, userID string, pageable *paging.Pageable) (*paging.Page[entity.Message], error)
	MarkAsRead(ctx context.Context, conversationID, userID string) error

	// Participant operations
	AddParticipant(ctx context.Context, conversationID, userID, newParticipantID string) error
	RemoveParticipant(ctx context.Context, conversationID, userID, participantID string) error
	GetParticipants(ctx context.Context, conversationID, userID string) ([]entity.Participant, error)
}

type chatUseCase struct {
	chatRepo repository.ChatRepository
}

func NewChatUseCase(chatRepo repository.ChatRepository) ChatUseCase {
	return &chatUseCase{
		chatRepo: chatRepo,
	}
}

// CreateDirectConversation creates a direct conversation between two users
func (u *chatUseCase) CreateDirectConversation(ctx context.Context, userID1, userID2 string) (*entity.Conversation, error) {
	// Check if conversation already exists
	existingConv, err := u.chatRepo.GetConversationByParticipants(ctx, userID1, userID2)
	if err != nil {
		return nil, err
	}
	if existingConv != nil {
		return existingConv, nil
	}

	// Create new conversation
	conv := &entity.Conversation{
		Type:      entity.ConversationTypeDirect,
		CreatedAt: time.Now(),
	}
	if err := u.chatRepo.CreateConversation(ctx, conv); err != nil {
		return nil, err
	}

	// Add participants
	for _, userID := range []string{userID1, userID2} {
		participant := &entity.Participant{
			ConversationID: conv.ID,
			UserID:         userID,
			Role:           entity.ParticipantRoleMember,
			JoinedAt:       time.Now(),
		}
		if err := u.chatRepo.AddParticipant(ctx, participant); err != nil {
			return nil, err
		}
	}

	return conv, nil
}

// CreateGroupConversation creates a group conversation
func (u *chatUseCase) CreateGroupConversation(ctx context.Context, name string, creatorID string, participantIDs []string) (*entity.Conversation, error) {
	conv := &entity.Conversation{
		Type:      entity.ConversationTypeGroup,
		Name:      name,
		CreatedAt: time.Now(),
	}
	if err := u.chatRepo.CreateConversation(ctx, conv); err != nil {
		return nil, err
	}

	// Add creator as admin
	creatorParticipant := &entity.Participant{
		ConversationID: conv.ID,
		UserID:         creatorID,
		Role:           entity.ParticipantRoleAdmin,
		JoinedAt:       time.Now(),
	}
	if err := u.chatRepo.AddParticipant(ctx, creatorParticipant); err != nil {
		return nil, err
	}

	// Add other participants as members
	for _, participantID := range participantIDs {
		if participantID == creatorID {
			continue
		}
		participant := &entity.Participant{
			ConversationID: conv.ID,
			UserID:         participantID,
			Role:           entity.ParticipantRoleMember,
			JoinedAt:       time.Now(),
		}
		if err := u.chatRepo.AddParticipant(ctx, participant); err != nil {
			return nil, err
		}
	}

	return conv, nil
}

// GetConversation gets a conversation by ID
func (u *chatUseCase) GetConversation(ctx context.Context, conversationID, userID string) (*entity.Conversation, error) {
	conv, err := u.chatRepo.GetConversationByID(ctx, conversationID)
	if err != nil {
		return nil, err
	}
	if conv == nil {
		return nil, ErrConversationNotFound
	}

	// Check if user is a participant
	participants, err := u.chatRepo.GetParticipants(ctx, conversationID)
	if err != nil {
		return nil, err
	}

	isParticipant := false
	for _, p := range participants {
		if p.UserID == userID {
			isParticipant = true
			break
		}
	}
	if !isParticipant {
		return nil, ErrUnauthorized
	}

	conv.Participants = participants
	return conv, nil
}

// GetConversations gets all conversations for a user
func (u *chatUseCase) GetConversations(ctx context.Context, userID string, pageable *paging.Pageable) (*paging.Page[entity.Conversation], error) {
	return u.chatRepo.GetConversationsByUserID(ctx, userID, pageable)
}

// SendMessage sends a message to a conversation
func (u *chatUseCase) SendMessage(ctx context.Context, conversationID, senderID, content string, messageType entity.MessageType) (*entity.Message, error) {
	// Verify sender is a participant
	participants, err := u.chatRepo.GetParticipants(ctx, conversationID)
	if err != nil {
		return nil, err
	}

	isParticipant := false
	for _, p := range participants {
		if p.UserID == senderID {
			isParticipant = true
			break
		}
	}
	if !isParticipant {
		return nil, ErrUnauthorized
	}

	msg := &entity.Message{
		ConversationID: conversationID,
		SenderID:       senderID,
		Content:        content,
		MessageType:    messageType,
		Status:         entity.MessageStatusSent,
		CreatedAt:      time.Now(),
	}

	if err := u.chatRepo.CreateMessage(ctx, msg); err != nil {
		return nil, err
	}

	return msg, nil
}

// GetMessages gets messages from a conversation
func (u *chatUseCase) GetMessages(ctx context.Context, conversationID, userID string, pageable *paging.Pageable) (*paging.Page[entity.Message], error) {
	// Verify user is a participant
	participants, err := u.chatRepo.GetParticipants(ctx, conversationID)
	if err != nil {
		return nil, err
	}

	isParticipant := false
	for _, p := range participants {
		if p.UserID == userID {
			isParticipant = true
			break
		}
	}
	if !isParticipant {
		return nil, ErrUnauthorized
	}

	return u.chatRepo.GetMessagesByConversationID(ctx, conversationID, pageable)
}

// MarkAsRead marks messages as read
func (u *chatUseCase) MarkAsRead(ctx context.Context, conversationID, userID string) error {
	// Verify user is a participant
	participants, err := u.chatRepo.GetParticipants(ctx, conversationID)
	if err != nil {
		return err
	}

	isParticipant := false
	for _, p := range participants {
		if p.UserID == userID {
			isParticipant = true
			break
		}
	}
	if !isParticipant {
		return ErrUnauthorized
	}

	return u.chatRepo.MarkMessagesAsRead(ctx, conversationID, userID)
}

// AddParticipant adds a participant to a group conversation
func (u *chatUseCase) AddParticipant(ctx context.Context, conversationID, userID, newParticipantID string) error {
	// Get conversation
	conv, err := u.chatRepo.GetConversationByID(ctx, conversationID)
	if err != nil {
		return err
	}
	if conv == nil {
		return ErrConversationNotFound
	}
	if conv.Type != entity.ConversationTypeGroup {
		return errors.New("cannot add participants to direct conversation")
	}

	// Check if user is admin
	participants, err := u.chatRepo.GetParticipants(ctx, conversationID)
	if err != nil {
		return err
	}

	isAdmin := false
	for _, p := range participants {
		if p.UserID == userID && p.Role == entity.ParticipantRoleAdmin {
			isAdmin = true
			break
		}
	}
	if !isAdmin {
		return ErrUnauthorized
	}

	participant := &entity.Participant{
		ConversationID: conversationID,
		UserID:         newParticipantID,
		Role:           entity.ParticipantRoleMember,
		JoinedAt:       time.Now(),
	}
	return u.chatRepo.AddParticipant(ctx, participant)
}

// RemoveParticipant removes a participant from a group conversation
func (u *chatUseCase) RemoveParticipant(ctx context.Context, conversationID, userID, participantID string) error {
	// Get conversation
	conv, err := u.chatRepo.GetConversationByID(ctx, conversationID)
	if err != nil {
		return err
	}
	if conv == nil {
		return ErrConversationNotFound
	}
	if conv.Type != entity.ConversationTypeGroup {
		return errors.New("cannot remove participants from direct conversation")
	}

	// Check if user is admin or removing self
	participants, err := u.chatRepo.GetParticipants(ctx, conversationID)
	if err != nil {
		return err
	}

	isAdmin := false
	for _, p := range participants {
		if p.UserID == userID && p.Role == entity.ParticipantRoleAdmin {
			isAdmin = true
			break
		}
	}

	if !isAdmin && userID != participantID {
		return ErrUnauthorized
	}

	return u.chatRepo.RemoveParticipant(ctx, conversationID, participantID)
}

// GetParticipants gets all participants in a conversation
func (u *chatUseCase) GetParticipants(ctx context.Context, conversationID, userID string) ([]entity.Participant, error) {
	// Verify user is a participant
	participants, err := u.chatRepo.GetParticipants(ctx, conversationID)
	if err != nil {
		return nil, err
	}

	isParticipant := false
	for _, p := range participants {
		if p.UserID == userID {
			isParticipant = true
			break
		}
	}
	if !isParticipant {
		return nil, ErrUnauthorized
	}

	return participants, nil
}
