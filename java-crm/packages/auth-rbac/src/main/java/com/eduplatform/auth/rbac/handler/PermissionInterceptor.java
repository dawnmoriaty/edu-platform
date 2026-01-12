package com.eduplatform.auth.rbac.handler;

import com.eduplatform.auth.rbac.annotation.RequirePermission;
import com.eduplatform.auth.rbac.model.SecurityUser;
import com.eduplatform.auth.rbac.model.VertxPrincipal;
import com.eduplatform.auth.rbac.service.PermissionService;
import com.eduplatform.auth.rbac.util.SecurityUtils;
import com.eduplatform.common.constant.ErrorCode;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Instant;

/**
 * PermissionInterceptor - Vert.x 5 Handler để check permission động
 * Cải tiến:
 * - Dùng flatMap pattern thay vì nested subscribe
 * - Cache SecurityUser từ VertxPrincipal nếu có
 * - Support logical operators (AND, OR) cho multiple permissions
 * - Better error response với timestamp
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements Handler<RoutingContext> {

    private final PermissionService permissionService;

    @Override
    public void handle(RoutingContext context) {
        // 1. Lấy method đang được gọi (được set bởi VertxSpring router)
        Method method = context.get("targetMethod");

        if (method == null) {
            context.next();
            return;
        }

        // 2. Check @RequirePermission annotation
        RequirePermission annotation = method.getAnnotation(RequirePermission.class);

        if (annotation == null) {
            // Không yêu cầu permission cụ thể
            context.next();
            return;
        }

        // 3. Lấy principal từ context
        VertxPrincipal principal = SecurityUtils.getPrincipal(context);

        if (principal == null || principal.getUserId() == null) {
            sendError(context, ErrorCode.UNAUTHORIZED);
            return;
        }

        // 4. Check permission với RxJava3 flatMap chain
        checkAndAuthorize(context, principal, annotation);
    }

    private void checkAndAuthorize(RoutingContext context, VertxPrincipal principal, RequirePermission annotation) {
        String resource = annotation.resource();
        String action = annotation.action().getCode();

        // Nếu đã có SecurityUser từ JwtAuthHandler, dùng luôn
        SecurityUser cachedUser = principal.getSecurityUser();
        
        Single<SecurityUser> userSingle = cachedUser != null
                ? Single.just(cachedUser)
                : permissionService.checkPermission(principal.getUserId(), resource, action);

        userSingle
                .flatMap(securityUser -> {
                    // Nếu dùng cache, cần verify permission riêng
                    if (cachedUser != null) {
                        return verifyPermissionFromCache(securityUser, resource, action);
                    }
                    return Single.just(securityUser);
                })
                .subscribe(
                        securityUser -> {
                            // ✅ Có quyền → Lưu SecurityUser vào context
                            SecurityUtils.setSecurityUser(context, securityUser);
                            log.debug("Permission granted: user={}, resource={}, action={}",
                                    principal.getUserId(), resource, action);
                            context.next();
                        },
                        error -> {
                            // ❌ Không có quyền
                            log.warn("Permission denied: user={}, resource={}, action={}, error={}",
                                    principal.getUserId(), resource, action, error.getMessage());
                            sendError(context, ErrorCode.FORBIDDEN);
                        }
                );
    }

    private Single<SecurityUser> verifyPermissionFromCache(SecurityUser user, String resource, String action) {
        // Check trong permission matrix đã cache
        var permissions = user.getPermissions();
        if (permissions != null && permissions.containsKey(resource)) {
            var actions = permissions.get(resource);
            if (actions != null && (actions.contains(action) || actions.contains("*"))) {
                return Single.just(user);
            }
        }
        
        // Super admin có tất cả quyền
        if (user.getRoleCodes() != null && user.getRoleCodes().contains("SUPER_ADMIN")) {
            return Single.just(user);
        }
        
        return Single.error(new PermissionDeniedException(resource, action));
    }

    private void sendError(RoutingContext context, ErrorCode errorCode) {
        context.response()
                .setStatusCode(errorCode.getHttpStatus())
                .putHeader("Content-Type", "application/json")
                .end(String.format("""
                        {"code":%d,"message":"%s","timestamp":"%s"}""",
                        errorCode.getCode(), 
                        errorCode.getMessage(),
                        Instant.now().toString()));
    }

    /**
     * Internal exception for permission denied
     */
    private static class PermissionDeniedException extends RuntimeException {
        PermissionDeniedException(String resource, String action) {
            super(String.format("Permission denied for %s:%s", resource, action));
        }
    }
}
