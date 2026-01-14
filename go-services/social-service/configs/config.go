package configs

import (
	"os"
	"strconv"
	"time"

	"github.com/joho/godotenv"
)

const (
	ProductionEnv  = "production"
	DevelopmentEnv = "development"
)

var (
	DatabaseTimeout = 5 * time.Second
	PostCachingTime = 5 * time.Minute
	FeedCachingTime = 2 * time.Minute
	UserCachingTime = 10 * time.Minute
)

type Config struct {
	Environment   string
	HttpPort      int
	GrpcPort      int
	DatabaseURI   string
	RedisURI      string
	RedisPassword string
	RedisDB       int

	// CRM gRPC Config
	CrmGrpcHost string
	CrmGrpcPort int

	// JWT Config
	JwtSecret string
	JwtExpiry time.Duration
}

var cfg *Config

func LoadConfig() *Config {
	_ = godotenv.Load()

	cfg = &Config{
		Environment:   getEnv("APP_ENV", DevelopmentEnv),
		HttpPort:      getEnvInt("HTTP_PORT", 8081),
		GrpcPort:      getEnvInt("GRPC_PORT", 50052),
		DatabaseURI:   getEnv("DATABASE_URL", "postgres://root:123456@localhost:5432/social_db?sslmode=disable"),
		RedisURI:      getEnv("REDIS_URI", "localhost:6379"),
		RedisPassword: getEnv("REDIS_PASSWORD", ""),
		RedisDB:       getEnvInt("REDIS_DB", 0),
		CrmGrpcHost:   getEnv("CRM_GRPC_HOST", "localhost"),
		CrmGrpcPort:   getEnvInt("CRM_GRPC_PORT", 50051),
		JwtSecret:     getEnv("JWT_SECRET", "your-secret-key"),
		JwtExpiry:     time.Duration(getEnvInt("JWT_EXPIRY_HOURS", 24)) * time.Hour,
	}

	return cfg
}

func GetConfig() *Config {
	if cfg == nil {
		return LoadConfig()
	}
	return cfg
}

func getEnv(key, defaultValue string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return defaultValue
}

func getEnvInt(key string, defaultValue int) int {
	if value := os.Getenv(key); value != "" {
		if intValue, err := strconv.Atoi(value); err == nil {
			return intValue
		}
	}
	return defaultValue
}
