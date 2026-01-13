package websocket

import (
	"encoding/json"
	"log"
	"sync"

	"github.com/google/uuid"
)

// Hub maintains the set of active clients and broadcasts messages
type Hub struct {
	// Registered clients
	clients map[*Client]bool

	// Clients indexed by user ID (one user can have multiple connections)
	userClients map[uuid.UUID]map[*Client]bool

	// Rooms (conversations)
	rooms map[string]map[*Client]bool

	// Register requests from clients
	Register chan *Client

	// Unregister requests from clients
	Unregister chan *Client

	// Message handler
	messageHandler MessageHandler

	mu sync.RWMutex
}

// MessageHandler processes incoming messages
type MessageHandler interface {
	HandleChatMessage(client *Client, msg *Message) error
	HandleTyping(client *Client, msg *Message) error
	HandleReadReceipt(client *Client, msg *Message) error
	HandleReaction(client *Client, msg *Message) error
}

// NewHub creates a new Hub
func NewHub(handler MessageHandler) *Hub {
	return &Hub{
		clients:        make(map[*Client]bool),
		userClients:    make(map[uuid.UUID]map[*Client]bool),
		rooms:          make(map[string]map[*Client]bool),
		Register:       make(chan *Client),
		Unregister:     make(chan *Client),
		messageHandler: handler,
	}
}

// Run starts the hub
func (h *Hub) Run() {
	for {
		select {
		case client := <-h.Register:
			h.registerClient(client)

		case client := <-h.Unregister:
			h.unregisterClient(client)
		}
	}
}

func (h *Hub) registerClient(client *Client) {
	h.mu.Lock()
	defer h.mu.Unlock()

	h.clients[client] = true

	// Add to user clients map
	if h.userClients[client.UserID] == nil {
		h.userClients[client.UserID] = make(map[*Client]bool)
	}
	h.userClients[client.UserID][client] = true

	log.Printf("Client registered: userID=%s, clientID=%s", client.UserID, client.ID)

	// Broadcast user online
	h.broadcastPresence(client.UserID, "online")
}

func (h *Hub) unregisterClient(client *Client) {
	h.mu.Lock()
	defer h.mu.Unlock()

	if _, ok := h.clients[client]; ok {
		delete(h.clients, client)
		close(client.Send)

		// Remove from user clients
		if clients, ok := h.userClients[client.UserID]; ok {
			delete(clients, client)
			if len(clients) == 0 {
				delete(h.userClients, client.UserID)
				// Broadcast user offline only when all connections closed
				h.broadcastPresence(client.UserID, "offline")
			}
		}

		// Remove from all rooms
		for roomID := range client.Rooms {
			if room, ok := h.rooms[roomID]; ok {
				delete(room, client)
			}
		}

		log.Printf("Client unregistered: userID=%s, clientID=%s", client.UserID, client.ID)
	}
}

// JoinRoom adds a client to a room
func (h *Hub) JoinRoom(client *Client, roomID string) {
	h.mu.Lock()
	defer h.mu.Unlock()

	if h.rooms[roomID] == nil {
		h.rooms[roomID] = make(map[*Client]bool)
	}
	h.rooms[roomID][client] = true
	client.Rooms[roomID] = true

	log.Printf("Client joined room: userID=%s, roomID=%s", client.UserID, roomID)
}

// LeaveRoom removes a client from a room
func (h *Hub) LeaveRoom(client *Client, roomID string) {
	h.mu.Lock()
	defer h.mu.Unlock()

	if room, ok := h.rooms[roomID]; ok {
		delete(room, client)
		if len(room) == 0 {
			delete(h.rooms, roomID)
		}
	}
	delete(client.Rooms, roomID)

	log.Printf("Client left room: userID=%s, roomID=%s", client.UserID, roomID)
}

// BroadcastToRoom sends a message to all clients in a room
func (h *Hub) BroadcastToRoom(roomID string, message *Message) {
	h.mu.RLock()
	defer h.mu.RUnlock()

	room, ok := h.rooms[roomID]
	if !ok {
		return
	}

	data, err := json.Marshal(message)
	if err != nil {
		log.Printf("Error marshaling message: %v", err)
		return
	}

	for client := range room {
		select {
		case client.Send <- data:
		default:
			// Buffer full, client too slow
		}
	}
}

// BroadcastToRoomExcept sends a message to all clients in a room except one user
func (h *Hub) BroadcastToRoomExcept(roomID string, exceptUserID uuid.UUID, message *Message) {
	h.mu.RLock()
	defer h.mu.RUnlock()

	room, ok := h.rooms[roomID]
	if !ok {
		return
	}

	data, err := json.Marshal(message)
	if err != nil {
		log.Printf("Error marshaling message: %v", err)
		return
	}

	for client := range room {
		if client.UserID != exceptUserID {
			select {
			case client.Send <- data:
			default:
			}
		}
	}
}

// SendToUser sends a message to all connections of a user
func (h *Hub) SendToUser(userID uuid.UUID, message *Message) {
	h.mu.RLock()
	defer h.mu.RUnlock()

	clients, ok := h.userClients[userID]
	if !ok {
		return
	}

	data, err := json.Marshal(message)
	if err != nil {
		log.Printf("Error marshaling message: %v", err)
		return
	}

	for client := range clients {
		select {
		case client.Send <- data:
		default:
		}
	}
}

// IsUserOnline checks if a user has any active connections
func (h *Hub) IsUserOnline(userID uuid.UUID) bool {
	h.mu.RLock()
	defer h.mu.RUnlock()

	clients, ok := h.userClients[userID]
	return ok && len(clients) > 0
}

// GetOnlineUsers returns list of online user IDs
func (h *Hub) GetOnlineUsers() []uuid.UUID {
	h.mu.RLock()
	defer h.mu.RUnlock()

	users := make([]uuid.UUID, 0, len(h.userClients))
	for userID := range h.userClients {
		users = append(users, userID)
	}
	return users
}

func (h *Hub) broadcastPresence(userID uuid.UUID, status string) {
	msg := &Message{
		Type: TypeUserOnline,
		Data: &PresenceData{
			UserID: userID,
			Status: status,
		},
	}
	if status == "offline" {
		msg.Type = TypeUserOffline
	}

	data, _ := json.Marshal(msg)

	// Broadcast to all clients
	for client := range h.clients {
		select {
		case client.Send <- data:
		default:
		}
	}
}

// HandleMessage processes incoming messages from clients
func (h *Hub) HandleMessage(client *Client, msg *Message) {
	if h.messageHandler == nil {
		return
	}

	var err error
	switch msg.Type {
	case TypeChatMessage:
		err = h.messageHandler.HandleChatMessage(client, msg)
	case TypeChatTyping:
		err = h.messageHandler.HandleTyping(client, msg)
	case TypeChatRead:
		err = h.messageHandler.HandleReadReceipt(client, msg)
	case TypeChatReaction:
		err = h.messageHandler.HandleReaction(client, msg)
	case TypeJoinRoom:
		if msg.RoomID != "" {
			client.JoinRoom(msg.RoomID)
		}
	case TypeLeaveRoom:
		if msg.RoomID != "" {
			client.LeaveRoom(msg.RoomID)
		}
	case TypePing:
		client.SendJSON(&Message{Type: TypePong})
	}

	if err != nil {
		client.SendJSON(NewError(500, err.Error()))
	}
}
