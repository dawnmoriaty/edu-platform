package com.eduplatform.config;

import com.eduplatform.auth.rbac.handler.JwtAuthHandler;
import com.eduplatform.common.vertx.routing.VertxRoutingBinder;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

/**
 * WebConfig - Vert.x Router Configuration
 * 
 * Sử dụng VertxRoutingBinder để auto-scan @VertxRestController
 * Không cần khai báo routes thủ công
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebConfig {

    private final JwtAuthHandler jwtAuthHandler;
    private final VertxRoutingBinder vertxRoutingBinder;

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

        // JWT Auth handler cho protected routes
        // Skip auth cho public routes
        router.route("/api/*").handler(ctx -> {
            String path = ctx.request().path();
            
            // Public routes - skip auth
            if (path.equals("/api/v1/auth/login") || 
                path.equals("/api/v1/auth/register")) {
                ctx.next();
                return;
            }
            
            // Protected routes - require auth
            jwtAuthHandler.handle(ctx);
        });

        // Auto-bind @VertxRestController routes
        log.info("Binding VertxRestController routes...");
        vertxRoutingBinder.bind(router, vertx);

        // Health check
        router.get("/health").handler(ctx ->
                ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end("{\"status\":\"UP\"}")
        );

        return router;
    }
}

