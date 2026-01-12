package com.eduplatform.identity.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.eduplatform.jooq.generated.Tables.*;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * PermissionRepository - jOOQ repository theo style TruyenRealm
 * Không dùng interface, viết trực tiếp với DSLContext
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PermissionRepository {

    private final DSLContext dsl;

    /**
     * Build condition cho search permissions
     */
    private Condition getWhereCondition(List<UUID> roleIds, String resource, String action) {
        Condition condition = DSL.trueCondition();

        if (roleIds != null && !roleIds.isEmpty()) {
            condition = condition.and(ROLE_PERMISSIONS.ROLE_ID.in(roleIds));
        }

        if (isNotEmpty(resource)) {
            condition = condition.and(PERMISSIONS.RESOURCE.eq(resource));
        }

        if (isNotEmpty(action)) {
            condition = condition.and(PERMISSIONS.ACTION.eq(action));
        }

        return condition;
    }

    // ==================== QUERY METHODS ====================

    /**
     * Đếm permissions matching role, resource, action
     */
    public long countPermissions(List<UUID> roleIds, String resource, String action) {
        if (roleIds == null || roleIds.isEmpty()) {
            return 0;
        }

        Condition condition = getWhereCondition(roleIds, resource, action);

        return dsl.selectCount()
                .from(ROLE_PERMISSIONS)
                .join(PERMISSIONS).on(ROLE_PERMISSIONS.PERMISSION_ID.eq(PERMISSIONS.ID))
                .where(condition)
                .fetchOne(0, Long.class);
    }

    /**
     * Check user có permission không (thông qua roles)
     */
    public boolean hasPermission(UUID userId, String resource, String action) {
        if (userId == null) return false;

        return dsl.fetchExists(
                dsl.selectOne()
                        .from(USER_ROLES)
                        .join(ROLE_PERMISSIONS).on(USER_ROLES.ROLE_ID.eq(ROLE_PERMISSIONS.ROLE_ID))
                        .join(PERMISSIONS).on(ROLE_PERMISSIONS.PERMISSION_ID.eq(PERMISSIONS.ID))
                        .where(USER_ROLES.USER_ID.eq(userId))
                        .and(PERMISSIONS.RESOURCE.eq(resource))
                        .and(PERMISSIONS.ACTION.eq(action))
        );
    }

    /**
     * Lấy permission matrix: resource -> List<action>
     */
    public Map<String, List<String>> getPermissionMatrix(List<UUID> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new HashMap<>();
        }

        var result = dsl.select(PERMISSIONS.RESOURCE, PERMISSIONS.ACTION)
                .from(ROLE_PERMISSIONS)
                .join(PERMISSIONS).on(ROLE_PERMISSIONS.PERMISSION_ID.eq(PERMISSIONS.ID))
                .where(ROLE_PERMISSIONS.ROLE_ID.in(roleIds))
                .fetch();

        Map<String, List<String>> matrix = new HashMap<>();
        for (var record : result) {
            String resource = record.get(PERMISSIONS.RESOURCE);
            String action = record.get(PERMISSIONS.ACTION);
            matrix.computeIfAbsent(resource, k -> new ArrayList<>()).add(action);
        }

        return matrix;
    }

    /**
     * Lấy permission matrix theo userId
     */
    public Map<String, List<String>> getPermissionMatrixByUserId(UUID userId) {
        if (userId == null) {
            return new HashMap<>();
        }

        var result = dsl.select(PERMISSIONS.RESOURCE, PERMISSIONS.ACTION)
                .from(USER_ROLES)
                .join(ROLE_PERMISSIONS).on(USER_ROLES.ROLE_ID.eq(ROLE_PERMISSIONS.ROLE_ID))
                .join(PERMISSIONS).on(ROLE_PERMISSIONS.PERMISSION_ID.eq(PERMISSIONS.ID))
                .where(USER_ROLES.USER_ID.eq(userId))
                .fetch();

        Map<String, List<String>> matrix = new HashMap<>();
        for (var record : result) {
            String resource = record.get(PERMISSIONS.RESOURCE);
            String action = record.get(PERMISSIONS.ACTION);
            matrix.computeIfAbsent(resource, k -> new ArrayList<>()).add(action);
        }

        return matrix;
    }

    /**
     * Lấy tất cả permissions của một role
     */
    public List<PermissionInfo> getPermissionsByRoleId(UUID roleId) {
        if (roleId == null) return List.of();

        return dsl.select(
                        PERMISSIONS.ID,
                        PERMISSIONS.NAME,
                        PERMISSIONS.RESOURCE,
                        PERMISSIONS.ACTION,
                        PERMISSIONS.DESCRIPTION
                )
                .from(ROLE_PERMISSIONS)
                .join(PERMISSIONS).on(ROLE_PERMISSIONS.PERMISSION_ID.eq(PERMISSIONS.ID))
                .where(ROLE_PERMISSIONS.ROLE_ID.eq(roleId))
                .fetch()
                .map(r -> new PermissionInfo(
                        r.get(PERMISSIONS.ID),
                        r.get(PERMISSIONS.NAME),
                        r.get(PERMISSIONS.RESOURCE),
                        r.get(PERMISSIONS.ACTION),
                        r.get(PERMISSIONS.DESCRIPTION)
                ));
    }

    /**
     * Lấy tất cả permissions
     */
    public List<PermissionInfo> findAllPermissions() {
        return dsl.select(
                        PERMISSIONS.ID,
                        PERMISSIONS.NAME,
                        PERMISSIONS.RESOURCE,
                        PERMISSIONS.ACTION,
                        PERMISSIONS.DESCRIPTION
                )
                .from(PERMISSIONS)
                .orderBy(PERMISSIONS.RESOURCE, PERMISSIONS.ACTION)
                .fetch()
                .map(r -> new PermissionInfo(
                        r.get(PERMISSIONS.ID),
                        r.get(PERMISSIONS.NAME),
                        r.get(PERMISSIONS.RESOURCE),
                        r.get(PERMISSIONS.ACTION),
                        r.get(PERMISSIONS.DESCRIPTION)
                ));
    }

    // ==================== MUTATION METHODS ====================

    /**
     * Gán permission cho role
     */
    public void assignPermission(UUID roleId, UUID permissionId) {
        if (roleId == null || permissionId == null) return;

        dsl.insertInto(ROLE_PERMISSIONS)
                .set(ROLE_PERMISSIONS.ROLE_ID, roleId)
                .set(ROLE_PERMISSIONS.PERMISSION_ID, permissionId)
                .onDuplicateKeyIgnore()
                .execute();
    }

    /**
     * Gỡ permission khỏi role
     */
    public boolean removePermission(UUID roleId, UUID permissionId) {
        if (roleId == null || permissionId == null) return false;

        return dsl.deleteFrom(ROLE_PERMISSIONS)
                .where(ROLE_PERMISSIONS.ROLE_ID.eq(roleId))
                .and(ROLE_PERMISSIONS.PERMISSION_ID.eq(permissionId))
                .execute() > 0;
    }

    /**
     * Thay thế tất cả permissions của role
     */
    public void replacePermissions(UUID roleId, List<UUID> permissionIds) {
        if (roleId == null) return;

        // Xóa permissions cũ
        dsl.deleteFrom(ROLE_PERMISSIONS)
                .where(ROLE_PERMISSIONS.ROLE_ID.eq(roleId))
                .execute();

        // Thêm permissions mới
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (UUID permId : permissionIds) {
                dsl.insertInto(ROLE_PERMISSIONS)
                        .set(ROLE_PERMISSIONS.ROLE_ID, roleId)
                        .set(ROLE_PERMISSIONS.PERMISSION_ID, permId)
                        .execute();
            }
        }
    }

    // ==================== INNER CLASS ====================

    /**
     * Simple DTO cho permission info
     */
    public record PermissionInfo(
            UUID id,
            String name,
            String resource,
            String action,
            String description
    ) {}
}
