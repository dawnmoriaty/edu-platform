package com.eduplatform.identity.service;

import com.eduplatform.auth.rbac.model.SecurityUser;
import com.eduplatform.auth.rbac.service.TokenService;
import com.eduplatform.common.constant.ErrorCode;
import com.eduplatform.common.exception.AppException;
import com.eduplatform.entity.enums.UserStatus;
import com.eduplatform.identity.entity.User;
import com.eduplatform.identity.repository.PermissionRepository;
import com.eduplatform.identity.repository.RoleRepository;
import com.eduplatform.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * AuthServiceImpl - Synchronous style, không dùng RxJava
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final TokenService tokenService;

    @Override
    public String login(String identity, String password) {
        User user = userRepository.getUserByUsernameOrEmail(identity);
        
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Mật khẩu không đúng");
        }
        
        if (!user.isActive()) {
            throw new AppException(ErrorCode.FORBIDDEN, "Tài khoản đã bị khóa");
        }
        
        SecurityUser securityUser = buildSecurityUser(user);
        return tokenService.generate(securityUser).blockingGet();
    }

    @Override
    public SecurityUser register(String username, String email, String password, String name) {
        // Check username exists
        if (userRepository.existsByUsername(username, null)) {
            throw new AppException(ErrorCode.DUPLICATE_ENTRY, "Username đã tồn tại");
        }
        
        // Check email exists
        if (userRepository.existsByEmail(email, null)) {
            throw new AppException(ErrorCode.DUPLICATE_ENTRY, "Email đã tồn tại");
        }
        
        // Create user
        User newUser = User.builder()
                .username(username)
                .email(email)
                .passwordHash(BCrypt.hashpw(password, BCrypt.gensalt(12)))
                .firstName(name)
                .status(UserStatus.ACTIVE)
                .build();
        
        User created = userRepository.insertUser(newUser);
        return buildSecurityUser(created);
    }

    @Override
    public SecurityUser getCurrentUser(String token) {
        SecurityUser securityUser = tokenService.getSecurityUser(token).blockingGet();
        
        // Refresh permissions
        Map<String, List<String>> permissions = permissionRepository.getPermissionMatrixByUserId(securityUser.getId());
        securityUser.setPermissions(permissions);
        
        return securityUser;
    }

    @Override
    public String refreshToken(String refreshToken) {
        Boolean isExpired = tokenService.isExpired(refreshToken).blockingGet();
        
        if (isExpired) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }
        
        SecurityUser securityUser = tokenService.getSecurityUser(refreshToken).blockingGet();
        return tokenService.generate(securityUser).blockingGet();
    }

    @Override
    public boolean logout(String token) {
        return tokenService.invalidate(token).blockingGet();
    }

    @Override
    public boolean changePassword(UUID userId, String oldPassword, String newPassword) {
        User user = userRepository.getUserById(userId);
        
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        
        if (!BCrypt.checkpw(oldPassword, user.getPasswordHash())) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Mật khẩu cũ không đúng");
        }
        
        String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        return userRepository.updatePassword(userId, newHash);
    }

    /**
     * Build SecurityUser từ User entity
     */
    private SecurityUser buildSecurityUser(User user) {
        List<UUID> roleIds = userRepository.getRoleIds(user.getId());
        List<String> roleNames = roleRepository.getRoleNamesByUserId(user.getId());
        Map<String, List<String>> permissions = permissionRepository.getPermissionMatrixByUserId(user.getId());
        
        return SecurityUser.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getFullName())
                .avatar(user.getAvatar())
                .roleIds(roleIds)
                .roleCodes(roleNames)
                .permissions(permissions)
                .build();
    }
}
