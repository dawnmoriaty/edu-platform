package com.eduplatform.common.vertx.model;

import java.util.UUID;

/**
 * Principal model cho Vert.x security
 * Lưu thông tin user đã xác thực
 */
public class VertxPrincipal {
    
    private UUID userId;
    private String username;
    private String email;
    private Object securityUser;
    
    // Các thông tin bổ sung từ JWT hoặc context
    private String clientId;
    private String remoteHost;

    public VertxPrincipal() {}

    public VertxPrincipal(UUID userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @SuppressWarnings("unchecked")
    public <T> T getSecurityUser() {
        return (T) securityUser;
    }

    public void setSecurityUser(Object securityUser) {
        this.securityUser = securityUser;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }
}
