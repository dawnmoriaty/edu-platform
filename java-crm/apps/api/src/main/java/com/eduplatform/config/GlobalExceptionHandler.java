package com.eduplatform.config;

import com.eduplatform.common.constant.ErrorCode;
import com.eduplatform.common.exception.AppException;
import com.eduplatform.common.response.ApiResponse;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * GlobalExceptionHandler - Vert.x Failure Handler
 */
@Slf4j
@Component
public class GlobalExceptionHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext context) {
        Throwable failure = context.failure();

        if (failure instanceof AppException e) {
            log.error("AppException: {} - {}", e.getErrorCode(), e.getMessage());
            sendError(context, e.getHttpStatus(), e.getCode(), e.getErrorMessage());
            return;
        }

        if (failure instanceof IllegalArgumentException e) {
            log.warn("Validation error: {}", e.getMessage());
            sendError(context, 400, ErrorCode.BAD_REQUEST.getCode(), e.getMessage());
            return;
        }

        // Unknown error
        log.error("Unhandled exception", failure);
        sendError(context, 500, ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "Internal server error");
    }

    private void sendError(RoutingContext context, int httpStatus, int code, String message) {
        ApiResponse<?> response = ApiResponse.error(code, message);
        context.response()
                .setStatusCode(httpStatus)
                .putHeader("Content-Type", "application/json")
                .end(Json.encode(response));
    }
}
