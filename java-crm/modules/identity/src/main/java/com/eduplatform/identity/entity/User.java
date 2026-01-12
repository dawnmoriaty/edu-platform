package com.eduplatform.identity.entity;

import com.eduplatform.entity.base.BaseEntity;
import com.eduplatform.entity.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * User entity - Tài khoản người dùng
 * Dùng chung cho tất cả các vai trò
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
    private String username;
    private String email;
    private String password;
    private String name;
    private String avatar;
    private UserStatus status;
    
    // Roles (Many-to-Many)
    private List<Integer> roleIds;
    private List<Role> roles;

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
    
    public List<String> getRoleCodes() {
        if (roles == null) return List.of();
        return roles.stream().map(Role::getCode).toList();
    }
}
