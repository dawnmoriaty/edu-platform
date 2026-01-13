package com.eduplatform.auth.rbac.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CheckPermission - Annotation để check permission dựa trên URI
 * 
 * @deprecated Use {@link com.eduplatform.common.vertx.annotation.RequirePermission} instead.
 * RequirePermission uses Action enum (type-safe) and is auto-checked by VertxRoutingBinder.
 * 
 * Migration:
 * <pre>
 * // Old:
 * @CheckPermission(uri = "/user/", action = "VIEW")
 * 
 * // New:
 * @RequirePermission(resource = "USER", action = Action.VIEW)
 * </pre>
 */
@Deprecated(since = "2.0", forRemoval = true)
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckPermission {
    
    /**
     * URI resource (nếu không set sẽ lấy từ field URI của class)
     */
    String uri() default "";
    
    /**
     * Action: VIEW, ADD, UPDATE, DELETE, EXPORT, IMPORT, etc.
     */
    String action();
    
    /**
     * Có yêu cầu login không (default: true)
     */
    boolean requireAuth() default true;
    
    /**
     * Có bỏ qua check permission cho super admin không (default: true)
     */
    boolean skipForSuperAdmin() default true;
}
