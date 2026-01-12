package com.eduplatform.auth.rbac.model;

import com.eduplatform.common.domain.DataScope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * SecurityUser - Thông tin user đã xác thực (Spring Boot 4 + Vert.x 5)
 * Cải tiến:
 * - Implements Serializable cho Redis cache
 * - Thêm helper methods cho permission check
 * - Support wildcard permissions
 * - Uses UUID for IDs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityUser implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 2L;
    
    private UUID id;
    private String username;
    private String email;
    private String name;
    private String avatar;
    
    // Role info
    private List<UUID> roleIds;
    private List<String> roleCodes;
    
    // Permission matrix: resource -> actions
    private Map<String, List<String>> permissions;
    
    // Data scope
    private DataScope dataScope;
    private UUID departmentId;
    private List<UUID> subordinateIds;

    /**
     * Check xem user có permission không (support wildcard)
     */
    public boolean hasPermission(String resource, String action) {
        if (isSuperAdmin()) {
            return true;
        }
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }
        
        List<String> actions = permissions.get(resource);
        if (actions == null) {
            // Check wildcard resource
            actions = permissions.get("*");
        }
        
        return actions != null && (actions.contains(action) || actions.contains("*"));
    }

    /**
     * Check xem user có tất cả permissions không
     */
    public boolean hasAllPermissions(Map<String, String> requiredPermissions) {
        if (isSuperAdmin()) {
            return true;
        }
        return requiredPermissions.entrySet().stream()
                .allMatch(entry -> hasPermission(entry.getKey(), entry.getValue()));
    }

    /**
     * Check xem user có bất kỳ permission nào không
     */
    public boolean hasAnyPermission(Map<String, String> requiredPermissions) {
        if (isSuperAdmin()) {
            return true;
        }
        return requiredPermissions.entrySet().stream()
                .anyMatch(entry -> hasPermission(entry.getKey(), entry.getValue()));
    }

    /**
     * Get all resources user has access to
     */
    public Set<String> getAccessibleResources() {
        if (permissions == null) {
            return Collections.emptySet();
        }
        return permissions.keySet();
    }

    /**
     * Check xem user có role cụ thể không
     */
    public boolean hasRole(String roleCode) {
        return roleCodes != null && roleCodes.contains(roleCode);
    }

    /**
     * Check xem user có bất kỳ role nào không
     */
    public boolean hasAnyRole(String... roleCodesToCheck) {
        if (roleCodes == null) {
            return false;
        }
        for (String role : roleCodesToCheck) {
            if (roleCodes.contains(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check xem user có phải super admin không
     */
    public boolean isSuperAdmin() {
        return roleCodes != null && roleCodes.contains("SUPER_ADMIN");
    }

    /**
     * Check xem user có phải admin không (bao gồm cả super admin)
     */
    public boolean isAdmin() {
        return roleCodes != null && 
               (roleCodes.contains("SUPER_ADMIN") || roleCodes.contains("ADMIN"));
    }

    /**
     * Check if user can access specific department's data
     */
    public boolean canAccessDepartment(UUID targetDepartmentId) {
        if (isSuperAdmin() || dataScope == DataScope.ALL) {
            return true;
        }
        if (dataScope == DataScope.DEPARTMENT) {
            return departmentId != null && departmentId.equals(targetDepartmentId);
        }
        return false;
    }

    /**
     * Check if user can access specific user's data
     */
    public boolean canAccessUser(UUID targetUserId) {
        if (isSuperAdmin() || dataScope == DataScope.ALL) {
            return true;
        }
        if (dataScope == DataScope.OWN) {
            return id != null && id.equals(targetUserId);
        }
        if (dataScope == DataScope.DEPARTMENT && subordinateIds != null) {
            return subordinateIds.contains(targetUserId);
        }
        return false;
    }
}
