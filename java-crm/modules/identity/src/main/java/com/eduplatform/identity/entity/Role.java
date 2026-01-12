package com.eduplatform.identity.entity;

import com.eduplatform.entity.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Role entity - Vai trò người dùng
 * Default roles: SUPER_ADMIN, SCHOOL_ADMIN, TEACHER, STUDENT, ENTERPRISE
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Role extends BaseEntity {
    private String code;        // SUPER_ADMIN, STUDENT, etc.
    private String name;        // Display name
    private String description;
    private boolean isSystem;   // System role cannot be deleted
    
    private List<Permission> permissions;
}
