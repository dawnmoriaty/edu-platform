package com.eduplatform.identity.service;

import com.eduplatform.auth.rbac.model.SecurityUser;
import com.eduplatform.identity.entity.User;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.UUID;

/**
 * UserService - Service cho User operations
 * Synchronous style, không dùng RxJava
 */
public interface UserService {

    /**
     * Lấy danh sách users với pagination
     */
    List<User> getUsers(String query, String status, int page, int size);

    /**
     * Đếm tổng users
     */
    long countUsers(String query, String status);

    /**
     * Lấy user theo ID
     */
    User getUserById(UUID id);

    /**
     * Tạo user mới
     */
    User createUser(JsonObject data, SecurityUser currentUser);

    /**
     * Cập nhật user
     */
    User updateUser(UUID id, JsonObject data, SecurityUser currentUser);

    /**
     * Xóa user
     */
    boolean deleteUser(UUID id, SecurityUser currentUser);
}
