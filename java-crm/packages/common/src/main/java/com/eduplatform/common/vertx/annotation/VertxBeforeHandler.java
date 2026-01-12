package com.eduplatform.common.vertx.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * VertxBeforeHandler - Đánh dấu class là Before Handler (middleware)
 * 
 * Chạy TRƯỚC tất cả routes, dùng cho:
 * - Logging request
 * - Thêm headers
 * - Rate limiting
 * - Request validation
 * 
 * Usage:
 * <pre>
 * @VertxBeforeHandler
 * public class LoggingHandler implements VertxRouterBinder {
 *     @Override
 *     public void bind(Router router) {
 *         router.route().handler(ctx -> {
 *             log.info("{} {}", ctx.request().method(), ctx.request().uri());
 *             ctx.next();
 *         });
 *     }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface VertxBeforeHandler {
    
    /**
     * Order of execution (lower = earlier)
     */
    int order() default 100;
}
