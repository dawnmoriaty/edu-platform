package repository

import (
	"context"
	"fmt"
	"time"

	"social-service/db"
	"social-service/internals/chat/entity"
	"social-service/pkgs/paging"

	"github.com/google/uuid"
	"github.com/jackc/pgx/v5"
)

type ChatRepository interface {
	// Conversation methods
	CreateConversation(ctx context.Context, conversation *entity.Conversation) error
	GetConversationByID(ctx context.Context, id string) (*entity.Conversation, error)
	GetConversationByParticipants(ctx context.Context, userID1, userID2 string) (*entity.Conversation, error)
	GetConversationsByUserID(ctx context.Context, userID string, pageable *paging.Pageable) (*paging.Page[entity.Conversation], error)
	UpdateConversationLastMessage(ctx context.Context, conversationID string, message string, sentAt time.Time) error

	// Message methods
	CreateMessage(ctx context.Context, message *entity.Message) error
	GetMessageByID(ctx context.Context, id string) (*entity.Message, error)
	GetMessagesByConversationID(ctx context.Context, conversationID string, pageable *paging.Pageable) (*paging.Page[entity.Message], error)
	UpdateMessageStatus(ctx context.Context, messageID string, status entity.MessageStatus) error
	MarkMessagesAsRead(ctx context.Context, conversationID, userID string) error

	// Participant methods
	AddParticipant(ctx context.Context, participant *entity.Participant) error
	GetParticipants(ctx context.Context, conversationID string) ([]entity.Participant, error)
	RemoveParticipant(ctx context.Context, conversationID, userID string) error
}

type chatRepository struct {
	db *db.Database
}

func NewChatRepository(database *db.Database) ChatRepository {
	return &chatRepository{db: database}
}

// Conversation methods
func (r *chatRepository) CreateConversation(ctx context.Context, conversation *entity.Conversation) error {
	if conversation.ID == "" {
		conversation.ID = uuid.New().String()
	}
	if conversation.CreatedAt.IsZero() {
		conversation.CreatedAt = time.Now()
	}
	conversation.UpdatedAt = conversation.CreatedAt

	query := `
		INSERT INTO conversations (id, type, name, created_at, updated_at)
		VALUES ($1, $2, $3, $4, $5)
	`
	_, err := r.db.Pool.Exec(ctx, query,
		conversation.ID,
		conversation.Type,
		conversation.Name,
		conversation.CreatedAt,
		conversation.UpdatedAt,
	)
	return err
}

func (r *chatRepository) GetConversationByID(ctx context.Context, id string) (*entity.Conversation, error) {
	query := `
		SELECT id, type, name, last_message, last_message_at, created_at, updated_at
		FROM conversations
		WHERE id = $1
	`
	row := r.db.Pool.QueryRow(ctx, query, id)

	var conv entity.Conversation
	err := row.Scan(
		&conv.ID,
		&conv.Type,
		&conv.Name,
		&conv.LastMessage,
		&conv.LastMessageAt,
		&conv.CreatedAt,
		&conv.UpdatedAt,
	)
	if err != nil {
		if err == pgx.ErrNoRows {
			return nil, nil
		}
		return nil, err
	}
	return &conv, nil
}

func (r *chatRepository) GetConversationByParticipants(ctx context.Context, userID1, userID2 string) (*entity.Conversation, error) {
	query := `
		SELECT c.id, c.type, c.name, c.last_message, c.last_message_at, c.created_at, c.updated_at
		FROM conversations c
		JOIN conversation_participants p1 ON c.id = p1.conversation_id AND p1.user_id = $1
		JOIN conversation_participants p2 ON c.id = p2.conversation_id AND p2.user_id = $2
		WHERE c.type = 'direct'
		LIMIT 1
	`
	row := r.db.Pool.QueryRow(ctx, query, userID1, userID2)

	var conv entity.Conversation
	err := row.Scan(
		&conv.ID,
		&conv.Type,
		&conv.Name,
		&conv.LastMessage,
		&conv.LastMessageAt,
		&conv.CreatedAt,
		&conv.UpdatedAt,
	)
	if err != nil {
		if err == pgx.ErrNoRows {
			return nil, nil
		}
		return nil, err
	}
	return &conv, nil
}

func (r *chatRepository) GetConversationsByUserID(ctx context.Context, userID string, pageable *paging.Pageable) (*paging.Page[entity.Conversation], error) {
	// Count total
	countQuery := `
		SELECT COUNT(DISTINCT c.id)
		FROM conversations c
		JOIN conversation_participants p ON c.id = p.conversation_id
		WHERE p.user_id = $1
	`
	var total int64
	err := r.db.Pool.QueryRow(ctx, countQuery, userID).Scan(&total)
	if err != nil {
		return nil, err
	}
	pageable.SetTotal(total)

	if total == 0 {
		return paging.NewPage(pageable, []entity.Conversation{}), nil
	}

	// Get conversations
	query := `
		SELECT c.id, c.type, c.name, c.last_message, c.last_message_at, c.created_at, c.updated_at
		FROM conversations c
		JOIN conversation_participants p ON c.id = p.conversation_id
		WHERE p.user_id = $1
		ORDER BY COALESCE(c.last_message_at, c.created_at) DESC
		LIMIT $2 OFFSET $3
	`
	rows, err := r.db.Pool.Query(ctx, query, userID, pageable.Limit, pageable.GetOffset())
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var conversations []entity.Conversation
	for rows.Next() {
		var conv entity.Conversation
		err := rows.Scan(
			&conv.ID,
			&conv.Type,
			&conv.Name,
			&conv.LastMessage,
			&conv.LastMessageAt,
			&conv.CreatedAt,
			&conv.UpdatedAt,
		)
		if err != nil {
			return nil, err
		}
		conversations = append(conversations, conv)
	}

	return paging.NewPage(pageable, conversations), nil
}

