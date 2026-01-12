package com.eduplatform.auth.rbac.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.Principal;
import java.util.UUID;

/**
 * VertxPrincipal - Principal cho Vert.x context
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VertxPrincipal implements Principal {
    private UUID userId;
    private String username;
    private String token;
    private SecurityUser securityUser;

    @Override
    public String getName() {
        return username;
    }
}
