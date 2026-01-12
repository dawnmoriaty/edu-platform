package com.eduplatform.common.vertx.exception;

import com.eduplatform.common.constant.ErrorCode;
import com.eduplatform.common.exception.AppException;
import com.eduplatform.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ExceptionResolver - Xử lý exception thống nhất cho Vert.x
 * 
 * Sử dụng instanceof để xác định loại exception thay vì string matching.
 * Tận dụng AppException và ErrorCode để ánh xạ trực tiếp sang HTTP status.
 */
@Slf4j
@Component
public class ExceptionResolver {

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    /**
     * Resolve exception và gửi response
     */
    public void resolve(RoutingContext ctx, Throwable throwable) {
        // Unwrap nếu là wrapper exception
        Throwable cause = unwrap(throwable);
        
        log.error("Request error: {} - {}", ctx.request().uri(), cause.getMessage());
        
        if (log.isDebugEnabled()) {
            log.debug("Stack trace:", cause);
        }

        ExceptionInfo info = extractExceptionInfo(cause);
        sendErrorResponse(ctx, info.statusCode, info.errorCode, info.message);
    }

    /**
     * Unwrap exception để lấy cause thực sự
     */
    private Throwable unwrap(Throwable throwable) {
        Throwable cause = throwable;
        
        // Unwrap common wrapper exceptions
        while (cause.getCause() != null && 
               (cause instanceof java.lang.reflect.InvocationTargetException ||
                cause.getClass().getSimpleName().equals("RuntimeException") && 
                cause.getMessage() == null)) {
            cause = cause.getCause();
        }
        
        return cause;
    }

    /**
     * Extract exception info using instanceof pattern matching
     */
    protected ExceptionInfo extractExceptionInfo(Throwable throwable) {
        // AppException - ưu tiên cao nhất, có đầy đủ thông tin
        if (throwable instanceof AppException appEx) {
            return new ExceptionInfo(
                    appEx.getHttpStatus(),
                    appEx.getCode(),
                    appEx.getErrorMessage()
            );
        }
        
        // IllegalArgumentException - Bad Request
        if (throwable instanceof IllegalArgumentException) {
            return new ExceptionInfo(400, ErrorCode.BAD_REQUEST.getCode(), throwable.getMessage());
        }
        
        // SecurityException hoặc AccessDeniedException
        if (throwable instanceof SecurityException ||
            throwable.getClass().getSimpleName().contains("AccessDenied")) {
            return new ExceptionInfo(403, ErrorCode.FORBIDDEN.getCode(), throwable.getMessage());
        }
        
        // NullPointerException
        if (throwable instanceof NullPointerException) {
            return new ExceptionInfo(500, ErrorCode.INTERNAL_ERROR.getCode(), 
                    "Null pointer: " + throwable.getMessage());
        }
        
        // NumberFormatException - Bad Request (invalid parameter format)
        if (throwable instanceof NumberFormatException) {
            return new ExceptionInfo(400, ErrorCode.BAD_REQUEST.getCode(), 
                    "Invalid number format: " + throwable.getMessage());
        }
        
        // IllegalStateException
        if (throwable instanceof IllegalStateException) {
            return new ExceptionInfo(409, ErrorCode.CONFLICT.getCode(), throwable.getMessage());
        }
        
        // UnsupportedOperationException
        if (throwable instanceof UnsupportedOperationException) {
            return new ExceptionInfo(501, 5011, "Not implemented: " + throwable.getMessage());
        }
        
        // Default: Internal Server Error
        return new ExceptionInfo(500, ErrorCode.INTERNAL_ERROR.getCode(), 
                throwable.getMessage() != null ? throwable.getMessage() : "Internal server error");
    }

    protected void sendErrorResponse(RoutingContext ctx, int statusCode, int errorCode, String message) {
        ApiResponse<?> response = ApiResponse.error(errorCode, message);
        
        try {
            String json = objectMapper != null 
                ? objectMapper.writeValueAsString(response)
                : String.format("{\"code\":%d,\"message\":\"%s\"}", errorCode, escapeJson(message));
            
            ctx.response()
                .setStatusCode(statusCode)
                .putHeader("Content-Type", "application/json")
                .end(json);
        } catch (Exception e) {
            log.error("Failed to send error response", e);
            ctx.response()
                .setStatusCode(500)
                .putHeader("Content-Type", "application/json")
                .end("{\"code\":5001,\"message\":\"Internal Server Error\"}");
        }
    }
    
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    /**
     * Helper record to hold exception info
     */
    protected record ExceptionInfo(int statusCode, int errorCode, String message) {}
}
