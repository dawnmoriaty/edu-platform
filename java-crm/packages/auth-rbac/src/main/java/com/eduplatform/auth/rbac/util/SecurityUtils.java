package com.eduplatform.auth.rbac.util;

import com.eduplatform.auth.rbac.model.SecurityUser;
import com.eduplatform.auth.rbac.model.VertxPrincipal;
import io.vertx.ext.web.RoutingContext;

import java.util.Optional;
import java.util.UUID;

/**
 * SecurityUtils - Helper methods cho security với Vert.x 5
 * Cải tiến:
 * - Dùng Optional thay vì null check
 * - Thêm type-safe helper methods
 * - Support context data keys as constants
 * - Uses UUID for user IDs
 */
public final class SecurityUtils {

    // Context keys (type-safe)
    public static final String KEY_PRINCIPAL = "principal";
    public static final String KEY_SECURITY_USER = "securityUser";
    public static final String KEY_TARGET_METHOD = "targetMethod";

    private SecurityUtils() {}

    /**
     * Lấy userId từ principal
     */
    public static UUID userId(VertxPrincipal principal) {
        return principal != null ? principal.getUserId() : null;
    }

    /**
     * Lấy userId từ context (shortcut)
     */
    public static Optional<UUID> getUserId(RoutingContext context) {
        return getPrincipalOpt(context).map(VertxPrincipal::getUserId);
    }

    /**
     * Lấy SecurityUser từ RoutingContext
     */
    public static SecurityUser getSecurityUser(RoutingContext context) {
        return context.get(KEY_SECURITY_USER);
    }

    /**
     * Lấy SecurityUser từ RoutingContext với Optional
     */
    public static Optional<SecurityUser> getSecurityUserOpt(RoutingContext context) {
        return Optional.ofNullable(context.get(KEY_SECURITY_USER));
    }

    /**
     * Lấy VertxPrincipal từ RoutingContext
     */
    public static VertxPrincipal getPrincipal(RoutingContext context) {
        return context.get(KEY_PRINCIPAL);
    }

    /**
     * Lấy VertxPrincipal từ RoutingContext với Optional
     */
    public static Optional<VertxPrincipal> getPrincipalOpt(RoutingContext context) {
        return Optional.ofNullable(context.get(KEY_PRINCIPAL));
    }

    /**
     * Set SecurityUser vào RoutingContext
     */
    public static void setSecurityUser(RoutingContext context, SecurityUser user) {
        context.put(KEY_SECURITY_USER, user);
    }

    /**
     * Set VertxPrincipal vào RoutingContext
     */
    public static void setPrincipal(RoutingContext context, VertxPrincipal principal) {
        context.put(KEY_PRINCIPAL, principal);
        // Cũng set SecurityUser nếu có trong principal
        if (principal != null && principal.getSecurityUser() != null) {
            setSecurityUser(context, principal.getSecurityUser());
        }
    }

    /**
     * Extract Bearer token từ Authorization header
     */
    public static String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Check if request is authenticated
     */
    public static boolean isAuthenticated(RoutingContext context) {
        return getPrincipal(context) != null;
    }

    /**
     * Check if user has specific role
     */
    public static boolean hasRole(RoutingContext context, String roleCode) {
        return getSecurityUserOpt(context)
                .map(SecurityUser::getRoleCodes)
                .map(roles -> roles.contains(roleCode))
                .orElse(false);
    }

    /**
     * Check if user has any of the specified roles
     */
    public static boolean hasAnyRole(RoutingContext context, String... roleCodes) {
        return getSecurityUserOpt(context)
                .map(SecurityUser::getRoleCodes)
                .map(roles -> {
                    for (String role : roleCodes) {
                        if (roles.contains(role)) return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    /**
     * Check if user is super admin
     */
    public static boolean isSuperAdmin(RoutingContext context) {
        return hasRole(context, "SUPER_ADMIN");
    }
}
