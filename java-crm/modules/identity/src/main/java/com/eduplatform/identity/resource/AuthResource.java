package com.eduplatform.identity.resource;

import com.eduplatform.common.response.ApiResponse;
import com.eduplatform.common.vertx.annotation.*;
import com.eduplatform.common.vertx.model.VertxPrincipal;
import com.eduplatform.common.vertx.resource.BaseResource;
import com.eduplatform.identity.dto.request.LoginRequest;
import com.eduplatform.identity.dto.request.RefreshTokenRequest;
import com.eduplatform.identity.dto.request.RegisterRequest;
import com.eduplatform.identity.dto.response.AuthResponse;
import com.eduplatform.identity.service.AuthService;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

/**
 * AuthResource - Authentication endpoints (public, không cần permission)
 */
@Slf4j
@VertxRestController
@RequiredArgsConstructor
public class AuthResource extends BaseResource {

    private final AuthService authService;

    /**
     * POST /api/v1/auth/login
     */
    @VertxPost("/api/v1/auth/login")
    public Single<ResponseEntity<ApiResponse<AuthResponse>>> login(
            @VertxRequestBody LoginRequest request
    ) {
        log.debug("Login: {}", request.getIdentity());
        return query(() -> {
            String token = authService.login(request.getIdentity(), request.getPassword());
            return AuthResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .build();
        });
    }

    /**
     * POST /api/v1/auth/register
     */
    @VertxPost("/api/v1/auth/register")
    public Single<ResponseEntity<ApiResponse<AuthResponse>>> register(
            @VertxRequestBody RegisterRequest request
    ) {
        log.debug("Register: {}", request.getUsername());
        return query(() -> {
            var user = authService.register(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getName()
            );
            return AuthResponse.builder().user(user).build();
        });
    }

    /**
     * GET /api/v1/auth/me - Current user
     */
    @VertxGet("/api/v1/auth/me")
    public Single<ResponseEntity<ApiResponse<Object>>> getCurrentUser(VertxPrincipal principal) {
        return getUser(principal).map(this::ok);
    }

    /**
     * POST /api/v1/auth/refresh
     */
    @VertxPost("/api/v1/auth/refresh")
    public Single<ResponseEntity<ApiResponse<AuthResponse>>> refreshToken(
            @VertxRequestBody RefreshTokenRequest request
    ) {
        return query(() -> {
            String token = authService.refreshToken(request.getRefreshToken());
            return AuthResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .build();
        });
    }

    /**
     * POST /api/v1/auth/logout
     */
    @VertxPost("/api/v1/auth/logout")
    public Single<ResponseEntity<ApiResponse<String>>> logout(
            VertxPrincipal principal,
            io.vertx.ext.web.RoutingContext ctx
    ) {
        String authHeader = ctx.request().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Single.just(ok("Logged out"));
        }
        
        String token = authHeader.substring(7);
        return query(() -> {
            authService.logout(token);
            return "Logged out successfully";
        });
    }
}
