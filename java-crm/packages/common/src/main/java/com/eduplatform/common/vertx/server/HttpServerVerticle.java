package com.eduplatform.common.vertx.server;

import com.eduplatform.common.vertx.VertxWrapper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;

/**
 * HttpServerVerticle - Verticle chính để chạy HTTP Server
 * 
 * Thay vì chạy server trực tiếp trong CommandLineRunner,
 * sử dụng Verticle pattern để tận dụng multi-instancing của Vert.x.
 * 
 * Multi-instancing:
 * - Mỗi CPU core chạy 1 instance của Verticle
 * - Vert.x tự động load balance requests giữa các instances
 * - Tận dụng tối đa tài nguyên phần cứng
 * 
 * Lifecycle hooks:
 * - onBeforeStart(): Trước khi start server
 * - onAfterStart(): Sau khi server started
 * - onBeforeStop(): Trước khi stop server
 * 
 * So với spring-vertx-core:
 * - Không dùng RxJava verticle wrapper (nhẹ hơn)
 * - Không dùng reflection hack cho URL
 * - Có HTTP server options (compression, TCP tuning)
 * - Có multi-instance support built-in
 */
@Slf4j
public class HttpServerVerticle extends AbstractVerticle {

    private final Router router;
    private final int port;
    private final HttpServerOptions options;
    private HttpServer server;

    public HttpServerVerticle(Router router, int port) {
        this(router, port, defaultOptions());
    }

    public HttpServerVerticle(Router router, int port, HttpServerOptions options) {
        this.router = router;
        this.port = port;
        this.options = options != null ? options : defaultOptions();
    }

    private static HttpServerOptions defaultOptions() {
        return new HttpServerOptions()
                .setCompressionSupported(true)  // Enable gzip compression
                .setTcpFastOpen(true)           // TCP Fast Open for performance
                .setTcpNoDelay(true)            // Disable Nagle's algorithm
                .setTcpQuickAck(true)           // Quick ACK mode
                .setReusePort(true)             // Allow multiple verticles to bind to same port
                .setIdleTimeout(120);           // Connection idle timeout in seconds
    }

    @Override
    public void start(Promise<Void> startPromise) {
        // Init VertxWrapper for static access
        VertxWrapper.init(vertx);

        try {
            // Lifecycle hook: before start
            onBeforeStart();
            
            server = vertx.createHttpServer(options)
                    .requestHandler(router);

            server.listen(port)
                    .onSuccess(httpServer -> {
                        String verticleId = context.deploymentID();
                        String shortId = verticleId.length() > 8 ? verticleId.substring(0, 8) : verticleId;
                        log.info("🚀 HTTP Server Verticle [{}] started on port {}", 
                                shortId, httpServer.actualPort());
                        
                        // Lifecycle hook: after start
                        try {
                            onAfterStart();
                        } catch (Exception e) {
                            log.warn("onAfterStart hook failed: {}", e.getMessage());
                        }
                        
                        startPromise.complete();
                    })
                    .onFailure(err -> {
                        log.error("❌ Failed to start HTTP Server Verticle", err);
                        startPromise.fail(err);
                    });
        } catch (Exception e) {
            startPromise.fail(e);
        }
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        try {
            onBeforeStop();
        } catch (Exception e) {
            log.warn("onBeforeStop hook failed: {}", e.getMessage());
        }
        
        if (server != null) {
            server.close()
                    .onSuccess(v -> {
                        log.info("HTTP Server Verticle stopped");
                        stopPromise.complete();
                    })
                    .onFailure(stopPromise::fail);
        } else {
            stopPromise.complete();
        }
    }
    
    // ============================================
    // Lifecycle hooks - Override để customize
    // ============================================
    
    /**
     * Called before HTTP server starts
     * Override để init resources, register event bus handlers, etc.
     */
    protected void onBeforeStart() throws Exception {
        // Override in subclass
    }
    
    /**
     * Called after HTTP server successfully started
     * Override để register additional handlers, start background tasks, etc.
     */
    protected void onAfterStart() throws Exception {
        // Override in subclass
    }
    
    /**
     * Called before HTTP server stops
     * Override để cleanup resources, close connections, etc.
     */
    protected void onBeforeStop() throws Exception {
        // Override in subclass
    }
    
    // ============================================
    // Utility methods
    // ============================================
    
    /**
     * Get EventBus for inter-verticle communication
     */
    protected EventBus eventBus() {
        return vertx.eventBus();
    }
    
    /**
     * Get deployment ID
     */
    protected String deploymentId() {
        return context.deploymentID();
    }
    
    /**
     * Check if this is the first instance (useful for one-time initialization)
     */
    protected boolean isFirstInstance() {
        return deploymentId().endsWith("-0");
    }
}
