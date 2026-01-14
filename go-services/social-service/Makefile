.PHONY: build clean test migrate-up migrate-down migrate-reset seed sqlc run dev help

# Variables
DB_URL ?= postgres://root:123456@localhost:5432/social_db?sslmode=disable

# Build the service
build:
	go build -o bin/social-service ./cmd/app/main.go

# Clean build artifacts
clean:
	rm -rf bin/

# Run tests
test:
	go test ./...

# Run migrations up
migrate-up:
	goose -dir db/migrations postgres "$(DB_URL)" up

# Run migrations down
migrate-down:
	goose -dir db/migrations postgres "$(DB_URL)" down

# Reset database (down all then up)
migrate-reset:
	goose -dir db/migrations postgres "$(DB_URL)" reset
	goose -dir db/migrations postgres "$(DB_URL)" up

# Seed database with sample data
seed:
	psql "$(DB_URL)" -f db/seeds/seed.sql

# Generate sqlc code
sqlc:
	sqlc generate

# Run the service (development)
dev:
	go run ./cmd/app/main.go

# Run the service (production build)
run: build
	./bin/social-service

# Setup: migrate and seed
setup: migrate-up seed

# Help
help:
	@echo "Available targets:"
	@echo "  build         - Build the service"
	@echo "  clean         - Clean build artifacts"
	@echo "  test          - Run tests"
	@echo "  migrate-up    - Run migrations up"
	@echo "  migrate-down  - Run migrations down"
	@echo "  migrate-reset - Reset database and run migrations"
	@echo "  seed          - Seed database with sample data"
	@echo "  setup         - Run migrations and seed"
	@echo "  sqlc          - Generate sqlc code from queries"
	@echo "  dev           - Run in development mode"
	@echo "  run           - Build and run the service"
