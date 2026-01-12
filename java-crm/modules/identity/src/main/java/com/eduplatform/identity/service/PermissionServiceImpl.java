package com.eduplatform.identity.service;

import com.eduplatform.auth.rbac.model.SecurityUser;
import com.eduplatform.auth.rbac.service.PermissionService;
import com.eduplatform.common.constant.ErrorCode;
import com.eduplatform.common.exception.AppException;
import com.eduplatform.identity.repository.PermissionRepository;
import com.eduplatform.identity.repository.UserRepository;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * PermissionServiceImpl - Implementation với RxJava3 và caching
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    // Simple in-memory cache (production: use Redis)
    private final Map<String, Object> cache = new ConcurrentHashMap<>();

    @Override
    public Single<SecurityUser> checkPermission(Integer userId, String resource, String action) {
        String cacheKey = buildCacheKey(userId, resource, action);
        
        // Check cache first
        Boolean cached = (Boolean) cache.get(cacheKey);
        if (Boolean.TRUE.equals(cached)) {
            return buildSecurityUser(userId);
        }

        return hasPermission(userId, resource, action)
                .flatMap(hasPermission -> {
                    if (!hasPermission) {
                        return Single.error(new AppException(ErrorCode.FORBIDDEN,
                                String.format("Không có quyền %s trên %s", action, resource)));
                    }
                    
                    // Cache result
                    cache.put(cacheKey, true);
                    
                    return buildSecurityUser(userId);
                });
    }

    @Override
    public Single<Boolean> hasPermission(Integer userId, String resource, String action) {
        return userRepository.getRoleIds(userId)
                .flatMap(roleIds -> {
                    if (roleIds.isEmpty()) {
                        return Single.just(false);
                    }
                    return permissionRepository.countPermissions(roleIds, resource, action)
                            .map(count -> count > 0);
                });
    }

    @Override
    public Single<Map<String, List<String>>> getUserPermissions(Integer userId) {
        String cacheKey = "user:perms:" + userId;
        
        @SuppressWarnings("unchecked")
        Map<String, List<String>> cached = (Map<String, List<String>>) cache.get(cacheKey);
        if (cached != null) {
            return Single.just(cached);
        }

        return userRepository.getRoleIds(userId)
                .flatMap(permissionRepository::getPermissionsByRoles)
                .map(permissions -> {
                    Map<String, List<String>> result = permissions.stream()
                            .collect(Collectors.groupingBy(
                                    p -> p.getResourceCode(),
                                    Collectors.flatMapping(
                                            p -> p.getActions().stream(),
                                            Collectors.toList()
                                    )
                            ));
                    
                    // Cache
                    cache.put(cacheKey, result);
                    
                    return result;
                });
    }

    @Override
    public void invalidateUserPermissions(Integer userId) {
        cache.keySet().removeIf(key -> key.startsWith("perm:" + userId + ":"));
        cache.remove("user:perms:" + userId);
    }

    private Single<SecurityUser> buildSecurityUser(Integer userId) {
        return userRepository.findById(userId)
                .zipWith(getUserPermissions(userId), (user, permissions) -> 
                        SecurityUser.builder()
                                .id(user.getId())
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .name(user.getName())
                                .avatar(user.getAvatar())
                                .roleIds(user.getRoleIds())
                                .roleCodes(user.getRoleCodes())
                                .permissions(permissions)
                                .build()
                );
    }

    private String buildCacheKey(Integer userId, String resource, String action) {
        return String.format("perm:%d:%s:%s", userId, resource, action);
    }
}
