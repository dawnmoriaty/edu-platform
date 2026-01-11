.PHONY: build clean test generate-jooq docker-up docker-down run help

# Build the project
build:
	gradlew build

# Clean build artifacts
clean:
	gradlew clean

# Run tests
test:
	gradlew test

# Generate JOOQ code from database
generate-jooq:
	gradlew :entity-share:jooqCodegen

# Start docker services
docker-up:
	cd ../infrastructure/docker && docker-compose -f docker-compose.dev.yml up -d

# Stop docker services
docker-down:
	cd ../infrastructure/docker && docker-compose -f docker-compose.dev.yml down

# Run the application
run:
	gradlew :app:bootRun

# Build and run
build-run: build run

# Help
help:
	@echo "Available targets:"
	@echo "  build          - Build the project"
	@echo "  clean          - Clean build artifacts"
	@echo "  test           - Run tests"
	@echo "  generate-jooq  - Generate JOOQ code from database"
	@echo "  docker-up      - Start docker services"
	@echo "  docker-down    - Stop docker services"
	@echo "  run            - Run the application"
	@echo "  build-run      - Build and run"
