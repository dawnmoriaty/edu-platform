package com.eduplatform.common.vertx.server;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * VertxServerDeployer - Helper để deploy HttpServerVerticle với multi-instancing
 * 
 * Tự động phát hiện số CPU cores và deploy tương ứng số instances.
 * Mỗi instance chạy trên 1 event loop riêng biệt.
 * 
 * Usage:
 * <pre>
 * VertxServerDeployer.deploy(vertx, router, 8080)
 *     .onSuccess(id -> log.info("Server started"))
 *     .onFailure(err -> log.error("Failed to start"));
 * </pre>
 */
@Slf4j
public class VertxServerDeployer {

    /**
     * Deploy HTTP server with default options (auto-detect CPU cores)
     */
    public static io.vertx.core.Future<String> deploy(Vertx vertx, Router router, int port) {
        return deploy(vertx, router, port, null, null);
    }

    /**
     * Deploy HTTP server with custom instance count
     */
    public static io.vertx.core.Future<String> deploy(Vertx vertx, Router router, int port, int instances) {
        DeploymentOptions options = new DeploymentOptions().setInstances(instances);
        return deploy(vertx, router, port, null, options);
    }

    /**
     * Deploy HTTP server with full customization
     * 
     * @param vertx Vertx instance
     * @param router Router với routes đã được bind
     * @param port Port to listen
     * @param serverOptions HTTP server options (compression, timeout, etc.)
     * @param deploymentOptions Deployment options (instances, worker, etc.)
     */
    public static io.vertx.core.Future<String> deploy(
            Vertx vertx, 
            Router router, 
            int port,
            HttpServerOptions serverOptions,
            DeploymentOptions deploymentOptions) {
        
        // Default deployment options: based on VERTX_INSTANCES env
        if (deploymentOptions == null) {
            int instances = calculateOptimalInstances();
            deploymentOptions = new DeploymentOptions()
                    .setInstances(instances);
        }

        // Create Verticle supplier để mỗi instance có riêng Verticle
        final HttpServerOptions finalServerOptions = serverOptions;
        final DeploymentOptions finalDeploymentOptions = deploymentOptions;
        
        return vertx.deployVerticle(
                () -> new HttpServerVerticle(router, port, finalServerOptions),
                finalDeploymentOptions
        );
    }

    /**
     * Deploy và block cho đến khi hoàn thành (dùng trong CommandLineRunner)
     */
    public static String deployBlocking(Vertx vertx, Router router, int port) throws Exception {
        return deployBlocking(vertx, router, port, 30, TimeUnit.SECONDS);
    }

    public static String deployBlocking(Vertx vertx, Router router, int port, 
                                        long timeout, TimeUnit unit) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> deploymentId = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();

        deploy(vertx, router, port)
                .onSuccess(id -> {
                    deploymentId.set(id);
                    latch.countDown();
                })
                .onFailure(err -> {
                    error.set(err);
                    latch.countDown();
                });

        if (!latch.await(timeout, unit)) {
            throw new RuntimeException("Timeout waiting for server deployment");
        }

        if (error.get() != null) {
            throw new RuntimeException("Failed to deploy server", error.get());
        }

        return deploymentId.get();
    }

    /**
     * Calculate optimal number of instances
     * 
     * DEV mode (default): 1 instance - like Spring Boot
     * PROD mode: Set VERTX_INSTANCES env to auto-detect or specific number
     *   - VERTX_INSTANCES=auto → use CPU cores
     *   - VERTX_INSTANCES=8 → use 8 instances
     */
    private static int calculateOptimalInstances() {
        // Check environment variable
        String envInstances = System.getenv("VERTX_INSTANCES");
        
        if (envInstances == null || envInstances.isBlank()) {
            // DEV default: 1 instance like Spring Boot
            log.info("Using default 1 HTTP server verticle instance (set VERTX_INSTANCES for more)");
            return 1;
        }
        
        // PROD: "auto" = CPU cores, number = specific count
        if ("auto".equalsIgnoreCase(envInstances.trim())) {
            int cores = Runtime.getRuntime().availableProcessors();
            int instances = Math.max(2, Math.min(cores, 16));
            log.info("Auto-detected {} HTTP server verticle instances (based on {} CPU cores)", 
                    instances, cores);
            return instances;
        }
        
        try {
            int instances = Integer.parseInt(envInstances.trim());
            if (instances > 0) {
                log.info("Using configured {} HTTP server verticle instance(s)", instances);
                return Math.min(instances, 32);
            }
        } catch (NumberFormatException ignored) {
            // Fall through to default
        }
        
        return 1;
    }
}
