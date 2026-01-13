package http

import (
	"github.com/gin-gonic/gin"

	"social-service/db"
	"social-service/internals/chat/repository"
	"social-service/internals/chat/usecase"
	ws "social-service/internals/chat/websocket"
)

func Routes(r *gin.RouterGroup, database *db.Database, hub *ws.Hub) {
	chatRepo := repository.NewChatRepository(database)
	chatUseCase := usecase.NewChatUseCase(chatRepo)
	chatHandler := NewChatHandler(chatUseCase, hub)

	// WebSocket endpoint
	r.GET("/ws", chatHandler.WebSocketHandler)

	// Conversations
	conversations := r.Group("/conversations")
	{
		conversations.POST("/direct", chatHandler.CreateDirectConversation)
		conversations.POST("/group", chatHandler.CreateGroupConversation)
		conversations.GET("", chatHandler.GetConversations)
		conversations.GET("/:id", chatHandler.GetConversation)
		conversations.POST("/:id/messages", chatHandler.SendMessage)
		conversations.GET("/:id/messages", chatHandler.GetMessages)
		conversations.POST("/:id/read", chatHandler.MarkAsRead)
		conversations.GET("/:id/participants", chatHandler.GetParticipants)
		conversations.POST("/:id/participants", chatHandler.AddParticipant)
		conversations.DELETE("/:id/participants/:userId", chatHandler.RemoveParticipant)
	}
}
