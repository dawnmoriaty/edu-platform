package com.eduplatform.identity.handler;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * IdentityRoutes - Vert.x Router configuration cho Identity module
 */
@Configuration
@RequiredArgsConstructor
public class IdentityRoutes {

    private final AuthHandler authHandler;
    private final UserHandler userHandler;

    @Bean
    public Router identityRouter(Vertx vertx) {
        Router router = Router.router(vertx);

        // Auth routes - không cần authentication
        router.post("/api/v1/auth/login").handler(authHandler.login());
        router.post("/api/v1/auth/register").handler(authHandler.register());
        router.get("/api/v1/auth/me").handler(authHandler.getCurrentUser());
        router.post("/api/v1/auth/refresh").handler(authHandler.refreshToken());

        // User routes - cần authentication
        router.get("/api/v1/users/me").handler(userHandler.getCurrentUser());
        router.get("/api/v1/users").handler(userHandler.getUsers());
        router.get("/api/v1/users/:id").handler(userHandler.getUserById());

        return router;
    }
}
