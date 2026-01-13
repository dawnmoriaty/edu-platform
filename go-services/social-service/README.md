# Social Service

Go service implementing social features with DDD architecture. Communicates with Java CRM service via gRPC for authentication.

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Go Social Service                         │
├─────────────────────────────────────────────────────────────────┤
│  API Layer (Gin)                                                 │
│  ├── handlers/      → BaseHandler (response helpers)            │
│  ├── dto/           → Request/Response DTOs                      │
│  ├── middleware/    → Auth middleware (gRPC client)              │
│  └── routes.go      → Public + Protected routes                  │
├─────────────────────────────────────────────────────────────────┤
│  Application Layer                                               │
│  └── *_service.go   → Business logic                            │
├─────────────────────────────────────────────────────────────────┤
│  Domain Layer                                                    │
│  └── *.go           → Domain entities                           │
├─────────────────────────────────────────────────────────────────┤
│  Infrastructure Layer                                            │
│  ├── repository/    → PostgreSQL repositories                   │
│  ├── database/      → DB connection & migrations                │
│  └── grpc/          → gRPC clients                              │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ gRPC (validate token, check permission)
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Java CRM Service                             │
│  ├── AuthService    → Login, Register, JWT generation           │
│  └── gRPC Server    → Token validation, permission check        │
└─────────────────────────────────────────────────────────────────┘
```

## Authentication Flow

1. User logs in via Java CRM → receives JWT token
2. User calls Go Social API with `Authorization: Bearer <token>`
3. Go service calls Java via gRPC to validate token
4. If valid, user info is set in Gin context
5. Handler can access user via `h.RequireUserID()` or `h.GetUserInfo()`

## Setup

1. Install dependencies:
```bash
go mod download
```

2. Install tools:
```bash
go install github.com/pressly/goose/v3/cmd/goose@latest
go install github.com/sqlc-dev/sqlc/cmd/sqlc@latest
go install google.golang.org/protobuf/cmd/protoc-gen-go@latest
go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@latest
```

3. Generate gRPC code (if proto files changed):
```bash
make proto
```

4. Run migrations:
```bash
make migrate-up
```

5. Generate sqlc code:
```bash
make sqlc
```

6. Build and run:
```bash
make run
```

## Project Structure

```
internal/
├── api/
│   ├── dto/              # Request/Response DTOs (tách riêng, không inner)
│   │   ├── request.go
│   │   └── response.go
│   ├── handlers/         # HTTP handlers
│   │   ├── base_handler.go    # Common helpers
│   │   ├── post_handler.go
│   │   ├── comment_handler.go
│   │   ├── like_handler.go
│   │   └── follow_handler.go
│   ├── middleware/       # Gin middlewares
│   │   ├── auth.go       # gRPC auth middleware
│   │   └── permission.go # Permission constants
│   └── routes.go         # Route definitions
├── application/          # Business logic services
├── domain/               # Domain entities
├── grpc/                 # gRPC clients
│   ├── auth_client.go    # Auth client wrapper
│   └── authpb/           # Generated protobuf code
├── infrastructure/
│   ├── database/         # DB connection
│   └── repository/       # Data access
├── di/                   # Dependency injection
├── mapper/               # Entity mappers
└── paging/               # Pagination helpers
proto/
└── auth.proto            # Auth service proto definition
```

## Handler Pattern

Handlers embed `BaseHandler` for common functionality:

```go
type PostHandler struct {
    BaseHandler                         // Embed base handler
    postService *application.PostService
}

// READ - public endpoint
func (h *PostHandler) GetPost(c *gin.Context) {
    id, ok := h.ParseUUID(c, "id")
    if !ok { return }

    post, err := h.postService.GetPost(c.Request.Context(), id)
    if err != nil {
        h.NotFound(c, "Post not found")
        return
    }

    h.OK(c, post)
}

// WRITE - requires auth
func (h *PostHandler) CreatePost(c *gin.Context) {
    userID, ok := h.RequireUserID(c)  // Returns 401 if not authenticated
    if !ok { return }

    var req dto.CreatePostRequest     // DTOs in separate package
    if !h.BindJSON(c, &req) { return }

    post, err := h.postService.CreatePost(c.Request.Context(), userID, req.Content)
    if err != nil {
        h.InternalError(c, err)
        return
    }

    h.Created(c, post)
}
```

## BaseHandler Methods

| Method | Description |
|--------|-------------|
| `OK(c, data)` | 200 response with data |
| `Created(c, data)` | 201 response |
| `Message(c, msg)` | 200 with message only |
| `NoContent(c)` | 204 response |
| `BadRequest(c, msg)` | 400 error |
| `Unauthorized(c)` | 401 error |
| `Forbidden(c)` | 403 error |
| `NotFound(c, msg)` | 404 error |
| `InternalError(c, err)` | 500 error |
| `RequireUserID(c)` | Get user ID or return 401 |
| `GetUserID(c)` | Get user ID (optional) |
| `GetUserInfo(c)` | Get full user info |
| `ParseUUID(c, param)` | Parse UUID from URL param |
| `BindJSON(c, &obj)` | Bind and validate JSON |

## Configuration

Configuration via Viper (`config/config.yaml` or environment variables):

```yaml
server:
  port: "8001"
  host: "0.0.0.0"

database:
  host: "localhost"
  port: "5432"
  user: "root"
  password: "123456"
  dbname: "edu_crm_db"

grpc:
  java_host: "localhost"   # Java CRM service host
  java_port: "9090"        # Java CRM gRPC port
```

## Routes

### Public (no auth required)
- `GET /api/v1/posts/:id` - Get post
- `GET /api/v1/users/:user_id/posts` - Get user's posts
- `GET /api/v1/comments/:id` - Get comment
- `GET /api/v1/posts/:post_id/comments` - Get post's comments
- `GET /api/v1/posts/:post_id/likes/count` - Get like count
- `GET /api/v1/users/:user_id/followers` - Get followers
- `GET /api/v1/users/:user_id/following` - Get following

### Protected (requires JWT)
- `POST /api/v1/posts` - Create post
- `PUT /api/v1/posts/:id` - Update post
- `DELETE /api/v1/posts/:id` - Delete post
- `GET /api/v1/feed` - Get feed
- `POST /api/v1/comments` - Create comment
- `POST /api/v1/likes` - Like post
- `DELETE /api/v1/posts/:post_id/likes` - Unlike post
- `POST /api/v1/follows` - Follow user
- `DELETE /api/v1/users/:user_id/follow` - Unfollow user
