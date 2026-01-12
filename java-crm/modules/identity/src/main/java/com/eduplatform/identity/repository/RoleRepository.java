package com.eduplatform.identity.repository;

import com.eduplatform.identity.entity.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.eduplatform.jooq.generated.Tables.*;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * RoleRepository - jOOQ repository theo style TruyenRealm
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RoleRepository {

    private final DSLContext dsl;

    /**
     * Danh sách fields của Role
     */
    private static List<Field<?>> getRoleFields() {
        return asList(
                ROLES.ID,
                ROLES.NAME,
                ROLES.DESCRIPTION,
                ROLES.CREATED_AT,
                ROLES.UPDATED_AT
        );
    }

    /**
     * Build condition cho search/filter
     */
    private Condition getWhereCondition(String query) {
        Condition condition = DSL.trueCondition();

        if (isNotEmpty(query)) {
            String searchTerm = "%" + query.trim().toLowerCase() + "%";
            condition = condition.and(
                    DSL.lower(ROLES.NAME).like(searchTerm)
                            .or(DSL.lower(ROLES.DESCRIPTION).like(searchTerm))
            );
        }

        return condition;
    }

    /**
     * Map Record to Role entity
     */
    private Role mapToRole(Record record) {
        if (record == null) return null;

        Role role = Role.builder()
                .name(record.get(ROLES.NAME))
                .description(record.get(ROLES.DESCRIPTION))
                .build();

        role.setId(record.get(ROLES.ID));
        role.setCreatedAt(record.get(ROLES.CREATED_AT));
        role.setUpdatedAt(record.get(ROLES.UPDATED_AT));

        return role;
    }

    // ==================== QUERY METHODS ====================

    /**
     * Tìm role theo ID
     */
    public Role getRoleById(UUID id) {
        if (id == null) return null;

        return dsl.select(getRoleFields())
                .from(ROLES)
                .where(ROLES.ID.eq(id))
                .fetchOptional()
                .map(this::mapToRole)
                .orElse(null);
    }

    /**
     * Tìm role theo name
     */
    public Role getRoleByName(String name) {
        if (StringUtils.isBlank(name)) return null;

        return dsl.select(getRoleFields())
                .from(ROLES)
                .where(ROLES.NAME.eq(name))
                .fetchOptional()
                .map(this::mapToRole)
                .orElse(null);
    }

    /**
     * Lấy tất cả roles của user
     */
    public List<Role> getRolesByUserId(UUID userId) {
        if (userId == null) return List.of();

        return dsl.select(getRoleFields())
                .from(USER_ROLES)
                .join(ROLES).on(USER_ROLES.ROLE_ID.eq(ROLES.ID))
                .where(USER_ROLES.USER_ID.eq(userId))
                .fetch()
                .map(this::mapToRole);
    }

    /**
     * Lấy role names của user (dùng cho JWT claims)
     */
    public List<String> getRoleNamesByUserId(UUID userId) {
        if (userId == null) return List.of();

        return dsl.select(ROLES.NAME)
                .from(USER_ROLES)
                .join(ROLES).on(USER_ROLES.ROLE_ID.eq(ROLES.ID))
                .where(USER_ROLES.USER_ID.eq(userId))
                .fetch(ROLES.NAME);
    }

    /**
     * Kiểm tra user có role không
     */
    public boolean hasRole(UUID userId, String roleName) {
        if (userId == null || StringUtils.isBlank(roleName)) return false;

        return dsl.fetchExists(
                dsl.selectOne()
                        .from(USER_ROLES)
                        .join(ROLES).on(USER_ROLES.ROLE_ID.eq(ROLES.ID))
                        .where(USER_ROLES.USER_ID.eq(userId))
                        .and(ROLES.NAME.eq(roleName))
        );
    }

    /**
     * Lấy tất cả roles với phân trang
     */
    public List<Role> findRolesByCriteria(String query, int offset, int limit) {
        Condition condition = getWhereCondition(query);

        return dsl.select(getRoleFields())
                .from(ROLES)
                .where(condition)
                .orderBy(ROLES.NAME)
                .offset(offset)
                .limit(limit)
                .fetch()
                .map(this::mapToRole);
    }

    /**
     * Đếm tổng roles
     */
    public long countRolesByCriteria(String query) {
        Condition condition = getWhereCondition(query);

        return dsl.selectCount()
                .from(ROLES)
                .where(condition)
                .fetchOne(0, Long.class);
    }

    /**
     * Lấy tất cả roles (không phân trang)
     */
    public List<Role> findAllRoles() {
        return dsl.select(getRoleFields())
                .from(ROLES)
                .orderBy(ROLES.NAME)
                .fetch()
                .map(this::mapToRole);
    }

    // ==================== MUTATION METHODS ====================

    /**
     * Tạo role mới
     */
    public Role insertRole(Role role) {
        LocalDateTime now = LocalDateTime.now();
        UUID roleId = role.getId() != null ? role.getId() : UUID.randomUUID();

        dsl.insertInto(ROLES)
                .set(ROLES.ID, roleId)
                .set(ROLES.NAME, role.getName())
                .set(ROLES.DESCRIPTION, role.getDescription())
                .set(ROLES.CREATED_AT, now)
                .set(ROLES.UPDATED_AT, now)
                .execute();

        role.setId(roleId);
        role.setCreatedAt(now);
        role.setUpdatedAt(now);
        return role;
    }

    /**
     * Cập nhật role
     */
    public Role updateRole(Role role) {
        LocalDateTime now = LocalDateTime.now();

        dsl.update(ROLES)
                .set(ROLES.NAME, role.getName())
                .set(ROLES.DESCRIPTION, role.getDescription())
                .set(ROLES.UPDATED_AT, now)
                .where(ROLES.ID.eq(role.getId()))
                .execute();

        role.setUpdatedAt(now);
        return role;
    }

    /**
     * Xóa role
     */
    public boolean deleteRole(UUID roleId) {
        if (roleId == null) return false;

        // Xóa permission mappings
        dsl.deleteFrom(ROLE_PERMISSIONS)
                .where(ROLE_PERMISSIONS.ROLE_ID.eq(roleId))
                .execute();

        // Xóa user role mappings
        dsl.deleteFrom(USER_ROLES)
                .where(USER_ROLES.ROLE_ID.eq(roleId))
                .execute();

        // Xóa role
        return dsl.deleteFrom(ROLES)
                .where(ROLES.ID.eq(roleId))
                .execute() > 0;
    }

    /**
     * Kiểm tra role name đã tồn tại
     */
    public boolean existsByName(String name, UUID excludeRoleId) {
        if (StringUtils.isBlank(name)) return false;

        Condition condition = ROLES.NAME.eq(name);
        if (excludeRoleId != null) {
            condition = condition.and(ROLES.ID.notEqual(excludeRoleId));
        }

        return dsl.fetchExists(
                dsl.selectOne().from(ROLES).where(condition)
        );
    }
}
