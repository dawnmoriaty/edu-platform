package com.eduplatform.identity.record;

import java.util.UUID;

public record PermissionInfo(
            UUID id,
            String name,
            String resource,
            String action,
            String description
    ) {}
