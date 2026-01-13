package com.eduplatform.identity.dto.request;

import java.util.UUID;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String email;
    private String name;
    private String status;
    private java.util.List<UUID> roleIds;
}
