package com.eduplatform.auth.rbac.service;

import com.eduplatform.auth.rbac.model.SecurityUser;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * PermissionService interface - Check permission
 * Synchronous style, không dùng RxJava
 */
public interface PermissionService {

    /**
     * Check permission - throw exception nếu không có quyền
     */
    SecurityUser checkPermission(UUID userId, String resource, String action);

    /**
     * Check permission - return boolean
     */
    boolean hasPermission(UUID userId, String resource, String action);

    /**
     * Get all permissions của user
     * @return Map<ResourceCode, List<ActionCode>>
     */
    Map<String, List<String>> getUserPermissions(UUID userId);

    /**
     * Invalidate cache khi permission thay đổi
     */
    void invalidateUserPermissions(UUID userId);
}
