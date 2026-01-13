# Vert.x Resource Pattern

## Overview

Pattern dùng cho tất cả REST controllers trong Java và Go services.

## BaseResource Methods

| Method | Use Case | Example |
|--------|----------|---------|
| `query(() -> ...)` | GET single item hoặc list không phân trang | `query(() -> service.getById(id))` |
| `page(list, count, pageable)` | GET list có phân trang | `page(() -> service.list(p), () -> service.count(), p)` |
| `execute(principal, user -> ...)` | CREATE, UPDATE, DELETE cần audit | `execute(p, u -> service.create(req, u))` |
| `ok(data)` | Wrap response | `return Single.just(ok(data))` |
| `error("msg")` | 400 error | `return error("Invalid input")` |
| `notFound("msg")` | 404 error | `return notFound("User not found")` |
| `getUser(principal)` | Get current user | `getUser(p).flatMap(...)` |

## Usage Pattern

### READ Operations (không cần principal)

```java
// List with pagination
@VertxGet("/api/v1/categories")
@RequirePermission(resource = "CATEGORY", action = Action.VIEW)
public Single<Response<Page<Category>>> list(Pageable pageable) {
    return page(
        () -> service.list(pageable),
        () -> service.count(),
        pageable
    );
}

// Get by ID
@VertxGet("/api/v1/categories/:id")
@RequirePermission(resource = "CATEGORY", action = Action.VIEW)
public Single<Response<Category>> getById(@VertxPathVariable("id") UUID id) {
    return query(() -> service.getById(id));
}
```

### WRITE Operations (cần principal để audit)

```java
// Create
@VertxPost("/api/v1/categories")
@RequirePermission(resource = "CATEGORY", action = Action.ADD)
public Single<Response<Category>> create(
        VertxPrincipal principal,
        @VertxRequestBody CreateRequest request
) {
    return execute(principal, (SecurityUser user) -> 
        service.create(request, user)
    );
}

// Update
@VertxPut("/api/v1/categories/:id")
@RequirePermission(resource = "CATEGORY", action = Action.UPDATE)
public Single<Response<Category>> update(
        VertxPrincipal principal,
        @VertxPathVariable("id") UUID id,
        @VertxRequestBody UpdateRequest request
) {
    return execute(principal, (SecurityUser user) -> 
        service.update(id, request, user)
    );
}

// Delete
@VertxDelete("/api/v1/categories/:id")
@RequirePermission(resource = "CATEGORY", action = Action.DELETE)
public Single<Response<Boolean>> delete(
        VertxPrincipal principal,
        @VertxPathVariable("id") UUID id
) {
    return execute(principal, (SecurityUser user) -> 
        service.delete(id, user)
    );
}
```

### Public Endpoints (không cần @RequirePermission)

```java
@VertxPost("/api/v1/auth/login")
public Single<Response<AuthResponse>> login(@VertxRequestBody LoginRequest req) {
    return query(() -> {
        String token = authService.login(req.getIdentity(), req.getPassword());
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .build();
    });
}
```

## Key Rules

1. **READ operations**: Dùng `query()` hoặc `page()`, không cần `VertxPrincipal`
2. **WRITE operations**: Dùng `execute()`, cần `VertxPrincipal` để audit
3. **Public endpoints**: Không gắn `@RequirePermission`
4. **Protected endpoints**: Gắn `@RequirePermission(resource, action)` - auto-check bởi interceptor

## Go Equivalent (for social-service)

```go
// BaseHandler
type BaseHandler struct {
    // ...
}

// READ - Query single
func (h *BaseHandler) Query[T any](fn func() (T, error)) fiber.Handler {
    return func(c *fiber.Ctx) error {
        result, err := fn()
        if err != nil {
            return h.Error(c, err)
        }
        return h.OK(c, result)
    }
}

// READ - Page with pagination
func (h *BaseHandler) Page[T any](
    listFn func(p Pageable) ([]T, error),
    countFn func() (int64, error),
    p Pageable,
) fiber.Handler {
    return func(c *fiber.Ctx) error {
        items, err := listFn(p)
        if err != nil {
            return h.Error(c, err)
        }
        total, _ := countFn()
        return h.OK(c, Page[T]{Items: items, Total: total, Pageable: p})
    }
}

// WRITE - Execute with user
func (h *BaseHandler) Execute[T any](fn func(user SecurityUser) (T, error)) fiber.Handler {
    return func(c *fiber.Ctx) error {
        user := c.Locals("user").(SecurityUser)
        result, err := fn(user)
        if err != nil {
            return h.Error(c, err)
        }
        return h.OK(c, result)
    }
}
```
