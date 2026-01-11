package main

import (
	"context"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/eduplatform/go-services/social-service/internal/config"
	"github.com/eduplatform/go-services/social-service/internal/di"
	"github.com/eduplatform/go-services/social-service/internal/infrastructure/database"
)

func main() {
	// Load config
	cfg, err := config.Load()
	if err != nil {
		log.Fatalf("Failed to load config: %v", err)
	}

	// Run migrations
	if err := database.RunMigrations(cfg); err != nil {
		log.Fatalf("Failed to run migrations: %v", err)
	}

	// Build DI container
	container, err := di.New(cfg)
	if err != nil {
		log.Fatalf("Failed to build DI container: %v", err)
	}

	// Setup routes
	if err := container.SetupRoutes(); err != nil {
		log.Fatalf("Failed to setup routes: %v", err)
	}

	// Get router
	router, err := container.GetRouter()
	if err != nil {
		log.Fatalf("Failed to get router: %v", err)
	}

	// Create HTTP server
	server := &http.Server{
		Addr:    cfg.Server.Host + ":" + cfg.Server.Port,
		Handler: router,
	}

	// Start server in goroutine
	go func() {
		log.Printf("Social Service starting on %s:%s", cfg.Server.Host, cfg.Server.Port)
		if err := server.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("Failed to start server: %v", err)
		}
	}()

	// Wait for interrupt signal
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit

	log.Println("Shutting down server...")

	// Graceful shutdown
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	if err := server.Shutdown(ctx); err != nil {
		log.Fatalf("Server forced to shutdown: %v", err)
	}

	log.Println("Server exited gracefully")
}
