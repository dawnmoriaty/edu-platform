package main

import (
	"os"
	"os/signal"
	"syscall"

	"social-service/configs"
	"social-service/db"
	ws "social-service/internals/chat/websocket"
	httpServer "social-service/internals/server/http"
	"social-service/pkgs/logger"
	"social-service/pkgs/redis"
)

func main() {
	cfg := configs.LoadConfig()
	logger.Initialize(cfg.Environment)

	logger.Info("Starting Social Service...")

	// Connect to database
	database, err := db.NewDatabase(cfg.DatabaseURI)
	if err != nil {
		logger.Fatal("Cannot connect to database: ", err)
	}
	defer database.Close()

	// Connect to Redis
	redisClient, err := redis.NewRedis(redis.Config{
		Address:  cfg.RedisURI,
		Password: cfg.RedisPassword,
		Database: cfg.RedisDB,
	})
	if err != nil {
		logger.Fatal("Cannot connect to Redis: ", err)
	}

	// Initialize WebSocket hub
	hub := ws.NewHub()
	go hub.Run()

	// Create HTTP server
	server := httpServer.NewServer(database, redisClient, hub)

	// Graceful shutdown
	go func() {
		quit := make(chan os.Signal, 1)
		signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
		<-quit

		logger.Info("Shutting down server...")
		database.Close()
		logger.Sync()
		os.Exit(0)
	}()

	// Run server
	if err := server.Run(); err != nil {
		logger.Fatal("Failed to run server: ", err)
	}
}
