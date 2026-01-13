package http

import (
	"net/http"

	"social-service/internals/chat/controller/dto"
	"social-service/internals/chat/entity"
	"social-service/internals/chat/usecase"
	ws "social-service/internals/chat/websocket"
	"social-service/pkgs/paging"
	"social-service/pkgs/response"

	"github.com/gin-gonic/gin"
	"github.com/gorilla/websocket"
)

var upgrader = websocket.Upgrader{
	ReadBufferSize:  1024,
	WriteBufferSize: 1024,
	CheckOrigin: func(r *http.Request) bool {
		return true // Allow all origins for development
	},
}

type ChatHandler struct {
	chatUseCase usecase.ChatUseCase
	hub         *ws.Hub
}

func NewChatHandler(chatUseCase usecase.ChatUseCase, hub *ws.Hub) *ChatHandler {
	return &ChatHandler{
		chatUseCase: chatUseCase,
		hub:         hub,
	}
}

// CreateDirectConversation creates a direct conversation between two users
func (h *ChatHandler) CreateDirectConversation(c *gin.Context) {
	userID := c.GetString("user_id")
	if userID == "" {
		response.Error(c, http.StatusUnauthorized, nil, "Unauthorized")
		return
	}

	var req dto.CreateDirectConversationRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.Error(c, http.StatusBadRequest, err, "Invalid request body")
		return
	}

	conv, err := h.chatUseCase.CreateDirectConversation(c.Request.Context(), userID, req.RecipientID)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, err, "Failed to create conversation")
		return
	}

	response.JSON(c, http.StatusCreated, dto.ToConversationResponse(conv))
}

// CreateGroupConversation creates a group conversation
func (h *ChatHandler) CreateGroupConversation(c *gin.Context) {
	userID := c.GetString("user_id")
	if userID == "" {
		response.Error(c, http.StatusUnauthorized, nil, "Unauthorized")
		return
	}

	var req dto.CreateGroupConversationRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.Error(c, http.StatusBadRequest, err, "Invalid request body")
		return
	}

	conv, err := h.chatUseCase.CreateGroupConversation(c.Request.Context(), req.Name, userID, req.ParticipantIDs)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, err, "Failed to create group conversation")
		return
	}

	response.JSON(c, http.StatusCreated, dto.ToConversationResponse(conv))
}

// GetConversations gets all conversations for the current user
func (h *ChatHandler) GetConversations(c *gin.Context) {
	userID := c.GetString("user_id")
	if userID == "" {
		response.Error(c, http.StatusUnauthorized, nil, "Unauthorized")
		return
	}

	pageable := paging.FromContext(c)

	page, err := h.chatUseCase.GetConversations(c.Request.Context(), userID, pageable)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, err, "Failed to get conversations")
		return
	}

	responseItems := make([]dto.ConversationResponse, len(page.Items))
	for i, conv := range page.Items {
		responseItems[i] = *dto.ToConversationResponse(&conv)
	}

	response.JSON(c, http.StatusOK, paging.Of(responseItems, page.Total, page.Page))
}

// GetConversation gets a conversation by ID
func (h *ChatHandler) GetConversation(c *gin.Context) {
	userID := c.GetString("user_id")
	if userID == "" {
		response.Error(c, http.StatusUnauthorized, nil, "Unauthorized")
		return
	}

	conversationID := c.Param("id")

	conv, err := h.chatUseCase.GetConversation(c.Request.Context(), conversationID, userID)
	if err != nil {
		if err == usecase.ErrConversationNotFound {
			response.Error(c, http.StatusNotFound, nil, "Conversation not found")
			return
		}
		if err == usecase.ErrUnauthorized {
			response.Error(c, http.StatusForbidden, nil, "Access denied")
			return
		}
		response.Error(c, http.StatusInternalServerError, err, "Failed to get conversation")
		return
	}

	response.JSON(c, http.StatusOK, dto.ToConversationResponse(conv))
}

// SendMessage sends a message to a conversation
func (h *ChatHandler) SendMessage(c *gin.Context) {
	userID := c.GetString("user_id")
	if userID == "" {
		response.Error(c, http.StatusUnauthorized, nil, "Unauthorized")
		return
	}

	conversationID := c.Param("id")

	var req dto.SendMessageRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.Error(c, http.StatusBadRequest, err, "Invalid request body")
		return
	}

	messageType := entity.MessageTypeText
	if req.MessageType != "" {
		messageType = entity.MessageType(req.MessageType)
	}

	msg, err := h.chatUseCase.SendMessage(c.Request.Context(), conversationID, userID, req.Content, messageType)
	if err != nil {
		if err == usecase.ErrUnauthorized {
			response.Error(c, http.StatusForbidden, nil, "Access denied")
			return
		}
		response.Error(c, http.StatusInternalServerError, err, "Failed to send message")
		return
	}

	// Broadcast message to WebSocket clients
	h.hub.BroadcastToConversation(conversationID, dto.ToMessageResponse(msg), userID)

	response.JSON(c, http.StatusCreated, dto.ToMessageResponse(msg))
}

