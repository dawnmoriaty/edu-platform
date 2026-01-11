.PHONY: build clean test migrate-up migrate-down sqlc help

# Build the service
build:
	go build -o bin/social-service ./main.go

# Clean build artifacts
clean:
	rm -rf bin/

# Run tests
test:
	go test ./...

# Run migrations up
migrate-up:
	goose -dir db/migrations postgres "host=localhost port=5432 user=root password=123456 dbname=edu_crm_db sslmode=disable" up

# Run migrations down
migrate-down:
	goose -dir db/migrations postgres "host=localhost port=5432 user=root password=123456 dbname=edu_crm_db sslmode=disable" down

# Generate sqlc code
sqlc:
	sqlc generate

# Run the service
run: build
	./bin/social-service

# Help
help:
	@echo "Available targets:"
	@echo "  build        - Build the service"
	@echo "  clean        - Clean build artifacts"
	@echo "  test         - Run tests"
	@echo "  migrate-up   - Run migrations up"
	@echo "  migrate-down - Run migrations down"
	@echo "  sqlc         - Generate sqlc code from queries"
	@echo "  run          - Build and run the service"
