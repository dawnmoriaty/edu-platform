package com.eduplatform.common.vertx.model;

import java.util.HashSet;
import java.util.Set;
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
    
    // Permissions: format "RESOURCE:ACTION" như "CONTACT:VIEW", "USER:UPDATE"
    private Set<String> permissions = new HashSet<>();
    
    // Roles
    private Set<String> roles = new HashSet<>();

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
    
    public Set<String> getPermissions() {
        return permissions;
    }
    
    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions != null ? permissions : new HashSet<>();
    }
    
    public void addPermission(String permission) {
        this.permissions.add(permission);
    }
    
    public boolean hasPermission(String permission) {
        return this.permissions.contains(permission) || this.permissions.contains("*:*");
    }
    
    public Set<String> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<String> roles) {
        this.roles = roles != null ? roles : new HashSet<>();
    }
    
    public void addRole(String role) {
        this.roles.add(role);
    }
    
    public boolean hasRole(String role) {
        return this.roles.contains(role);
    }
}
