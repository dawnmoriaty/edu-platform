package com.eduplatform.common.vertx.annotation;

import com.eduplatform.common.constant.Action;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RequirePermission - Annotation để declare permission metadata
 * 
 * Sử dụng kết hợp với PermissionInterceptor để tự động check quyền
 * 
 * Usage:
 * <pre>
 * @VertxGet("/api/v1/contacts")
 * @RequirePermission(resource = "CONTACT", action = Action.VIEW)
 * public Single<ResponseEntity<...>> list(...) {
 *     // Interceptor đã check permission
 *     // Chỉ cần lấy employee từ context
 *     return getAuthenticatedUser(principal)
 *         .flatMap(user -> ...);
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequirePermission {
    
    /**
     * Resource code (e.g., "CONTACT", "USER", "ORDER")
     */
    String resource();
    
    /**
     * Action required
     */
    Action action();
    
    /**
     * Có filter theo data scope không?
     * Nếu true, chỉ trả về data mà user có quyền xem
     */
    boolean dataScope() default false;
}
