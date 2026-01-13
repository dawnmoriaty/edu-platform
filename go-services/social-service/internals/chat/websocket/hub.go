package websocket

import (
	"encoding/json"
	"sync"

	"social-service/pkgs/logger"
)

type Hub struct {
	clients     map[*Client]bool
	userClients map[string]map[*Client]bool // userID -> clients
	convClients map[string]map[*Client]bool // conversationID -> clients
	Register    chan *Client
	Unregister  chan *Client
	JoinConv    chan *JoinRequest
	LeaveConv   chan *LeaveRequest
	broadcast   chan *BroadcastMessage
	mu          sync.RWMutex
}

type JoinRequest struct {
	Client         *Client
	ConversationID string
}

type LeaveRequest struct {
	Client         *Client
	ConversationID string
}

type BroadcastMessage struct {
	ConversationID string
	Message        []byte
	ExcludeUserID  string
}

func NewHub() *Hub {
	return &Hub{
		clients:     make(map[*Client]bool),
		userClients: make(map[string]map[*Client]bool),
		convClients: make(map[string]map[*Client]bool),
		Register:    make(chan *Client),
		Unregister:  make(chan *Client),
		JoinConv:    make(chan *JoinRequest),
		LeaveConv:   make(chan *LeaveRequest),
		broadcast:   make(chan *BroadcastMessage, 256),
	}
}

func (h *Hub) Run() {
	for {
		select {
		case client := <-h.Register:
			h.registerClient(client)
		case client := <-h.Unregister:
			h.unregisterClient(client)
		case req := <-h.JoinConv:
			h.joinConversation(req)
		case req := <-h.LeaveConv:
			h.leaveConversation(req)
		case msg := <-h.broadcast:
			h.broadcastToConversation(msg)
		}
	}
}

func (h *Hub) registerClient(client *Client) {
	h.mu.Lock()
	defer h.mu.Unlock()

	h.clients[client] = true
	if h.userClients[client.UserID] == nil {
		h.userClients[client.UserID] = make(map[*Client]bool)
	}
	h.userClients[client.UserID][client] = true

	logger.Infof("Client registered: %s", client.UserID)
}

func (h *Hub) unregisterClient(client *Client) {
	h.mu.Lock()
	defer h.mu.Unlock()

	if _, ok := h.clients[client]; ok {
		delete(h.clients, client)
		close(client.Send)

		if userClients, ok := h.userClients[client.UserID]; ok {
			delete(userClients, client)
			if len(userClients) == 0 {
				delete(h.userClients, client.UserID)
			}
		}

		// Remove from all conversations
		for convID, clients := range h.convClients {
			delete(clients, client)
			if len(clients) == 0 {
				delete(h.convClients, convID)
			}
		}

		logger.Infof("Client unregistered: %s", client.UserID)
	}
}

func (h *Hub) joinConversation(req *JoinRequest) {
	h.mu.Lock()
	defer h.mu.Unlock()

	if h.convClients[req.ConversationID] == nil {
		h.convClients[req.ConversationID] = make(map[*Client]bool)
	}
	h.convClients[req.ConversationID][req.Client] = true
}

func (h *Hub) leaveConversation(req *LeaveRequest) {
	h.mu.Lock()
	defer h.mu.Unlock()

	if clients, ok := h.convClients[req.ConversationID]; ok {
		delete(clients, req.Client)
		if len(clients) == 0 {
			delete(h.convClients, req.ConversationID)
		}
	}
}

func (h *Hub) broadcastToConversation(msg *BroadcastMessage) {
	h.mu.RLock()
	defer h.mu.RUnlock()

	clients := h.convClients[msg.ConversationID]
	for client := range clients {
		if client.UserID != msg.ExcludeUserID {
			select {
			case client.Send <- msg.Message:
			default:
				// Client buffer full, skip
			}
		}
	}
}

func (h *Hub) BroadcastToConversation(conversationID string, message any, excludeUserID string) {
	data, err := json.Marshal(message)
	if err != nil {
		logger.Errorf("Failed to marshal message: %v", err)
		return
	}

	h.broadcast <- &BroadcastMessage{
		ConversationID: conversationID,
		Message:        data,
		ExcludeUserID:  excludeUserID,
	}
}

func (h *Hub) SendToUser(userID string, message any) {
	h.mu.RLock()
	defer h.mu.RUnlock()

	data, err := json.Marshal(message)
	if err != nil {
		return
	}

	if clients, ok := h.userClients[userID]; ok {
		for client := range clients {
			select {
			case client.Send <- data:
			default:
			}
		}
	}
}

func (h *Hub) HandleMessage(client *Client, msg *WSMessage) {
	switch msg.Type {
	case "join_conversation":
		h.JoinConv <- &JoinRequest{
			Client:         client,
			ConversationID: msg.ConversationID,
		}
	case "leave_conversation":
		h.LeaveConv <- &LeaveRequest{
			Client:         client,
			ConversationID: msg.ConversationID,
		}
	case "typing":
		h.BroadcastToConversation(msg.ConversationID, map[string]any{
			"type":    "typing",
			"user_id": client.UserID,
		}, client.UserID)
	case "stop_typing":
		h.BroadcastToConversation(msg.ConversationID, map[string]any{
			"type":    "stop_typing",
			"user_id": client.UserID,
		}, client.UserID)
	}
}
