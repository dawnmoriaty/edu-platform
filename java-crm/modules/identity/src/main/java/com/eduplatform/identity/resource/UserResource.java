package com.eduplatform.identity.resource;

import com.eduplatform.auth.rbac.model.SecurityUser;
import com.eduplatform.common.response.ApiResponse;
import com.eduplatform.common.vertx.annotation.*;
import com.eduplatform.common.vertx.model.Page;
import com.eduplatform.common.vertx.model.Pageable;
import com.eduplatform.common.vertx.model.VertxPrincipal;
import com.eduplatform.common.vertx.resource.BaseResource;
import com.eduplatform.identity.entity.User;
import com.eduplatform.identity.service.UserService;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

/**
 * UserResource - User management endpoints
 * 
 * Sử dụng @VertxRestController pattern
 */
@Slf4j
@VertxRestController
@RequiredArgsConstructor
public class UserResource extends BaseResource {

    private static final String URI = "/user/";
    
    private final UserService userService;

    /**
     * GET /api/v1/users/me - Lấy thông tin user hiện tại
     */
    @VertxGet("/api/v1/users/me")
    public Single<ResponseEntity<ApiResponse<Object>>> getCurrentUser(
            VertxPrincipal principal
    ) {
        if (principal == null || principal.getUserId() == null) {
            return getCustomError("Unauthorized");
        }
        
        Object user = principal.getSecurityUser();
        if (user != null) {
            return Single.just(okEntity(user));
        }
        
        return getCustomError("User not found");
    }

    /**
     * GET /api/v1/users - Danh sách users (cần quyền VIEW)
     */
    @VertxGet("/api/v1/users")
    public Single<ResponseEntity<ApiResponse<Page<User>>>> getUsers(
            VertxPrincipal principal,
            Pageable pageable,
            @VertxRequestParam(value = "query", required = false) String query,
            @VertxRequestParam(value = "status", required = false) String status
    ) {
        return checkPermission(principal, URI, "VIEW")
                .flatMap(user -> Single.fromCallable(() -> {
                    List<User> users = userService.getUsers(query, status, pageable.getPage(), pageable.getSize());
                    long total = userService.countUsers(query, status);
                    
                    pageable.setTotal(total);
                    return new Page<>(pageable, users);
                }).subscribeOn(Schedulers.io()))
                .map(this::okEntity);
    }

    /**
     * GET /api/v1/users/:id - Chi tiết user (cần quyền VIEW)
     */
    @VertxGet("/api/v1/users/:id")
    public Single<ResponseEntity<ApiResponse<User>>> getUserById(
            VertxPrincipal principal,
            @VertxPathVariable("id") UUID userId
    ) {
        return checkPermission(principal, URI, "VIEW")
                .flatMap(user -> Single.fromCallable(() -> userService.getUserById(userId))
                        .subscribeOn(Schedulers.io()))
                .map(this::okEntity);
    }

    /**
     * POST /api/v1/users - Tạo user mới (cần quyền ADD)
     */
    @VertxPost("/api/v1/users")
    public Single<ResponseEntity<ApiResponse<User>>> createUser(
            VertxPrincipal principal,
            @VertxRequestBody CreateUserRequest request
    ) {
        return checkPermission(principal, URI, "ADD")
                .flatMap(currentUser -> Single.fromCallable(() -> {
                    JsonObject body = JsonObject.mapFrom(request);
                    return userService.createUser(body, (SecurityUser) currentUser);
                }).subscribeOn(Schedulers.io()))
                .map(this::okEntity);
    }

    /**
     * PUT /api/v1/users/:id - Cập nhật user (cần quyền UPDATE)
     */
    @VertxPut("/api/v1/users/:id")
    public Single<ResponseEntity<ApiResponse<User>>> updateUser(
            VertxPrincipal principal,
            @VertxPathVariable("id") UUID userId,
            @VertxRequestBody UpdateUserRequest request
    ) {
        return checkPermission(principal, URI, "UPDATE")
                .flatMap(currentUser -> Single.fromCallable(() -> {
                    JsonObject body = JsonObject.mapFrom(request);
                    return userService.updateUser(userId, body, (SecurityUser) currentUser);
                }).subscribeOn(Schedulers.io()))
                .map(this::okEntity);
    }

    /**
     * DELETE /api/v1/users/:id - Xóa user (cần quyền DELETE)
     */
    @VertxDelete("/api/v1/users/:id")
    public Single<ResponseEntity<ApiResponse<Boolean>>> deleteUser(
            VertxPrincipal principal,
            @VertxPathVariable("id") UUID userId
    ) {
        return checkPermission(principal, URI, "DELETE")
                .flatMap(currentUser -> Single.fromCallable(() -> 
                        userService.deleteUser(userId, (SecurityUser) currentUser))
                        .subscribeOn(Schedulers.io()))
                .map(this::okEntity);
    }

    /**
     * PUT /api/v1/users/:id/status - Thay đổi trạng thái user
     */
    @VertxPut("/api/v1/users/:id/status")
    public Single<ResponseEntity<ApiResponse<User>>> updateUserStatus(
            VertxPrincipal principal,
            @VertxPathVariable("id") UUID userId,
            @VertxRequestBody StatusUpdateRequest request
    ) {
        return checkPermission(principal, URI, "UPDATE")
                .flatMap(currentUser -> Single.fromCallable(() -> {
                    JsonObject body = new JsonObject().put("status", request.getStatus());
                    return userService.updateUser(userId, body, (SecurityUser) currentUser);
                }).subscribeOn(Schedulers.io()))
                .map(this::okEntity);
    }

    // ============================================
    // Request DTOs
    // ============================================

    @lombok.Data
    public static class CreateUserRequest {
        private String username;
        private String email;
        private String password;
        private String name;
        private String status;
        private java.util.List<UUID> roleIds;
    }

    @lombok.Data
    public static class UpdateUserRequest {
        private String email;
        private String name;
        private String status;
        private java.util.List<UUID> roleIds;
    }

    @lombok.Data
    public static class StatusUpdateRequest {
        private String status;
    }
}
