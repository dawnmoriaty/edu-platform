package com.eduplatform.identity.entity;

import com.eduplatform.entity.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RbacResource entity - Các tài nguyên trong hệ thống
 * Dùng để quản lý phân quyền động
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RbacResource extends BaseEntity {
    private String code;        // CONTACT, USER, STUDENT
    private String name;        // Quản lý liên hệ
    private String uriPattern;  // /api/v1/contacts/**
    private Integer parentId;   // Parent resource (menu tree)
    private Integer sortOrder;
    
    private List<RbacResource> children;
}
