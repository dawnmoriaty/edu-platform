package com.eduplatform.auth.rbac.handler;

import com.eduplatform.auth.rbac.model.SecurityUser;
import com.eduplatform.auth.rbac.model.VertxPrincipal;
import com.eduplatform.auth.rbac.service.TokenService;
import com.eduplatform.auth.rbac.util.SecurityUtils;
import com.eduplatform.common.constant.ErrorCode;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * JwtAuthHandler - Vert.x 5 Handler để xác thực JWT token
 * Cải tiến:
 * - Dùng flatMap chain thay vì nested subscribe (cleaner)
 * - Support cả access token và refresh token validation
 * - Better error handling với specific error codes
 * - Thread-safe với RxJava3 Schedulers
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthHandler implements Handler<RoutingContext> {

    private final TokenService tokenService;

    @Override
    public void handle(RoutingContext context) {
        String authHeader = context.request().getHeader("Authorization");
        String token = SecurityUtils.extractToken(authHeader);

        if (token == null) {
            // Không có token, vẫn cho qua (để check @RequireAuth ở layer sau)
            context.next();
            return;
        }

        // Validate và extract user bằng RxJava3 flatMap chain (không nested subscribe)
        validateAndExtract(token, context);
    }

    private void validateAndExtract(String token, RoutingContext context) {
        tokenService.validate(token)
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Single.error(new TokenException(ErrorCode.TOKEN_INVALID));
                    }
                    return tokenService.isExpired(token);
                })
                .flatMap(isExpired -> {
                    if (isExpired) {
                        return Single.error(new TokenException(ErrorCode.TOKEN_EXPIRED));
                    }
                    return buildPrincipal(token);
                })
                .subscribe(
                        principal -> {
                            SecurityUtils.setPrincipal(context, principal);
                            context.next();
                        },
                        error -> handleError(context, error)
                );
    }

    private Single<VertxPrincipal> buildPrincipal(String token) {
        return tokenService.getSecurityUser(token)
                .map(securityUser -> VertxPrincipal.builder()
                        .userId(securityUser.getId())
                        .username(securityUser.getUsername())
                        .token(token)
                        .securityUser(securityUser)
                        .build());
    }

    private void handleError(RoutingContext context, Throwable error) {
        if (error instanceof TokenException tokenEx) {
            log.debug("Token validation failed: {}", tokenEx.getErrorCode().getMessage());
            sendError(context, tokenEx.getErrorCode());
        } else {
            log.error("Unexpected error during token validation", error);
            sendError(context, ErrorCode.TOKEN_INVALID);
        }
    }

    private void sendError(RoutingContext context, ErrorCode errorCode) {
        context.response()
                .setStatusCode(errorCode.getHttpStatus())
                .putHeader("Content-Type", "application/json")
                .end(String.format("""
                        {"code":%d,"message":"%s","timestamp":"%s"}""",
                        errorCode.getCode(), 
                        errorCode.getMessage(),
                        java.time.Instant.now().toString()));
    }

    /**
     * Internal exception for token validation errors
     */
    @lombok.Getter
    private static class TokenException extends RuntimeException {
        private final ErrorCode errorCode;

        TokenException(ErrorCode errorCode) {
            super(errorCode.getMessage());
            this.errorCode = errorCode;
        }
    }
}
