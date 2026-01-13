package com.eduplatform.identity.resource;

import com.eduplatform.auth.rbac.model.SecurityUser;
import com.eduplatform.common.constant.Action;
import com.eduplatform.common.response.ApiResponse;
import com.eduplatform.common.vertx.annotation.*;
import com.eduplatform.common.vertx.model.Page;
import com.eduplatform.common.vertx.model.Pageable;
import com.eduplatform.common.vertx.model.VertxPrincipal;
import com.eduplatform.common.vertx.resource.BaseResource;
import com.eduplatform.identity.dto.request.CreateUserRequest;
import com.eduplatform.identity.dto.request.StatusUpdateRequest;
import com.eduplatform.identity.dto.request.UpdateUserRequest;
import com.eduplatform.identity.entity.User;
import com.eduplatform.identity.service.UserService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

/**
 * UserResource - User management endpoints
 * 
 * Pattern:
 * - READ: dùng query() hoặc page() - không cần principal
 * - WRITE: dùng execute() - cần principal để audit
 */
@Slf4j
@VertxRestController
@RequiredArgsConstructor
public class UserResource extends BaseResource {

    private static final String RESOURCE = "USER";
    
    private final UserService userService;

    // ========== READ Operations ==========

    /**
     * GET /api/v1/users/me - User hiện tại
     */
    @VertxGet("/api/v1/users/me")
    public Single<ResponseEntity<ApiResponse<Object>>> getCurrentUser(VertxPrincipal principal) {
        return getUser(principal).map(this::ok);
    }

    /**
     * GET /api/v1/users - Danh sách users
     */
    @VertxGet("/api/v1/users")
    @RequirePermission(resource = RESOURCE, action = Action.VIEW)
    public Single<ResponseEntity<ApiResponse<Page<User>>>> getUsers(
            Pageable pageable,
            @VertxRequestParam(value = "query", required = false) String query,
            @VertxRequestParam(value = "status", required = false) String status
    ) {
        return page(
                () -> userService.getUsers(query, status, pageable.getPage(), pageable.getSize()),
                () -> userService.countUsers(query, status),
                pageable
        );
    }

    /**
     * GET /api/v1/users/:id - Chi tiết user
     */
    @VertxGet("/api/v1/users/:id")
    @RequirePermission(resource = RESOURCE, action = Action.VIEW)
    public Single<ResponseEntity<ApiResponse<User>>> getUserById(@VertxPathVariable("id") UUID userId) {
        return query(() -> userService.getUserById(userId));
    }

    // ========== WRITE Operations ==========

    /**
     * POST /api/v1/users - Tạo user
     */
    @VertxPost("/api/v1/users")
    @RequirePermission(resource = RESOURCE, action = Action.ADD)
    public Single<ResponseEntity<ApiResponse<User>>> createUser(
            VertxPrincipal principal,
            @VertxRequestBody CreateUserRequest request
    ) {
        return execute(principal, (SecurityUser user) -> {
            JsonObject body = JsonObject.mapFrom(request);
            return userService.createUser(body, user);
        });
    }

    /**
     * PUT /api/v1/users/:id - Cập nhật user
     */
    @VertxPut("/api/v1/users/:id")
    @RequirePermission(resource = RESOURCE, action = Action.UPDATE)
    public Single<ResponseEntity<ApiResponse<User>>> updateUser(
            VertxPrincipal principal,
            @VertxPathVariable("id") UUID userId,
            @VertxRequestBody UpdateUserRequest request
    ) {
        return execute(principal, (SecurityUser user) -> {
            JsonObject body = JsonObject.mapFrom(request);
            return userService.updateUser(userId, body, user);
        });
    }

    /**
     * DELETE /api/v1/users/:id - Xóa user
     */
    @VertxDelete("/api/v1/users/:id")
    @RequirePermission(resource = RESOURCE, action = Action.DELETE)
    public Single<ResponseEntity<ApiResponse<Boolean>>> deleteUser(
            VertxPrincipal principal,
            @VertxPathVariable("id") UUID userId
    ) {
        return execute(principal, (SecurityUser user) -> userService.deleteUser(userId, user));
    }

    /**
     * PUT /api/v1/users/:id/status - Đổi trạng thái
     */
    @VertxPut("/api/v1/users/:id/status")
    @RequirePermission(resource = RESOURCE, action = Action.UPDATE)
    public Single<ResponseEntity<ApiResponse<User>>> updateUserStatus(
            VertxPrincipal principal,
            @VertxPathVariable("id") UUID userId,
            @VertxRequestBody StatusUpdateRequest request
    ) {
        return execute(principal, (SecurityUser user) -> {
            JsonObject body = new JsonObject().put("status", request.getStatus());
            return userService.updateUser(userId, body, user);
        });
    }
}

