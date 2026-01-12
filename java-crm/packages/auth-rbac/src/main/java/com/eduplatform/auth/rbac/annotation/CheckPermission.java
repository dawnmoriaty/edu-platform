package com.eduplatform.auth.rbac.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CheckPermission - Annotation để check permission dựa trên URI
 * 
 * Usage:
 * <pre>
 * public class ContactHandler extends BaseHandler {
 *     private static final String URI = "/contact/";
 *     
 *     @CheckPermission(action = "VIEW")
 *     public Handler<RoutingContext> list() { ... }
 *     
 *     @CheckPermission(action = "ADD")
 *     public Handler<RoutingContext> create() { ... }
 * }
 * </pre>
 * 
 * Hoặc override URI:
 * <pre>
 *     @CheckPermission(uri = "/custom/", action = "DELETE")
 *     public Handler<RoutingContext> delete() { ... }
 * </pre>
 */
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
