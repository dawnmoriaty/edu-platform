package com.eduplatform.identity.service;

import com.eduplatform.auth.rbac.model.SecurityUser;

import java.util.UUID;

/**
 * AuthService - Service xác thực người dùng
 * Synchronous style, không dùng RxJava
 */
public interface AuthService {

    /**
     * Login với username/email và password
     * @return Token nếu thành công
     */
    String login(String identity, String password);

    /**
     * Register người dùng mới
     * @return SecurityUser đã tạo
     */
    SecurityUser register(String username, String email, String password, String name);

    /**
     * Lấy thông tin user từ token
     */
    SecurityUser getCurrentUser(String token);

    /**
     * Làm mới token
     */
    String refreshToken(String refreshToken);

    /**
     * Logout - invalidate token
     */
    boolean logout(String token);

    /**
     * Đổi mật khẩu
     */
    boolean changePassword(UUID userId, String oldPassword, String newPassword);
}
