package com.eduplatform;

import com.eduplatform.common.vertx.server.VertxServerDeployer;
import com.eduplatform.config.GlobalExceptionHandler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
public class EduPlatformApplication {

    @Value("${server.port:8080}")
    private int port;

    public static void main(String[] args) {
        SpringApplication.run(EduPlatformApplication.class, args);
    }

    @Bean
    public CommandLineRunner startVertxServer(
            Vertx vertx,
            Router mainRouter,
            GlobalExceptionHandler exceptionHandler) {
        return args -> {
            // Register failure handler
            mainRouter.route().failureHandler(exceptionHandler);

            // Deploy HTTP server (set VERTX_INSTANCES=1 for dev, auto for prod)
            VertxServerDeployer.deploy(vertx, mainRouter, port)
                    .onSuccess(deploymentId -> {
                        String instances = System.getenv("VERTX_INSTANCES");
                        String instanceInfo = instances != null ? instances : "auto";
                        log.info("🚀 EduPlatform API Server started on port {} (instances: {})", port, instanceInfo);
                        log.info("📚 API Docs: http://localhost:{}/api/docs", port);
                    })
                    .onFailure(err -> {
                        log.error("❌ Failed to start server", err);
                        System.exit(1);
                    });
        };
    }
}
