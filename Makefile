.PHONY: help build-java build-go docker-up docker-down generate-jooq sqlc migrate-up migrate-down clean test

# Default target
help:
	@echo "Edu Platform - Makefile Commands"
	@echo ""
	@echo "Available targets:"
	@echo "  build-java      - Build Java monolith"
	@echo "  build-go         - Build Go services"
	@echo "  docker-up        - Start docker services (dev)"
	@echo "  docker-down      - Stop docker services"
	@echo "  docker-up-prod   - Start docker services (prod)"
	@echo "  generate-jooq   - Generate JOOQ code from database"
	@echo "  sqlc             - Generate sqlc code from queries"
	@echo "  migrate-up       - Run database migrations"
	@echo "  migrate-down     - Rollback database migrations"
	@echo "  clean            - Clean build artifacts"
	@echo "  test             - Run all tests"
	@echo "  setup            - Initial setup (copy .env, install deps)"

# Build Java monolith
build-java:
	@echo "Building Java CRM..."
	cd java-crm && ./gradlew build

# Build Go services
build-go:
	@echo "Building Go services..."
	cd go-services/social-service && make build
	cd kraken-gateway && make build

# Start docker services (development)
docker-up:
	@echo "Starting docker services (dev)..."
	cd infrastructure/docker && docker-compose -f docker-compose.dev.yml up -d

# Start docker services (production)
docker-up-prod:
	@echo "Starting docker services (prod)..."
	cd infrastructure/docker && docker-compose -f docker-compose.yml up -d

# Stop docker services
docker-down:
	@echo "Stopping docker services..."
	cd infrastructure/docker && docker-compose -f docker-compose.dev.yml down || true
	cd infrastructure/docker && docker-compose -f docker-compose.yml down || true

# Generate JOOQ code from database
generate-jooq:
	@echo "Generating JOOQ code..."
	cd java-crm && ./gradlew :entity-share:generateJooq

# Generate sqlc code
sqlc:
	@echo "Generating sqlc code..."
	cd go-services/social-service && make sqlc

# Run migrations
migrate-up:
	@echo "Running migrations..."
	cd go-services/social-service && make migrate-up

# Rollback migrations
migrate-down:
	@echo "Rolling back migrations..."
	cd go-services/social-service && make migrate-down

# Clean build artifacts
clean:
	@echo "Cleaning build artifacts..."
	cd java-crm && ./gradlew clean
	cd go-services/social-service && make clean
	cd kraken-gateway && make clean

# Run all tests
test:
	@echo "Running tests..."
	cd java-crm && ./gradlew test
	cd go-services/social-service && make test
	cd kraken-gateway && make test

# Initial setup
setup:
	@echo "Setting up project..."
	@if [ ! -f .env ]; then \
		cp .env.example .env; \
		echo "Created .env file from .env.example"; \
		echo "Please edit .env with your configuration"; \
	else \
		echo ".env file already exists"; \
	fi
	@echo "Installing Go tools..."
	@go install github.com/pressly/goose/v3/cmd/goose@latest || true
	@go install github.com/sqlc-dev/sqlc/cmd/sqlc@latest || true
	@echo "Setup complete!"
