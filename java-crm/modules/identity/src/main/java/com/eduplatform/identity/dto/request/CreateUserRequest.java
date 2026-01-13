package com.eduplatform.identity.dto.request;

import java.util.UUID;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String email;
    private String password;
    private String name;
    private String status;
    private java.util.List<UUID> roleIds;

}
