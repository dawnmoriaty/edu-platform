package com.eduplatform.identity.repository;

import com.eduplatform.identity.entity.RbacPermission;
import io.reactivex.rxjava3.core.Single;

import java.util.List;

/**
 * PermissionRepository - Repository cho RBAC Permission với RxJava3
 */
public interface PermissionRepository {

    /**
     * Count permissions matching role, resource, action
     */
    Single<Long> countPermissions(List<Integer> roleIds, String resourceCode, String action);

    /**
     * Get all permissions by role IDs
     */
    Single<List<RbacPermission>> getPermissionsByRoles(List<Integer> roleIds);

    /**
     * Get permissions for a specific role
     */
    Single<List<RbacPermission>> getPermissionsByRoleId(Integer roleId);

    /**
     * Assign permission to role
     */
    Single<RbacPermission> assignPermission(Integer roleId, Integer resourceId, List<String> actions);

    /**
     * Remove permission from role
     */
    Single<Boolean> removePermission(Integer roleId, Integer resourceId);
}