// GetMessages gets messages from a conversation
func (h *ChatHandler) GetMessages(c *gin.Context) {
	userID := c.GetString("user_id")
	if userID == "" {
		response.Error(c, http.StatusUnauthorized, nil, "Unauthorized")
		return
	}

	conversationID := c.Param("id")
	pageable := paging.FromContext(c)

	page, err := h.chatUseCase.GetMessages(c.Request.Context(), conversationID, userID, pageable)
	if err != nil {
		if err == usecase.ErrUnauthorized {
			response.Error(c, http.StatusForbidden, nil, "Access denied")
			return
		}
		response.Error(c, http.StatusInternalServerError, err, "Failed to get messages")
		return
	}

	responseItems := make([]dto.MessageResponse, len(page.Items))
	for i, msg := range page.Items {
		responseItems[i] = *dto.ToMessageResponse(&msg)
	}

	response.JSON(c, http.StatusOK, paging.Of(responseItems, page.Total, page.Page))
}

// MarkAsRead marks messages as read
func (h *ChatHandler) MarkAsRead(c *gin.Context) {
	userID := c.GetString("user_id")
	if userID == "" {
		response.Error(c, http.StatusUnauthorized, nil, "Unauthorized")
		return
	}

	conversationID := c.Param("id")

	if err := h.chatUseCase.MarkAsRead(c.Request.Context(), conversationID, userID); err != nil {
		if err == usecase.ErrUnauthorized {
			response.Error(c, http.StatusForbidden, nil, "Access denied")
			return
		}
		response.Error(c, http.StatusInternalServerError, err, "Failed to mark messages as read")
		return
	}

	response.JSON(c, http.StatusOK, nil)
}

// AddParticipant adds a participant to a group conversation
func (h *ChatHandler) AddParticipant(c *gin.Context) {
	userID := c.GetString("user_id")
	if userID == "" {
		response.Error(c, http.StatusUnauthorized, nil, "Unauthorized")
		return
	}

	conversationID := c.Param("id")

	var req dto.AddParticipantRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.Error(c, http.StatusBadRequest, err, "Invalid request body")
		return
	}

	if err := h.chatUseCase.AddParticipant(c.Request.Context(), conversationID, userID, req.UserID); err != nil {
		if err == usecase.ErrUnauthorized {
			response.Error(c, http.StatusForbidden, nil, "Access denied")
			return
		}
		if err == usecase.ErrConversationNotFound {
			response.Error(c, http.StatusNotFound, nil, "Conversation not found")
			return
		}
		response.Error(c, http.StatusInternalServerError, err, "Failed to add participant")
		return
	}

	response.JSON(c, http.StatusOK, nil)
}

// RemoveParticipant removes a participant from a group conversation
func (h *ChatHandler) RemoveParticipant(c *gin.Context) {
	userID := c.GetString("user_id")
	if userID == "" {
		response.Error(c, http.StatusUnauthorized, nil, "Unauthorized")
		return
	}

	conversationID := c.Param("id")
	participantID := c.Param("userId")

	if err := h.chatUseCase.RemoveParticipant(c.Request.Context(), conversationID, userID, participantID); err != nil {
		if err == usecase.ErrUnauthorized {
			response.Error(c, http.StatusForbidden, nil, "Access denied")
			return
		}
		if err == usecase.ErrConversationNotFound {
			response.Error(c, http.StatusNotFound, nil, "Conversation not found")
			return
		}
		response.Error(c, http.StatusInternalServerError, err, "Failed to remove participant")
		return
	}

	response.JSON(c, http.StatusOK, nil)
}

// GetParticipants gets all participants in a conversation
func (h *ChatHandler) GetParticipants(c *gin.Context) {
	userID := c.GetString("user_id")
	if userID == "" {
		response.Error(c, http.StatusUnauthorized, nil, "Unauthorized")
		return
	}

	conversationID := c.Param("id")

	participants, err := h.chatUseCase.GetParticipants(c.Request.Context(), conversationID, userID)
	if err != nil {
		if err == usecase.ErrUnauthorized {
			response.Error(c, http.StatusForbidden, nil, "Access denied")
			return
		}
		response.Error(c, http.StatusInternalServerError, err, "Failed to get participants")
		return
	}

	responseItems := make([]dto.ParticipantResponse, len(participants))
	for i, p := range participants {
		responseItems[i] = *dto.ToParticipantResponse(&p)
	}

	response.JSON(c, http.StatusOK, responseItems)
}

// WebSocketHandler handles WebSocket connections for real-time chat
func (h *ChatHandler) WebSocketHandler(c *gin.Context) {
	userID := c.GetString("user_id")
	if userID == "" {
		response.Error(c, http.StatusUnauthorized, nil, "Unauthorized")
		return
	}

	conn, err := upgrader.Upgrade(c.Writer, c.Request, nil)
	if err != nil {
		return
	}

	client := ws.NewClient(h.hub, conn, userID)
	h.hub.Register <- client

	go client.WritePump()
	go client.ReadPump()
}
