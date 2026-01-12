package com.eduplatform.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * ErrorCode - Mã lỗi chuẩn hóa cho toàn bộ hệ thống
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 2xx - Success
    SUCCESS(200, "Success", 200),
    
    // 4xx - Client errors
    BAD_REQUEST(4001, "Bad request", 400),
    VALIDATION_ERROR(4002, "Validation error", 400),
    DUPLICATE_ENTRY(4003, "Duplicate entry", 400),
    
    // Auth errors
    UNAUTHORIZED(1001, "Unauthorized", 401),
    TOKEN_EXPIRED(1002, "Token expired", 401),
    TOKEN_INVALID(1003, "Invalid token", 401),
    INVALID_CREDENTIALS(1004, "Invalid credentials", 401),
    
    // Authorization errors
    FORBIDDEN(2001, "Forbidden", 403),
    PERMISSION_DENIED(2002, "Permission denied", 403),
    USER_DISABLED(2003, "User is disabled", 403),
    ENTERPRISE_NOT_VERIFIED(2004, "Enterprise not verified", 403),
    
    // Resource errors
    NOT_FOUND(3001, "Not found", 404),
    USER_NOT_FOUND(3002, "User not found", 404),
    ROLE_NOT_FOUND(3003, "Role not found", 404),
    
    // Conflict errors
    CONFLICT(4009, "Conflict", 409),
    DUPLICATE_EMAIL(4010, "Email already exists", 409),
    DUPLICATE_USERNAME(4011, "Username already exists", 409),
    
    // Server errors
    INTERNAL_ERROR(5001, "Internal server error", 500),
    INTERNAL_SERVER_ERROR(5001, "Internal server error", 500),
    DATABASE_ERROR(5002, "Database error", 500),
    SERVICE_UNAVAILABLE(5003, "Service unavailable", 503);

    private final int code;
    private final String message;
    private final int httpStatus;
}
