package com.eduplatform.identity.entity;

import com.eduplatform.common.domain.DataScope;
import com.eduplatform.entity.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * RbacPermission entity - Quyền hạn: Role - Resource - Actions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RbacPermission extends BaseEntity {
    private UUID roleId;
    private UUID permissionId;
    
    // Permission details
    private String resource;
    private String action;
    private String description;
    
    // Actions list (for grouped view)
    private List<String> actions;
    
    // Data scope: 1=All, 2=Department, 3=Own
    private DataScope dataScope;
}
