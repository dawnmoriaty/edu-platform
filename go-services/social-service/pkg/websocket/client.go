package websocket

import (
	"encoding/json"
	"log"
	"sync"
	"time"

	"github.com/google/uuid"
	"github.com/gorilla/websocket"
)

const (
	writeWait      = 10 * time.Second
	pongWait       = 60 * time.Second
	pingPeriod     = (pongWait * 9) / 10
	maxMessageSize = 512 * 1024 // 512KB
)

// Client represents a WebSocket client
type Client struct {
	ID     uuid.UUID
	UserID uuid.UUID
	Hub    *Hub
	Conn   *websocket.Conn
	Send   chan []byte
	Rooms  map[string]bool // conversation IDs
	mu     sync.RWMutex
}

// NewClient creates a new WebSocket client
func NewClient(userID uuid.UUID, hub *Hub, conn *websocket.Conn) *Client {
	return &Client{
		ID:     uuid.New(),
		UserID: userID,
		Hub:    hub,
		Conn:   conn,
		Send:   make(chan []byte, 256),
		Rooms:  make(map[string]bool),
	}
}

// JoinRoom adds client to a room (conversation)
func (c *Client) JoinRoom(roomID string) {
	c.mu.Lock()
	defer c.mu.Unlock()
	c.Rooms[roomID] = true
	c.Hub.JoinRoom(c, roomID)
}

// LeaveRoom removes client from a room
func (c *Client) LeaveRoom(roomID string) {
	c.mu.Lock()
	defer c.mu.Unlock()
	delete(c.Rooms, roomID)
	c.Hub.LeaveRoom(c, roomID)
}

// IsInRoom checks if client is in a room
func (c *Client) IsInRoom(roomID string) bool {
	c.mu.RLock()
	defer c.mu.RUnlock()
	return c.Rooms[roomID]
}

// ReadPump pumps messages from the websocket connection to the hub
func (c *Client) ReadPump() {
	defer func() {
		c.Hub.Unregister <- c
		c.Conn.Close()
	}()

	c.Conn.SetReadLimit(maxMessageSize)
	c.Conn.SetReadDeadline(time.Now().Add(pongWait))
	c.Conn.SetPongHandler(func(string) error {
		c.Conn.SetReadDeadline(time.Now().Add(pongWait))
		return nil
	})

	for {
		_, message, err := c.Conn.ReadMessage()
		if err != nil {
			if websocket.IsUnexpectedCloseError(err, websocket.CloseGoingAway, websocket.CloseAbnormalClosure) {
				log.Printf("error: %v", err)
			}
			break
		}

		// Parse and handle message
		var msg Message
		if err := json.Unmarshal(message, &msg); err != nil {
			log.Printf("error unmarshaling message: %v", err)
			continue
		}

		msg.SenderID = c.UserID
		msg.Timestamp = time.Now()

		c.Hub.HandleMessage(c, &msg)
	}
}

// WritePump pumps messages from the hub to the websocket connection
func (c *Client) WritePump() {
	ticker := time.NewTicker(pingPeriod)
	defer func() {
		ticker.Stop()
		c.Conn.Close()
	}()

	for {
		select {
		case message, ok := <-c.Send:
			c.Conn.SetWriteDeadline(time.Now().Add(writeWait))
			if !ok {
				c.Conn.WriteMessage(websocket.CloseMessage, []byte{})
				return
			}

			w, err := c.Conn.NextWriter(websocket.TextMessage)
			if err != nil {
				return
			}
			w.Write(message)

			// Add queued messages to the current websocket message
			n := len(c.Send)
			for i := 0; i < n; i++ {
				w.Write([]byte{'\n'})
				w.Write(<-c.Send)
			}

			if err := w.Close(); err != nil {
				return
			}

		case <-ticker.C:
			c.Conn.SetWriteDeadline(time.Now().Add(writeWait))
			if err := c.Conn.WriteMessage(websocket.PingMessage, nil); err != nil {
				return
			}
		}
	}
}

// SendJSON sends a JSON message to the client
func (c *Client) SendJSON(v interface{}) error {
	data, err := json.Marshal(v)
	if err != nil {
		return err
	}

	select {
	case c.Send <- data:
		return nil
	default:
		return nil // Buffer full, drop message
	}
}
