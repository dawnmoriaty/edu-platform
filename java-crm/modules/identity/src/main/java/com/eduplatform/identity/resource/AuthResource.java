package com.eduplatform.identity.resource;

import com.eduplatform.common.response.ApiResponse;
import com.eduplatform.common.vertx.annotation.*;
import com.eduplatform.common.vertx.model.VertxPrincipal;
import com.eduplatform.common.vertx.resource.BaseResource;
import com.eduplatform.identity.dto.request.LoginRequest;
import com.eduplatform.identity.dto.request.RegisterRequest;
import com.eduplatform.identity.dto.response.AuthResponse;
import com.eduplatform.identity.service.AuthService;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

/**
 * AuthResource - Authentication endpoints
 * 
 * Sử dụng @VertxRestController pattern như TruyenRealm
 * Không cần implements interface, code đơn giản trực tiếp
 */
@Slf4j
@VertxRestController
@RequiredArgsConstructor
public class AuthResource extends BaseResource {

    private static final String URI = "/auth/";
    
    private final AuthService authService;

    /**
     * POST /api/v1/auth/login - Public, không cần auth
     */
    @VertxPost("/api/v1/auth/login")
    public Single<ResponseEntity<ApiResponse<AuthResponse>>> login(
            @VertxRequestBody LoginRequest request
    ) {
        log.debug("Login request for: {}", request.getIdentity());
        
        return Single.fromCallable(() -> authService.login(request.getIdentity(), request.getPassword()))
                .subscribeOn(Schedulers.io())
                .map(token -> {
                    AuthResponse response = AuthResponse.builder()
                            .accessToken(token)
                            .tokenType("Bearer")
                            .build();
                    return okEntity(response);
                });
    }

    /**
     * POST /api/v1/auth/register - Public, không cần auth
     */
    @VertxPost("/api/v1/auth/register")
    public Single<ResponseEntity<ApiResponse<AuthResponse>>> register(
            @VertxRequestBody RegisterRequest request
    ) {
        log.debug("Register request for: {}", request.getUsername());
        
        return Single.fromCallable(() -> authService.register(
                        request.getUsername(),
                        request.getEmail(),
                        request.getPassword(),
                        request.getName()))
                .subscribeOn(Schedulers.io())
                .map(user -> {
                    AuthResponse response = AuthResponse.builder()
                            .user(user)
                            .build();
                    return okEntity(response);
                });
    }

    /**
     * GET /api/v1/auth/me - Cần auth
     * VertxPrincipal được inject tự động từ context
     */
    @VertxGet("/api/v1/auth/me")
    public Single<ResponseEntity<ApiResponse<Object>>> getCurrentUser(
            VertxPrincipal principal
    ) {
        if (principal == null || principal.getUserId() == null) {
            return getCustomError("Unauthorized");
        }
        
        // Lấy user từ principal (đã được cache bởi auth interceptor)
        Object user = principal.getSecurityUser();
        if (user != null) {
            return Single.just(okEntity(user));
        }
        
        // Fallback: không có user trong principal
        return getCustomError("User not found");
    }

    /**
     * POST /api/v1/auth/refresh - Refresh token
     */
    @VertxPost("/api/v1/auth/refresh")
    public Single<ResponseEntity<ApiResponse<AuthResponse>>> refreshToken(
            @VertxRequestBody RefreshTokenRequest request
    ) {
        return Single.fromCallable(() -> authService.refreshToken(request.getRefreshToken()))
                .subscribeOn(Schedulers.io())
                .map(token -> {
                    AuthResponse response = AuthResponse.builder()
                            .accessToken(token)
                            .tokenType("Bearer")
                            .build();
                    return okEntity(response);
                });
    }

    /**
     * POST /api/v1/auth/logout - Logout (invalidate token)
     */
    @VertxPost("/api/v1/auth/logout")
    public Single<ResponseEntity<ApiResponse<String>>> logout(
            VertxPrincipal principal,
            io.vertx.ext.web.RoutingContext ctx
    ) {
        if (principal == null) {
            return Single.just(okEntity("Logged out"));
        }
        
        // Lấy token từ Authorization header
        String authHeader = ctx.request().getHeader("Authorization");
        String token = (authHeader != null && authHeader.startsWith("Bearer ")) 
                ? authHeader.substring(7) : null;
        
        if (token == null) {
            return Single.just(okEntity("Logged out"));
        }
        
        return Single.fromCallable(() -> {
                    authService.logout(token);
                    return "Logged out successfully";
                })
                .subscribeOn(Schedulers.io())
                .map(this::okEntity);
    }

    // DTO cho refresh token
    @lombok.Data
    public static class RefreshTokenRequest {
        private String refreshToken;
    }
}
