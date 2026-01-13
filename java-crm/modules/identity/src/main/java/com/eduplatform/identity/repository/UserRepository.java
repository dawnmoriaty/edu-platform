package com.eduplatform.identity.repository;

import com.eduplatform.identity.entity.User;
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
 * UserRepository - jOOQ repository theo style TruyenRealm
 * Không dùng interface, viết trực tiếp class với DSLContext
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final DSLContext dsl;

    /**
     * Danh sách fields cơ bản của User
     */
    private static List<Field<?>> getUserFields() {
        return asList(
                USERS.ID,
                USERS.USERNAME,
                USERS.EMAIL,
                USERS.PASSWORD_HASH,
                USERS.FIRST_NAME,
                USERS.LAST_NAME,
                USERS.PHONE,
                USERS.STATUS,
                USERS.CREATED_AT,
                USERS.UPDATED_AT,
                USERS.CREATED_BY,
                USERS.UPDATED_BY
        );
    }

    /**
     * Build condition cho search/filter
     */
    private Condition getWhereCondition(List<UUID> userIds, String query, String status) {
        Condition condition = DSL.trueCondition();

        // Filter by user IDs
        if (userIds != null && !userIds.isEmpty()) {
            condition = condition.and(USERS.ID.in(userIds));
        }

        // Filter by status
        if (isNotEmpty(status)) {
            condition = condition.and(USERS.STATUS.eq(status));
        }

        // Search by username, email, name
        if (isNotEmpty(query)) {
            String searchTerm = "%" + query.trim().toLowerCase() + "%";
            condition = condition.and(
                    DSL.lower(USERS.USERNAME).like(searchTerm)
                            .or(DSL.lower(USERS.EMAIL).like(searchTerm))
                            .or(DSL.lower(USERS.FIRST_NAME).like(searchTerm))
                            .or(DSL.lower(USERS.LAST_NAME).like(searchTerm))
                            .or(USERS.PHONE.like("%" + query.trim() + "%"))
            );
        }

        return condition;
    }

    /**
     * Map Record to User entity
     */
    private User mapToUser(Record record) {
        if (record == null) return null;
        
        User user = User.builder()
                .username(record.get(USERS.USERNAME))
                .email(record.get(USERS.EMAIL))
                .passwordHash(record.get(USERS.PASSWORD_HASH))
                .firstName(record.get(USERS.FIRST_NAME))
                .lastName(record.get(USERS.LAST_NAME))
                .phone(record.get(USERS.PHONE))
                .build();
        
        user.setId(record.get(USERS.ID));
        user.setCreatedAt(record.get(USERS.CREATED_AT));
        user.setUpdatedAt(record.get(USERS.UPDATED_AT));
        user.setCreatedBy(record.get(USERS.CREATED_BY));
        user.setUpdatedBy(record.get(USERS.UPDATED_BY));
        
        // Parse status
        String status = record.get(USERS.STATUS);
        if (status != null) {
            try {
                user.setStatus(com.eduplatform.entity.enums.UserStatus.valueOf(status));
            } catch (IllegalArgumentException e) {
                user.setStatus(com.eduplatform.entity.enums.UserStatus.ACTIVE);
            }
        }
        
        return user;
    }

    // ==================== QUERY METHODS ====================

    /**
     * Tìm user theo ID
     */
    public User getUserById(UUID id) {
        if (id == null) return null;
        
        return dsl.select(getUserFields())
                .from(USERS)
                .where(USERS.ID.eq(id))
                .fetchOptional()
                .map(this::mapToUser)
                .orElse(null);
    }

    /**
     * Tìm user theo username
     */
    public User getUserByUsername(String username) {
        if (StringUtils.isBlank(username)) return null;
        
        return dsl.select(getUserFields())
                .from(USERS)
                .where(USERS.USERNAME.eq(username))
                .limit(1)
                .fetchOptional()
                .map(this::mapToUser)
                .orElse(null);
    }

    /**
     * Tìm user theo email
     */
    public User getUserByEmail(String email) {
        if (StringUtils.isBlank(email)) return null;
        
        return dsl.select(getUserFields())
                .from(USERS)
                .where(USERS.EMAIL.eq(email))
                .limit(1)
                .fetchOptional()
                .map(this::mapToUser)
                .orElse(null);
    }

    /**
     * Tìm user theo username hoặc email (dùng cho login)
     */
    public User getUserByUsernameOrEmail(String identity) {
        if (StringUtils.isBlank(identity)) return null;
        
        return dsl.select(getUserFields())
                .from(USERS)
                .where(USERS.USERNAME.eq(identity)
                        .or(USERS.EMAIL.eq(identity)))
                .limit(1)
                .fetchOptional()
                .map(this::mapToUser)
                .orElse(null);
    }

    /**
     * Kiểm tra username đã tồn tại (trừ user hiện tại)
     */
    public boolean existsByUsername(String username, UUID excludeUserId) {
        if (StringUtils.isBlank(username)) return false;
        
        Condition condition = USERS.USERNAME.eq(username);
        if (excludeUserId != null) {
            condition = condition.and(USERS.ID.notEqual(excludeUserId));
        }
        
        return dsl.fetchExists(
                dsl.selectOne().from(USERS).where(condition)
        );
    }

    /**
     * Kiểm tra email đã tồn tại (trừ user hiện tại)
     */
    public boolean existsByEmail(String email, UUID excludeUserId) {
        if (StringUtils.isBlank(email)) return false;
        
        Condition condition = USERS.EMAIL.eq(email);
        if (excludeUserId != null) {
            condition = condition.and(USERS.ID.notEqual(excludeUserId));
        }
        
        return dsl.fetchExists(
                dsl.selectOne().from(USERS).where(condition)
        );
    }

    /**
     * Lấy danh sách role IDs của user
     */
    public List<UUID> getRoleIds(UUID userId) {
        if (userId == null) return List.of();
        
        return dsl.select(USER_ROLES.ROLE_ID)
                .from(USER_ROLES)
                .where(USER_ROLES.USER_ID.eq(userId))
                .fetch(USER_ROLES.ROLE_ID);
    }

    /**
     * Tìm kiếm users với phân trang
     */
    public List<User> findUsersByCriteria(List<UUID> userIds, String query, String status,
                                          int offset, int limit) {
        Condition condition = getWhereCondition(userIds, query, status);
        
        return dsl.select(getUserFields())
                .from(USERS)
                .where(condition)
                .orderBy(USERS.CREATED_AT.desc())
                .offset(offset)
                .limit(limit)
                .fetch()
                .map(this::mapToUser);
    }

    /**
     * Đếm số users theo criteria
     */
    public long countUsersByCriteria(List<UUID> userIds, String query, String status) {
        Condition condition = getWhereCondition(userIds, query, status);
        
        return dsl.selectCount()
                .from(USERS)
                .where(condition)
                .fetchOne(0, Long.class);
    }

    // ==================== MUTATION METHODS ====================

    /**
     * Tạo user mới
     */
    public User insertUser(User user) {
        LocalDateTime now = LocalDateTime.now();
        UUID userId = user.getId() != null ? user.getId() : UUID.randomUUID();
        
        dsl.insertInto(USERS)
                .set(USERS.ID, userId)
                .set(USERS.USERNAME, user.getUsername())
                .set(USERS.EMAIL, user.getEmail())
                .set(USERS.PASSWORD_HASH, user.getPasswordHash())
                .set(USERS.FIRST_NAME, user.getFirstName())
                .set(USERS.LAST_NAME, user.getLastName())
                .set(USERS.PHONE, user.getPhone())
                .set(USERS.STATUS, user.getStatus() != null ? user.getStatus().name() : "ACTIVE")
                .set(USERS.CREATED_AT, now)
                .set(USERS.UPDATED_AT, now)
                .set(USERS.CREATED_BY, user.getCreatedBy())
                .set(USERS.UPDATED_BY, user.getUpdatedBy())
                .execute();
        
        user.setId(userId);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return user;
    }

    /**
     * Cập nhật user
     */
    public User updateUser(User user) {
        LocalDateTime now = LocalDateTime.now();
        
        dsl.update(USERS)
                .set(USERS.USERNAME, user.getUsername())
                .set(USERS.EMAIL, user.getEmail())
                .set(USERS.FIRST_NAME, user.getFirstName())
                .set(USERS.LAST_NAME, user.getLastName())
                .set(USERS.PHONE, user.getPhone())
                .set(USERS.STATUS, user.getStatus() != null ? user.getStatus().name() : null)
                .set(USERS.UPDATED_AT, now)
                .set(USERS.UPDATED_BY, user.getUpdatedBy())
                .where(USERS.ID.eq(user.getId()))
                .execute();
        
        user.setUpdatedAt(now);
        return user;
    }

    /**
     * Cập nhật password
     */
    public boolean updatePassword(UUID userId, String passwordHash) {
        if (userId == null) return false;
        
        return dsl.update(USERS)
                .set(USERS.PASSWORD_HASH, passwordHash)
                .set(USERS.UPDATED_AT, LocalDateTime.now())
                .where(USERS.ID.eq(userId))
                .execute() > 0;
    }

    /**
     * Cập nhật status
     */
    public boolean updateStatus(UUID userId, String status) {
        if (userId == null) return false;
        
        return dsl.update(USERS)
                .set(USERS.STATUS, status)
                .set(USERS.UPDATED_AT, LocalDateTime.now())
                .where(USERS.ID.eq(userId))
                .execute() > 0;
    }

    /**
     * Xóa user
     */
    public boolean deleteUser(UUID userId) {
        if (userId == null) return false;
        
        // Xóa role mappings trước
        dsl.deleteFrom(USER_ROLES)
                .where(USER_ROLES.USER_ID.eq(userId))
                .execute();
        
        // Xóa user
        return dsl.deleteFrom(USERS)
                .where(USERS.ID.eq(userId))
                .execute() > 0;
    }

    // ==================== ROLE ASSIGNMENT ====================

    /**
     * Gán role cho user
     */
    public void assignRole(UUID userId, UUID roleId) {
        if (userId == null || roleId == null) return;
        
        dsl.insertInto(USER_ROLES)
                .set(USER_ROLES.USER_ID, userId)
                .set(USER_ROLES.ROLE_ID, roleId)
                .set(USER_ROLES.CREATED_AT, LocalDateTime.now())
                .onDuplicateKeyIgnore()
                .execute();
    }

    /**
     * Gỡ role khỏi user
     */
    public boolean removeRole(UUID userId, UUID roleId) {
        if (userId == null || roleId == null) return false;
        
        return dsl.deleteFrom(USER_ROLES)
                .where(USER_ROLES.USER_ID.eq(userId))
                .and(USER_ROLES.ROLE_ID.eq(roleId))
                .execute() > 0;
    }

    /**
     * Thay thế tất cả roles của user
     */
    public void replaceRoles(UUID userId, List<UUID> roleIds) {
        if (userId == null) return;
        
        // Xóa roles cũ
        dsl.deleteFrom(USER_ROLES)
                .where(USER_ROLES.USER_ID.eq(userId))
                .execute();
        
        // Thêm roles mới
        if (roleIds != null && !roleIds.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (UUID roleId : roleIds) {
                dsl.insertInto(USER_ROLES)
                        .set(USER_ROLES.USER_ID, userId)
                        .set(USER_ROLES.ROLE_ID, roleId)
                        .set(USER_ROLES.CREATED_AT, now)
                        .execute();
            }
        }
    }
}