func (r *chatRepository) UpdateConversationLastMessage(ctx context.Context, conversationID string, message string, sentAt time.Time) error {
	query := `
		UPDATE conversations
		SET last_message = $2, last_message_at = $3, updated_at = $4
		WHERE id = $1
	`
	_, err := r.db.Pool.Exec(ctx, query, conversationID, message, sentAt, time.Now())
	return err
}

// Message methods
func (r *chatRepository) CreateMessage(ctx context.Context, message *entity.Message) error {
	if message.ID == "" {
		message.ID = uuid.New().String()
	}
	if message.CreatedAt.IsZero() {
		message.CreatedAt = time.Now()
	}
	if message.Status == "" {
		message.Status = entity.MessageStatusSent
	}

	query := `
		INSERT INTO messages (id, conversation_id, sender_id, content, message_type, status, created_at)
		VALUES ($1, $2, $3, $4, $5, $6, $7)
	`
	_, err := r.db.Pool.Exec(ctx, query,
		message.ID,
		message.ConversationID,
		message.SenderID,
		message.Content,
		message.MessageType,
		message.Status,
		message.CreatedAt,
	)
	if err != nil {
		return err
	}

	// Update conversation's last message
	return r.UpdateConversationLastMessage(ctx, message.ConversationID, message.Content, message.CreatedAt)
}

func (r *chatRepository) GetMessageByID(ctx context.Context, id string) (*entity.Message, error) {
	query := `
		SELECT id, conversation_id, sender_id, content, message_type, status, created_at
		FROM messages
		WHERE id = $1
	`
	row := r.db.Pool.QueryRow(ctx, query, id)

	var msg entity.Message
	err := row.Scan(
		&msg.ID,
		&msg.ConversationID,
		&msg.SenderID,
		&msg.Content,
		&msg.MessageType,
		&msg.Status,
		&msg.CreatedAt,
	)
	if err != nil {
		if err == pgx.ErrNoRows {
			return nil, nil
		}
		return nil, err
	}
	return &msg, nil
}

func (r *chatRepository) GetMessagesByConversationID(ctx context.Context, conversationID string, pageable *paging.Pageable) (*paging.Page[entity.Message], error) {
	// Count total
	countQuery := `SELECT COUNT(*) FROM messages WHERE conversation_id = $1`
	var total int64
	err := r.db.Pool.QueryRow(ctx, countQuery, conversationID).Scan(&total)
	if err != nil {
		return nil, err
	}
	pageable.SetTotal(total)

	if total == 0 {
		return paging.NewPage(pageable, []entity.Message{}), nil
	}

	// Get messages
	query := `
		SELECT id, conversation_id, sender_id, content, message_type, status, created_at
		FROM messages
		WHERE conversation_id = $1
		ORDER BY created_at DESC
		LIMIT $2 OFFSET $3
	`
	rows, err := r.db.Pool.Query(ctx, query, conversationID, pageable.Limit, pageable.GetOffset())
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var messages []entity.Message
	for rows.Next() {
		var msg entity.Message
		err := rows.Scan(
			&msg.ID,
			&msg.ConversationID,
			&msg.SenderID,
			&msg.Content,
			&msg.MessageType,
			&msg.Status,
			&msg.CreatedAt,
		)
		if err != nil {
			return nil, err
		}
		messages = append(messages, msg)
	}

	return paging.NewPage(pageable, messages), nil
}

func (r *chatRepository) UpdateMessageStatus(ctx context.Context, messageID string, status entity.MessageStatus) error {
	query := `UPDATE messages SET status = $2 WHERE id = $1`
	result, err := r.db.Pool.Exec(ctx, query, messageID, status)
	if err != nil {
		return err
	}
	if result.RowsAffected() == 0 {
		return fmt.Errorf("message not found")
	}
	return nil
}

func (r *chatRepository) MarkMessagesAsRead(ctx context.Context, conversationID, userID string) error {
	query := `
		UPDATE messages
		SET status = $1
		WHERE conversation_id = $2 AND sender_id != $3 AND status != $1
	`
	_, err := r.db.Pool.Exec(ctx, query, entity.MessageStatusRead, conversationID, userID)
	return err
}

// Participant methods
func (r *chatRepository) AddParticipant(ctx context.Context, participant *entity.Participant) error {
	if participant.JoinedAt.IsZero() {
		participant.JoinedAt = time.Now()
	}

	query := `
		INSERT INTO conversation_participants (conversation_id, user_id, role, joined_at)
		VALUES ($1, $2, $3, $4)
		ON CONFLICT (conversation_id, user_id) DO NOTHING
	`
	_, err := r.db.Pool.Exec(ctx, query,
		participant.ConversationID,
		participant.UserID,
		participant.Role,
		participant.JoinedAt,
	)
	return err
}

func (r *chatRepository) GetParticipants(ctx context.Context, conversationID string) ([]entity.Participant, error) {
	query := `
		SELECT conversation_id, user_id, role, joined_at
		FROM conversation_participants
		WHERE conversation_id = $1
	`
	rows, err := r.db.Pool.Query(ctx, query, conversationID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var participants []entity.Participant
	for rows.Next() {
		var p entity.Participant
		err := rows.Scan(&p.ConversationID, &p.UserID, &p.Role, &p.JoinedAt)
		if err != nil {
			return nil, err
		}
		participants = append(participants, p)
	}
	return participants, nil
}

func (r *chatRepository) RemoveParticipant(ctx context.Context, conversationID, userID string) error {
	query := `DELETE FROM conversation_participants WHERE conversation_id = $1 AND user_id = $2`
	_, err := r.db.Pool.Exec(ctx, query, conversationID, userID)
	return err
}
