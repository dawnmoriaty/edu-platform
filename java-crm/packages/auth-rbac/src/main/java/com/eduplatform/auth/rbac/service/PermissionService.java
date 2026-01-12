package com.eduplatform.auth.rbac.service;

import com.eduplatform.auth.rbac.model.SecurityUser;
import io.reactivex.rxjava3.core.Single;

import java.util.List;
import java.util.Map;

/**
 * PermissionService interface - Check permission với RxJava3
 */
public interface PermissionService {

    /**
     * Check permission - throw exception nếu không có quyền
     */
    Single<SecurityUser> checkPermission(Integer userId, String resource, String action);

    /**
     * Check permission - return boolean
     */
    Single<Boolean> hasPermission(Integer userId, String resource, String action);

    /**
     * Get all permissions của user
     * @return Map<ResourceCode, List<ActionCode>>
     */
    Single<Map<String, List<String>>> getUserPermissions(Integer userId);

    /**
     * Invalidate cache khi permission thay đổi
     */
    void invalidateUserPermissions(Integer userId);
}
