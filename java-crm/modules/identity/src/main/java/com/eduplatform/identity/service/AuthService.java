package com.eduplatform.identity.service;

import com.eduplatform.auth.rbac.model.SecurityUser;
import io.reactivex.rxjava3.core.Single;

/**
 * AuthService - Service xác thực người dùng với RxJava3
 */
public interface AuthService {

    /**
     * Login với username/email và password
     * @return Token nếu thành công
     */
    Single<String> login(String identity, String password);

    /**
     * Register người dùng mới
     * @return SecurityUser đã tạo
     */
    Single<SecurityUser> register(String username, String email, String password, String name);

    /**
     * Lấy thông tin user từ token
     */
    Single<SecurityUser> getCurrentUser(String token);

    /**
     * Làm mới token
     */
    Single<String> refreshToken(String refreshToken);

    /**
     * Logout - invalidate token
     */
    Single<Boolean> logout(String token);

    /**
     * Đổi mật khẩu
     */
    Single<Boolean> changePassword(Integer userId, String oldPassword, String newPassword);
}
