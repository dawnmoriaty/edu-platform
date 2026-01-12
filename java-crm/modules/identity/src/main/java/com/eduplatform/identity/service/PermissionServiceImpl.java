package com.eduplatform.identity.service;

import com.eduplatform.auth.rbac.model.SecurityUser;
import com.eduplatform.auth.rbac.service.PermissionService;
import com.eduplatform.common.constant.ErrorCode;
import com.eduplatform.common.exception.AppException;
import com.eduplatform.identity.entity.User;
import com.eduplatform.identity.repository.PermissionRepository;
import com.eduplatform.identity.repository.RoleRepository;
import com.eduplatform.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PermissionServiceImpl - Synchronous style với caching
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    // Simple in-memory cache (production: use Redis)
    private final Map<String, Object> cache = new ConcurrentHashMap<>();

    @Override
    public SecurityUser checkPermission(UUID userId, String resource, String action) {
        String cacheKey = buildCacheKey(userId, resource, action);
        
        // Check cache first
        Boolean cached = (Boolean) cache.get(cacheKey);
        if (Boolean.TRUE.equals(cached)) {
            return buildSecurityUser(userId);
        }

        boolean hasPermission = hasPermission(userId, resource, action);
        
        if (!hasPermission) {
            throw new AppException(ErrorCode.FORBIDDEN,
                    String.format("Không có quyền %s trên %s", action, resource));
        }
        
        // Cache result
        cache.put(cacheKey, true);
        
        return buildSecurityUser(userId);
    }

    @Override
    public boolean hasPermission(UUID userId, String resource, String action) {
        return permissionRepository.hasPermission(userId, resource, action);
    }

    @Override
    public Map<String, List<String>> getUserPermissions(UUID userId) {
        String cacheKey = "user:perms:" + userId;
        
        @SuppressWarnings("unchecked")
        Map<String, List<String>> cached = (Map<String, List<String>>) cache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Map<String, List<String>> result = permissionRepository.getPermissionMatrixByUserId(userId);
        
        // Cache
        cache.put(cacheKey, result);
        return result;
    }

    @Override
    public void invalidateUserPermissions(UUID userId) {
        cache.keySet().removeIf(key -> key.startsWith("perm:" + userId + ":"));
        cache.remove("user:perms:" + userId);
    }

    private SecurityUser buildSecurityUser(UUID userId) {
        User user = userRepository.getUserById(userId);
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        
        List<UUID> roleIds = userRepository.getRoleIds(userId);
        List<String> roleNames = roleRepository.getRoleNamesByUserId(userId);
        Map<String, List<String>> permissions = getUserPermissions(userId);
        
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

    private String buildCacheKey(UUID userId, String resource, String action) {
        return String.format("perm:%s:%s:%s", userId, resource, action);
    }
}
