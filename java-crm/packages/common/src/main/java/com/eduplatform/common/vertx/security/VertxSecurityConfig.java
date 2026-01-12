package com.eduplatform.common.vertx.security;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * VertxSecurityConfig - Cấu hình bảo mật cho Vert.x Router
 * 
 * Cung cấp:
 * - BodyHandler với giới hạn kích thước để chống DoS
 * - Phân tách public routes và protected routes
 * - Rate limiting (optional)
 * 
 * Usage:
 * <pre>
 * VertxSecurityConfig.builder()
 *     .bodyLimit(10 * 1024 * 1024)  // 10MB
 *     .publicPaths("/api/auth/**", "/api/public/**", "/health")
 *     .build()
 *     .apply(router);
 * </pre>
 */
@Slf4j
@Getter
@Builder
public class VertxSecurityConfig {
    
    // Default body limit: 10MB
    private static final long DEFAULT_BODY_LIMIT = 10 * 1024 * 1024L;
    
    // Default upload limit: 50MB
    private static final long DEFAULT_UPLOAD_LIMIT = 50 * 1024 * 1024L;
    
    @Builder.Default
    private long bodyLimit = DEFAULT_BODY_LIMIT;
    
    @Builder.Default
    private long uploadLimit = DEFAULT_UPLOAD_LIMIT;
    
    @Builder.Default
    private boolean deleteUploadedFilesOnEnd = true;
    
    @Builder.Default
    private String uploadsDirectory = "file-uploads";
    
    @Builder.Default
    private List<String> publicPaths = new ArrayList<>();
    
    @Builder.Default
    private List<String> uploadPaths = new ArrayList<>();  // Paths cho phép upload lớn
    
    /**
     * Apply security configuration to router
     */
    public void apply(Router router) {
        // 1. Apply BodyHandler với limits cho các routes thông thường
        BodyHandler standardHandler = BodyHandler.create()
                .setBodyLimit(bodyLimit)
                .setDeleteUploadedFilesOnEnd(deleteUploadedFilesOnEnd);
        
        // 2. Apply BodyHandler với upload limits cho upload routes
        BodyHandler uploadHandler = BodyHandler.create(uploadsDirectory)
                .setBodyLimit(uploadLimit)
                .setDeleteUploadedFilesOnEnd(deleteUploadedFilesOnEnd);
        
        // Apply upload handler cho upload paths trước
        for (String path : uploadPaths) {
            router.route(path).handler(uploadHandler);
            log.debug("Applied upload handler (limit: {}MB) to: {}", uploadLimit / (1024 * 1024), path);
        }
        
        // Apply standard handler cho tất cả routes còn lại
        router.route().handler(standardHandler);
        
        log.info("VertxSecurityConfig applied: bodyLimit={}KB, uploadLimit={}MB, publicPaths={}", 
                bodyLimit / 1024, uploadLimit / (1024 * 1024), publicPaths.size());
    }
    
    /**
     * Check if path is public (không cần auth)
     */
    public boolean isPublicPath(String path) {
        for (String publicPath : publicPaths) {
            if (matchPath(publicPath, path)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Simple path matching với wildcard support
     */
    private boolean matchPath(String pattern, String path) {
        if (pattern.equals(path)) {
            return true;
        }
        
        // Handle ** wildcard (matches any depth)
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.startsWith(prefix);
        }
        
        // Handle * wildcard (matches single segment)
        if (pattern.contains("*")) {
            String regex = pattern
                    .replace(".", "\\.")
                    .replace("*", "[^/]*");
            return path.matches(regex);
        }
        
        return false;
    }
    
    /**
     * Builder convenience methods
     * Lombok sẽ generate builder, ta chỉ thêm helper methods
     */
    public static class VertxSecurityConfigBuilder {
        
        public VertxSecurityConfigBuilder publicPaths(String... paths) {
            this.publicPaths$value = new ArrayList<>(List.of(paths));
            this.publicPaths$set = true;
            return this;
        }
        
        public VertxSecurityConfigBuilder uploadPaths(String... paths) {
            this.uploadPaths$value = new ArrayList<>(List.of(paths));
            this.uploadPaths$set = true;
            return this;
        }
        
        public VertxSecurityConfigBuilder bodyLimitMB(int mb) {
            this.bodyLimit$value = mb * 1024 * 1024L;
            this.bodyLimit$set = true;
            return this;
        }
        
        public VertxSecurityConfigBuilder uploadLimitMB(int mb) {
            this.uploadLimit$value = mb * 1024 * 1024L;
            this.uploadLimit$set = true;
            return this;
        }
    }
}
