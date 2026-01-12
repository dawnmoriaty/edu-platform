package com.eduplatform.identity.handler;

import com.eduplatform.auth.rbac.model.SecurityUser;
import com.eduplatform.common.response.ApiResponse;
import com.eduplatform.identity.dto.request.LoginRequest;
import com.eduplatform.identity.dto.request.RegisterRequest;
import com.eduplatform.identity.dto.response.AuthResponse;
import com.eduplatform.identity.service.AuthService;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * AuthHandler - Vert.x Handler cho authentication
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthHandler {

    private final AuthService authService;

    /**
     * POST /api/v1/auth/login
     */
    public Handler<RoutingContext> login() {
        return ctx -> {
            JsonObject body = ctx.body().asJsonObject();
            String identity = body.getString("username", body.getString("email"));
            String password = body.getString("password");

            authService.login(identity, password)
                    .subscribe(
                            token -> {
                                AuthResponse response = AuthResponse.builder()
                                        .accessToken(token)
                                        .tokenType("Bearer")
                                        .build();
                                sendSuccess(ctx, response);
                            },
                            error -> sendError(ctx, error)
                    );
        };
    }

    /**
     * POST /api/v1/auth/register
     */
    public Handler<RoutingContext> register() {
        return ctx -> {
            JsonObject body = ctx.body().asJsonObject();
            String username = body.getString("username");
            String email = body.getString("email");
            String password = body.getString("password");
            String name = body.getString("name");

            authService.register(username, email, password, name)
                    .subscribe(
                            user -> {
                                AuthResponse response = AuthResponse.builder()
                                        .user(user)
                                        .build();
                                sendSuccess(ctx, response);
                            },
                            error -> sendError(ctx, error)
                    );
        };
    }

    /**
     * GET /api/v1/auth/me
     */
    public Handler<RoutingContext> getCurrentUser() {
        return ctx -> {
            String authHeader = ctx.request().getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                ctx.response()
                        .setStatusCode(401)
                        .putHeader("Content-Type", "application/json")
                        .end("{\"code\":401,\"message\":\"Unauthorized\"}");
                return;
            }

            String token = authHeader.substring(7);
            authService.getCurrentUser(token)
                    .subscribe(
                            user -> sendSuccess(ctx, user),
                            error -> sendError(ctx, error)
                    );
        };
    }

    /**
     * POST /api/v1/auth/refresh
     */
    public Handler<RoutingContext> refreshToken() {
        return ctx -> {
            JsonObject body = ctx.body().asJsonObject();
            String refreshToken = body.getString("refreshToken");

            authService.refreshToken(refreshToken)
                    .subscribe(
                            token -> {
                                AuthResponse response = AuthResponse.builder()
                                        .accessToken(token)
                                        .tokenType("Bearer")
                                        .build();
                                sendSuccess(ctx, response);
                            },
                            error -> sendError(ctx, error)
                    );
        };
    }

    private <T> void sendSuccess(RoutingContext ctx, T data) {
        ApiResponse<T> response = ApiResponse.success(data);
        ctx.response()
                .setStatusCode(200)
                .putHeader("Content-Type", "application/json")
                .end(Json.encode(response));
    }

    private void sendError(RoutingContext ctx, Throwable error) {
        log.error("Auth error: {}", error.getMessage());
        ctx.response()
                .setStatusCode(500)
                .putHeader("Content-Type", "application/json")
                .end(String.format("{\"code\":500,\"message\":\"%s\"}", error.getMessage()));
    }
}
