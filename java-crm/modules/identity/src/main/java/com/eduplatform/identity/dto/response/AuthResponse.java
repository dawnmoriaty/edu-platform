package com.eduplatform.identity.dto.response;

import com.eduplatform.auth.rbac.model.SecurityUser;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private SecurityUser user;
}
