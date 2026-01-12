package com.eduplatform.identity.entity;

import com.eduplatform.entity.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Permission entity - Quyền hạn chi tiết
 * Ví dụ: VIEW_STUDENTS, EDIT_GRADES, APPROVE_ENTERPRISE
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Permission extends BaseEntity {
    private String code;        // VIEW_STUDENTS, EDIT_GRADES, etc.
    private String name;        // Display name
    private String module;      // Module: identity, training, career, finance
    private String description;
}
