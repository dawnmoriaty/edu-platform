package http

import (
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"

	"social-service/configs"
	"social-service/db"
	chatHttp "social-service/internals/chat/controller/http"
	ws "social-service/internals/chat/websocket"
	commentHttp "social-service/internals/comment/controller/http"
	followHttp "social-service/internals/follow/controller/http"
	likeHttp "social-service/internals/like/controller/http"
	postHttp "social-service/internals/post/controller/http"
	"social-service/pkgs/logger"
	"social-service/pkgs/redis"
)

type Server struct {
	engine *gin.Engine
	cfg    *configs.Config
	db     *db.Database
	cache  redis.IRedis
	hub    *ws.Hub
}

func NewServer(
	database *db.Database,
	cache redis.IRedis,
	hub *ws.Hub,
) *Server {
	// Disable Gin's default logging
	gin.SetMode(gin.ReleaseMode)
	engine := gin.New()
	engine.Use(gin.Recovery())

	return &Server{
		engine: engine,
		cfg:    configs.GetConfig(),
		db:     database,
		cache:  cache,
		hub:    hub,
	}
}

func (s *Server) Run() error {
	_ = s.engine.SetTrustedProxies(nil)

	// CORS middleware
	s.engine.Use(corsMiddleware())

	// Health check
	s.engine.GET("/health", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"status": "healthy"})
	})

	// API routes
	if err := s.MapRoutes(); err != nil {
		logger.Fatalf("MapRoutes Error: %v", err)
	}

	// Start HTTP server
	logger.Info("HTTP server is listening on PORT: ", s.cfg.HttpPort)
	if err := s.engine.Run(fmt.Sprintf(":%d", s.cfg.HttpPort)); err != nil {
		logger.Fatalf("Running HTTP server: %v", err)
	}

	return nil
}

func (s *Server) MapRoutes() error {
	api := s.engine.Group("/api/v1")

	// Apply auth middleware to protected routes
	api.Use(authMiddleware())

	pool := s.db.GetPool()
	queries := s.db.GetQueries()

	// Mount feature routes
	postHttp.Routes(api, pool, queries, s.cache)
	commentHttp.Routes(api, pool, queries)
	likeHttp.Routes(api, queries)
	followHttp.Routes(api, pool, queries)
	chatHttp.Routes(api.Group("/chat"), s.db, s.hub)

	return nil
}

func (s *Server) GetEngine() *gin.Engine {
	return s.engine
}

// CORS middleware
func corsMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		c.Header("Access-Control-Allow-Origin", "*")
		c.Header("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS")
		c.Header("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization")
		c.Header("Access-Control-Expose-Headers", "Content-Length")
		c.Header("Access-Control-Allow-Credentials", "true")

		if c.Request.Method == "OPTIONS" {
			c.AbortWithStatus(http.StatusNoContent)
			return
		}

		c.Next()
	}
}

// Auth middleware (simplified - you'll integrate with CRM gRPC later)
func authMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		// Skip auth for WebSocket upgrade (handled separately)
		if c.Request.URL.Path == "/api/v1/ws" {
			c.Next()
			return
		}

		// Get user_id from header (for development)
		// In production, validate JWT and get user from CRM service
		userID := c.GetHeader("X-User-ID")
		if userID != "" {
			c.Set("user_id", userID)
		}

		// Also check Authorization header
		authHeader := c.GetHeader("Authorization")
		if authHeader != "" {
			// TODO: Validate JWT with CRM gRPC service
			// For now, just set a placeholder
			// userID = validateToken(authHeader)
		}

		c.Next()
	}
}
