.PHONY: build clean test run help

# Build the gateway
build:
	go build -o bin/kraken-gateway ./main.go

# Clean build artifacts
clean:
	rm -rf bin/

# Run tests
test:
	go test ./...

# Run the gateway
run: build
	./bin/kraken-gateway

# Help
help:
	@echo "Available targets:"
	@echo "  build  - Build the gateway"
	@echo "  clean  - Clean build artifacts"
	@echo "  test   - Run tests"
	@echo "  run    - Build and run the gateway"
