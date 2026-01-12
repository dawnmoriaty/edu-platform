package com.eduplatform.identity.entity;

import com.eduplatform.common.domain.DataScope;
import com.eduplatform.entity.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RbacPermission entity - Quyền hạn: Role - Resource - Actions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RbacPermission extends BaseEntity {
    private Integer roleId;
    private Integer resourceId;
    
    // Actions: ["VIEW", "ADD", "UPDATE", "DELETE"]
    private List<String> actions;
    
    // Data scope: 1=All, 2=Department, 3=Own
    private DataScope dataScope;
    
    // Joined fields
    private String resourceCode;
    private String resourceName;
}
