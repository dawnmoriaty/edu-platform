# Social Service

Go service implementing social features with DDD architecture.

## Setup

1. Install dependencies:
```bash
go mod download
```

2. Install tools:
```bash
go install github.com/pressly/goose/v3/cmd/goose@latest
go install github.com/sqlc-dev/sqlc/cmd/sqlc@latest
```

3. Run migrations:
```bash
make migrate-up
```

4. Generate sqlc code:
```bash
make sqlc
```

5. Build and run:
```bash
make run
```

## Structure

- `db/migrations/` - Goose migration files
- `db/queries/` - SQL queries for sqlc
- `db/sqlc/` - Generated code from sqlc (do not edit)
- `internal/domain/` - Domain entities
- `internal/application/` - Use cases
- `internal/infrastructure/` - Repositories, database, external services
- `internal/api/` - REST handlers

## Configuration

Configuration is managed via Viper. Set environment variables with `SOCIAL_` prefix or use `config/config.yaml`.
