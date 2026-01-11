package main

import (
	"context"
	"log"
	"net/http"
	"net/http/httputil"
	"net/url"
	"os"
	"os/signal"
	"strings"
	"syscall"
	"time"

	"github.com/spf13/viper"
)

// Gateway is a simple API gateway using reverse proxy
type Gateway struct {
	mux    *http.ServeMux
	routes []RouteConfig
}

func NewGateway() *Gateway {
	return &Gateway{
		mux:    http.NewServeMux(),
		routes: []RouteConfig{},
	}
}

func (g *Gateway) AddRoute(path, target, stripPrefix string) {
	targetURL, err := url.Parse(target)
	if err != nil {
		log.Printf("Invalid target URL %s: %v", target, err)
		return
	}

	proxy := httputil.NewSingleHostReverseProxy(targetURL)

	// Custom director to strip prefix
	originalDirector := proxy.Director
	proxy.Director = func(req *http.Request) {
		originalDirector(req)
		if stripPrefix != "" {
			req.URL.Path = strings.TrimPrefix(req.URL.Path, stripPrefix)
			if req.URL.Path == "" {
				req.URL.Path = "/"
			}
		}
		req.Host = targetURL.Host
	}

	// Strip trailing wildcard for ServeMux
	pattern := strings.TrimSuffix(path, "*")
	g.mux.HandleFunc(pattern, func(w http.ResponseWriter, r *http.Request) {
		log.Printf("[Gateway] %s %s -> %s", r.Method, r.URL.Path, target)
		proxy.ServeHTTP(w, r)
	})

	g.routes = append(g.routes, RouteConfig{Path: path, Target: target, StripPrefix: stripPrefix})
	log.Printf("Route registered: %s -> %s", path, target)
}

func (g *Gateway) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	g.mux.ServeHTTP(w, r)
}

func main() {
	// Load config with viper
	viper.SetConfigName("config")
	viper.SetConfigType("yaml")
	viper.AddConfigPath(".")
	viper.AddConfigPath("./config")
	viper.AutomaticEnv()
	viper.SetEnvPrefix("KRAKEN")

	viper.SetDefault("server.port", "8000")
	viper.SetDefault("server.host", "0.0.0.0")

	if err := viper.ReadInConfig(); err != nil {
		log.Printf("Config file not found, using defaults: %v", err)
	}

	port := viper.GetString("server.port")
	host := viper.GetString("server.host")

	// Initialize gateway
	gateway := NewGateway()

	// Load routing configuration
	config, err := loadConfig()
	if err != nil {
		log.Fatalf("Failed to load config: %v", err)
	}

	// Setup routes
	setupRoutes(gateway, config)

	// Create HTTP server
	server := &http.Server{
		Addr:    host + ":" + port,
		Handler: gateway,
	}

	// Start server in goroutine
	go func() {
		log.Printf("Kraken Gateway starting on %s:%s", host, port)
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

	log.Println("Server exited")
}

func loadConfig() (*Config, error) {
	// TODO: Load from config file or environment
	return &Config{
		Routes: []RouteConfig{
			{
				Path:        "/api/v1/social/",
				Target:      "http://localhost:8001",
				StripPrefix: "/api/v1/social",
			},
			{
				Path:        "/api/v1/crm/",
				Target:      "http://localhost:8080",
				StripPrefix: "/api/v1/crm",
			},
		},
	}, nil
}

func setupRoutes(gateway *Gateway, config *Config) {
	for _, route := range config.Routes {
		gateway.AddRoute(route.Path, route.Target, route.StripPrefix)
	}
}

type Config struct {
	Routes []RouteConfig
}

type RouteConfig struct {
	Path        string
	Target      string
	StripPrefix string
}
