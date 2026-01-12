package com.eduplatform.common.vertx.filter;

import io.reactivex.rxjava3.core.Completable;
import io.vertx.ext.web.RoutingContext;

/**
 * VertxFilter - Interface cho reactive filters
 * 
 * Dùng Completable thay vì void để support async filtering.
 * Filter chain sẽ chờ Completable complete trước khi gọi filter tiếp theo.
 * 
 * Usage:
 * <pre>
 * @Component
 * public class AuthFilter implements VertxFilter {
 *     
 *     @Override
 *     public String[] patterns() {
 *         return new String[]{"/api/*"};
 *     }
 *     
 *     @Override
 *     public Completable doFilter(RoutingContext ctx) {
 *         String token = ctx.request().getHeader("Authorization");
 *         if (token == null) {
 *             return Completable.error(new UnauthorizedException("Missing token"));
 *         }
 *         return validateToken(token)
 *             .doOnSuccess(user -> ctx.put("principal", user))
 *             .ignoreElement();  // Convert Single to Completable
 *     }
 * }
 * </pre>
 */
public interface VertxFilter {
    
    /**
     * URL patterns to apply this filter
     * Supports ant-style patterns: /api/*, /api/v1/users/**
     */
    String[] patterns();
    
    /**
     * Execute filter logic
     * Return Completable.complete() to continue, or Completable.error() to stop
     */
    Completable doFilter(RoutingContext ctx);
    
    /**
     * Filter order (lower = earlier)
     */
    default int order() {
        return 100;
    }
    
    /**
     * Excluded patterns (won't apply filter even if matches patterns())
     */
    default String[] excludePatterns() {
        return new String[0];
    }
}
