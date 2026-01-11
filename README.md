# Edu Platform - CRM & Social Platform

Hệ thống monolith Java CRM với Go microservices, sử dụng DDD architecture.

## 🏗️ Kiến trúc

```
edu-platform/
├── java-crm/              # Java monolith (Spring Boot 4, JDBC, JOOQ)
├── go-services/          # Go microservices
│   └── social-service/   # Social service với DDD
├── kraken-gateway/       # API Gateway (Go)
└── infrastructure/       # Docker, migrations
```

## 🚀 Quick Start

### Prerequisites

- Java 21
- Go 1.25
- Docker & Docker Compose
- PostgreSQL 16
- Gradle 8.7

### Setup

1. **Clone repository và copy environment file:**
```bash
cd edu-platform
cp .env.example .env
# Edit .env với các giá trị phù hợp
```

2. **Start infrastructure:**
```bash
make docker-up
# hoặc
cd infrastructure/docker
docker-compose -f docker-compose.dev.yml up -d
```

3. **Setup Java CRM:**
```bash
cd java-crm
./gradlew build
./gradlew :entity-share:generateJooq  # Generate JOOQ từ database
./gradlew :app:bootRun
```

4. **Setup Go Social Service:**
```bash
cd go-services/social-service
go mod download
go install github.com/pressly/goose/v3/cmd/goose@latest
go install github.com/sqlc-dev/sqlc/cmd/sqlc@latest

# Run migrations
make migrate-up

# Generate sqlc code
make sqlc

# Build and run
make run
```

5. **Setup Kraken Gateway:**
```bash
cd kraken-gateway
go mod download
make run
```

## 📁 Cấu trúc dự án

### Java CRM
- **entity-share**: JOOQ generated code từ database
- **crm-base**: Vert.x + RxJava configuration
- **shared-kernel**: Base entities, value objects
- **security-module**: JWT + OAuth2 + RBAC
- **grpc-service**: gRPC server cho Go services
- **crm-module**: CRM business logic (DDD)

### Go Services
- **social-service**: Social features (Posts, Comments, Likes, Follows)
  - `db/migrations/`: Goose migration files
  - `db/queries/`: SQL queries cho sqlc
  - `db/sqlc/`: Generated code từ sqlc
  - `internal/`: DDD layers (domain, application, infrastructure, api)

## 🔧 Environment Variables

Tất cả biến môi trường được quản lý tập trung trong `.env` file. Xem `.env.example` để biết chi tiết.

## 🐳 Docker

### Development
```bash
docker-compose -f infrastructure/docker/docker-compose.dev.yml up -d
```

### Production
```bash
docker-compose -f infrastructure/docker/docker-compose.yml up -d
```

### Build images
```bash
# Java CRM
docker build -t java-crm:latest -f java-crm/Dockerfile java-crm/

# Go Social Service
docker build -t social-service:latest -f go-services/social-service/Dockerfile go-services/social-service/

# Kraken Gateway
docker build -t kraken-gateway:latest -f kraken-gateway/Dockerfile kraken-gateway/
```

## 🧪 Testing

### Java
```bash
cd java-crm
./gradlew test
```

### Go
```bash
cd go-services/social-service
go test ./...
```

## 📦 Version Management

Tất cả versions được quản lý trong `versions.toml`:
- Java dependencies: `java-crm/versions.toml`
- Go dependencies: trong `go.mod` files
- Shared versions: `versions.toml` (root)

## 🔄 CI/CD

GitHub Actions workflow tự động:
- Build và test Java CRM
- Build và test Go services
- Integration tests với Docker Compose

Xem `.github/workflows/ci.yml` để biết chi tiết.

## 📝 Makefile Commands

```bash
# Root level
make build-java          # Build Java monolith
make build-go            # Build Go services
make docker-up           # Start docker services
make docker-down         # Stop docker services
make generate-jooq        # Generate JOOQ code
make sqlc                 # Generate sqlc code
make migrate-up           # Run migrations
```

## 🛠️ Development Tools

- **JOOQ**: Generate type-safe database code từ PostgreSQL
- **SQLC**: Generate type-safe Go code từ SQL queries
- **Goose**: Database migrations cho Go
- **Viper**: Configuration management cho Go
- **Dig**: Dependency injection cho Go
- **Vert.x + RxJava**: Async processing trong Java

## 📚 Documentation

- [Java CRM README](java-crm/README.md)
- [Go Social Service README](go-services/social-service/README.md)

## 🤝 Contributing

1. Tạo branch từ `develop`
2. Commit changes
3. Push và tạo Pull Request

## 📄 License

[Your License Here]
