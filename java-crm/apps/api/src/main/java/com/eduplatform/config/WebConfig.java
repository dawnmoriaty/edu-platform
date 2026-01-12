package com.eduplatform.config;

import com.eduplatform.auth.rbac.handler.JwtAuthHandler;
import com.eduplatform.auth.rbac.handler.PermissionInterceptor;
import com.eduplatform.identity.handler.AuthHandler;
import com.eduplatform.identity.handler.UserHandler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

/**
 * WebConfig - Vert.x Router Configuration
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig {

    private final JwtAuthHandler jwtAuthHandler;
    private final PermissionInterceptor permissionInterceptor;
    private final AuthHandler authHandler;
    private final UserHandler userHandler;

    @Bean
    public Vertx vertx() {
        return Vertx.vertx();
    }

    @Bean
    public Router mainRouter(Vertx vertx) {
        Router router = Router.router(vertx);

        // Body handler for JSON parsing
        router.route().handler(BodyHandler.create());

        // CORS handler
        Set<String> allowedHeaders = new HashSet<>();
        allowedHeaders.add("Authorization");
        allowedHeaders.add("Content-Type");
        allowedHeaders.add("Accept");

        router.route().handler(CorsHandler.create()
                .addOrigin("*")
                .allowedHeaders(allowedHeaders)
                .allowedMethod(io.vertx.core.http.HttpMethod.GET)
                .allowedMethod(io.vertx.core.http.HttpMethod.POST)
                .allowedMethod(io.vertx.core.http.HttpMethod.PUT)
                .allowedMethod(io.vertx.core.http.HttpMethod.DELETE)
                .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS));

        // Public routes (no auth required)
        router.post("/api/v1/auth/login").handler(authHandler.login());
        router.post("/api/v1/auth/register").handler(authHandler.register());

        // Protected routes
        router.route("/api/*").handler(jwtAuthHandler);
        router.route("/api/*").handler(permissionInterceptor);

        // Auth routes
        router.get("/api/v1/auth/me").handler(authHandler.getCurrentUser());
        router.post("/api/v1/auth/refresh").handler(authHandler.refreshToken());

        // User routes
        router.get("/api/v1/users/me").handler(userHandler.getCurrentUser());
        router.get("/api/v1/users").handler(userHandler.getUsers());
        router.get("/api/v1/users/:id").handler(userHandler.getUserById());

        // Health check
        router.get("/health").handler(ctx ->
                ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end("{\"status\":\"UP\"}")
        );

        return router;
    }
}

