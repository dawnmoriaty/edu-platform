package com.eduplatform.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcServerConfiguration {

    private final int port;
    private Server server;

    public GrpcServerConfiguration(@Value("${grpc.server.port:9090}") int port) {
        this.port = port;
    }

    @Bean
    public Server grpcServer() {
        server = ServerBuilder.forPort(port)
                .addService(new UserServiceImpl()) // TODO: Add service implementation
                .build();
        
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start gRPC server", e);
        }
        
        return server;
    }

    @PreDestroy
    public void shutdown() {
        if (server != null) {
            server.shutdown();
        }
    }
}
