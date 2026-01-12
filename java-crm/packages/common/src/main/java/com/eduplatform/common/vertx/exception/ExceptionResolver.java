package com.eduplatform.common.vertx.exception;

import com.eduplatform.common.constant.ErrorCode;
import com.eduplatform.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ExceptionResolver - Xử lý exception thống nhất cho Vert.x
 * 
 * Có thể extend và override để custom exception handling
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
        log.error("Request error: {} - {}", ctx.request().uri(), throwable.getMessage());
        
        if (log.isDebugEnabled()) {
            log.debug("Stack trace:", throwable);
        }

        int statusCode = extractStatusCode(throwable);
        String message = throwable.getMessage();

        sendErrorResponse(ctx, statusCode, message);
    }

    protected int extractStatusCode(Throwable throwable) {
        String msg = throwable.getMessage();
        if (msg == null) return 500;
        
        if (msg.contains("Unauthorized") || msg.contains("Chưa đăng nhập")) {
            return 401;
        }
        if (msg.contains("Forbidden") || msg.contains("Missing permission")) {
            return 403;
        }
        if (msg.contains("not found") || msg.contains("Not Found")) {
            return 404;
        }
        if (msg.contains("Bad Request") || msg.contains("Invalid")) {
            return 400;
        }
        
        return 500;
    }

    protected void sendErrorResponse(RoutingContext ctx, int statusCode, String message) {
        ApiResponse<?> response = ApiResponse.error(statusCode, message);
        
        try {
            String json = objectMapper != null 
                ? objectMapper.writeValueAsString(response)
                : String.format("{\"code\":%d,\"message\":\"%s\"}", statusCode, message);
            
            ctx.response()
                .setStatusCode(statusCode)
                .putHeader("Content-Type", "application/json")
                .end(json);
        } catch (Exception e) {
            ctx.response()
                .setStatusCode(500)
                .putHeader("Content-Type", "application/json")
                .end("{\"code\":500,\"message\":\"Internal Server Error\"}");
        }
    }
}
