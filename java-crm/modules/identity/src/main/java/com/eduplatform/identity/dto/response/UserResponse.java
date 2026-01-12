package com.eduplatform.identity.dto.response;

import com.eduplatform.entity.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Integer id;
    private String username;
    private String email;
    private String name;
    private String avatar;
    private UserStatus status;
    private String role;
}
