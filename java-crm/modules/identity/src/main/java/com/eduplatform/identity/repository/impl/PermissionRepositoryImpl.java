package com.eduplatform.identity.repository.impl;

import com.eduplatform.common.domain.DataScope;
import com.eduplatform.identity.entity.RbacPermission;
import com.eduplatform.identity.repository.PermissionRepository;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PermissionRepositoryImpl - Mock implementation cho development
 * TODO: Thay bằng jOOQ implementation khi có database schema
 */
@Slf4j
@Repository
public class PermissionRepositoryImpl implements PermissionRepository {

    // Mock data store (production: jOOQ/PostgreSQL)
    private final Map<String, RbacPermission> permissionStore = new ConcurrentHashMap<>();

    public PermissionRepositoryImpl() {
        // Initialize với mock data cho development
        initMockData();
    }

    private void initMockData() {
        // Role 1 = SUPER_ADMIN có tất cả quyền
        addPermission(1, 1, "user", "User Management", List.of("VIEW", "ADD", "UPDATE", "DELETE"));
        addPermission(1, 2, "role", "Role Management", List.of("VIEW", "ADD", "UPDATE", "DELETE"));
        addPermission(1, 3, "permission", "Permission Management", List.of("VIEW", "ADD", "UPDATE", "DELETE"));
        addPermission(1, 4, "student", "Student Management", List.of("VIEW", "ADD", "UPDATE", "DELETE"));
        addPermission(1, 5, "course", "Course Management", List.of("VIEW", "ADD", "UPDATE", "DELETE"));
        addPermission(1, 6, "finance", "Finance Management", List.of("VIEW", "ADD", "UPDATE", "DELETE"));

        // Role 2 = ADMIN có hầu hết quyền
        addPermission(2, 1, "user", "User Management", List.of("VIEW", "ADD", "UPDATE"));
        addPermission(2, 4, "student", "Student Management", List.of("VIEW", "ADD", "UPDATE", "DELETE"));
        addPermission(2, 5, "course", "Course Management", List.of("VIEW", "ADD", "UPDATE", "DELETE"));

        // Role 3 = TEACHER
        addPermission(3, 4, "student", "Student Management", List.of("VIEW"));
        addPermission(3, 5, "course", "Course Management", List.of("VIEW", "UPDATE"));

        // Role 4 = STUDENT (chỉ view)
        addPermission(4, 5, "course", "Course Management", List.of("VIEW"));

        log.info("Mock permission data initialized with {} entries", permissionStore.size());
    }

    private void addPermission(int roleId, int resourceId, String resourceCode, String resourceName, List<String> actions) {
        String key = roleId + "_" + resourceId;
        RbacPermission permission = RbacPermission.builder()
                .roleId(roleId)
                .resourceId(resourceId)
                .resourceCode(resourceCode)
                .resourceName(resourceName)
                .actions(actions)
                .dataScope(DataScope.ALL)
                .build();
        permission.setId(permissionStore.size() + 1);
        permissionStore.put(key, permission);
    }

    @Override
    public Single<Long> countPermissions(List<Integer> roleIds, String resourceCode, String action) {
        return Single.fromCallable(() -> {
            long count = permissionStore.values().stream()
                    .filter(p -> roleIds.contains(p.getRoleId()))
                    .filter(p -> p.getResourceCode().equals(resourceCode))
                    .filter(p -> p.getActions().contains(action) || p.getActions().contains("*"))
                    .count();
            log.debug("countPermissions: roleIds={}, resource={}, action={}, count={}", 
                    roleIds, resourceCode, action, count);
            return count;
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<List<RbacPermission>> getPermissionsByRoles(List<Integer> roleIds) {
        return Single.fromCallable(() -> {
            List<RbacPermission> result = permissionStore.values().stream()
                    .filter(p -> roleIds.contains(p.getRoleId()))
                    .toList();
            log.debug("getPermissionsByRoles: roleIds={}, found={}", roleIds, result.size());
            return result;
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<List<RbacPermission>> getPermissionsByRoleId(Integer roleId) {
        return Single.fromCallable(() -> 
            permissionStore.values().stream()
                    .filter(p -> p.getRoleId().equals(roleId))
                    .toList()
        ).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<RbacPermission> assignPermission(Integer roleId, Integer resourceId, List<String> actions) {
        return Single.fromCallable(() -> {
            String key = roleId + "_" + resourceId;
            RbacPermission existing = permissionStore.get(key);
            
            if (existing != null) {
                // Update actions
                List<String> merged = new ArrayList<>(existing.getActions());
                actions.forEach(a -> {
                    if (!merged.contains(a)) merged.add(a);
                });
                existing.setActions(merged);
                return existing;
            }
            
            // Create new
            RbacPermission newPerm = RbacPermission.builder()
                    .roleId(roleId)
                    .resourceId(resourceId)
                    .actions(actions)
                    .dataScope(DataScope.ALL)
                    .build();
            newPerm.setId(permissionStore.size() + 1);
            permissionStore.put(key, newPerm);
            return newPerm;
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Boolean> removePermission(Integer roleId, Integer resourceId) {
        return Single.fromCallable(() -> {
            String key = roleId + "_" + resourceId;
            return permissionStore.remove(key) != null;
        }).subscribeOn(Schedulers.io());
    }
}
