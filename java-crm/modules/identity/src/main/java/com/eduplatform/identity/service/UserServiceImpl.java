package com.eduplatform.identity.service;

import com.eduplatform.auth.rbac.model.SecurityUser;
import com.eduplatform.common.constant.ErrorCode;
import com.eduplatform.common.exception.AppException;
import com.eduplatform.entity.enums.UserStatus;
import com.eduplatform.identity.entity.User;
import com.eduplatform.identity.repository.UserRepository;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * UserServiceImpl - Synchronous style, không dùng RxJava
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<User> getUsers(String query, String status, int page, int size) {
        int offset = page * size;
        return userRepository.findUsersByCriteria(null, query, status, offset, size);
    }

    @Override
    public long countUsers(String query, String status) {
        return userRepository.countUsersByCriteria(null, query, status);
    }

    @Override
    public User getUserById(UUID id) {
        User user = userRepository.getUserById(id);
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    public User createUser(JsonObject data, SecurityUser currentUser) {
        String username = data.getString("username");
        String email = data.getString("email");
        String password = data.getString("password");

        // Validate required fields
        if (username == null || username.isBlank()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Username không được để trống");
        }
        if (email == null || email.isBlank()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Email không được để trống");
        }
        if (password == null || password.isBlank()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Password không được để trống");
        }

        // Check duplicates
        if (userRepository.existsByUsername(username, null)) {
            throw new AppException(ErrorCode.DUPLICATE_ENTRY, "Username đã tồn tại");
        }
        if (userRepository.existsByEmail(email, null)) {
            throw new AppException(ErrorCode.DUPLICATE_ENTRY, "Email đã tồn tại");
        }

        // Create user
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(BCrypt.hashpw(password, BCrypt.gensalt(12)))
                .firstName(data.getString("firstName"))
                .lastName(data.getString("lastName"))
                .phone(data.getString("phone"))
                .status(UserStatus.ACTIVE)
                .build();
        
        if (currentUser != null) {
            user.setCreatedBy(currentUser.getId());
            user.setUpdatedBy(currentUser.getId());
        }

        User created = userRepository.insertUser(user);

        // Assign roles if provided
        var roleIds = data.getJsonArray("roleIds");
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Object roleId : roleIds) {
                userRepository.assignRole(created.getId(), UUID.fromString(roleId.toString()));
            }
        }

        return created;
    }

    @Override
    public User updateUser(UUID id, JsonObject data, SecurityUser currentUser) {
        User existing = userRepository.getUserById(id);
        if (existing == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        // Check username duplicate
        String newUsername = data.getString("username");
        if (newUsername != null && !newUsername.equals(existing.getUsername())) {
            if (userRepository.existsByUsername(newUsername, id)) {
                throw new AppException(ErrorCode.DUPLICATE_ENTRY, "Username đã tồn tại");
            }
            existing.setUsername(newUsername);
        }

        // Check email duplicate
        String newEmail = data.getString("email");
        if (newEmail != null && !newEmail.equals(existing.getEmail())) {
            if (userRepository.existsByEmail(newEmail, id)) {
                throw new AppException(ErrorCode.DUPLICATE_ENTRY, "Email đã tồn tại");
            }
            existing.setEmail(newEmail);
        }

        // Update other fields
        if (data.containsKey("firstName")) {
            existing.setFirstName(data.getString("firstName"));
        }
        if (data.containsKey("lastName")) {
            existing.setLastName(data.getString("lastName"));
        }
        if (data.containsKey("phone")) {
            existing.setPhone(data.getString("phone"));
        }
        if (data.containsKey("status")) {
            existing.setStatus(UserStatus.valueOf(data.getString("status")));
        }

        // Update password if provided
        String newPassword = data.getString("password");
        if (newPassword != null && !newPassword.isBlank()) {
            existing.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt(12)));
        }

        if (currentUser != null) {
            existing.setUpdatedBy(currentUser.getId());
        }

        User updated = userRepository.updateUser(existing);

        // Update roles if provided
        var roleIds = data.getJsonArray("roleIds");
        if (roleIds != null) {
            List<UUID> roleIdList = roleIds.stream()
                    .map(r -> UUID.fromString(r.toString()))
                    .toList();
            userRepository.replaceRoles(id, roleIdList);
        }

        return updated;
    }

    @Override
    public boolean deleteUser(UUID id, SecurityUser currentUser) {
        User existing = userRepository.getUserById(id);
        if (existing == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        // Prevent self-deletion
        if (currentUser != null && currentUser.getId().equals(id)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Không thể xóa chính mình");
        }

        return userRepository.deleteUser(id);
    }
}
