package com.eduplatform.common.vertx.resource;

import com.eduplatform.common.constant.ErrorCode;
import com.eduplatform.common.exception.AppException;
import com.eduplatform.common.response.ApiResponse;
import com.eduplatform.common.vertx.model.VertxPrincipal;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

/**
 * BaseResource - Base class cho tất cả @VertxRestController
 * 
 * Cung cấp:
 * - checkPermission() - kiểm tra quyền
 * - sendSuccess() / sendError() - response helpers
 * - getCustomError() - error response
 * 
 * Usage:
 * <pre>
 * @VertxRestController
 * public class ContactResource extends BaseResource {
 *     private static final String URI = "/contact/";
 *     
 *     @VertxGet("/api/v1/contact/list")
 *     public Single<ResponseEntity<DfResponse<List<Contact>>>> list(
 *             VertxPrincipal principal,
 *             Pageable pageable
 *     ) {
 *         return checkPermission(principal, URI, "VIEW")
 *             .flatMap(user -> contactService.list(user.getBsnId(), pageable))
 *             .map(DfResponse::okEntity);
 *     }
 * }
 * </pre>
 */
@Slf4j
public abstract class BaseResource {

    @Autowired(required = false)
    protected ObjectMapper objectMapper;

    @PostConstruct
    protected void initObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules();
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        }
    }

    // ============================================
    // Permission Check Methods
    // ============================================

    /**
     * Check permission - Override trong subclass nếu cần
     * Default implementation: chỉ check xem có principal không
     */
    protected <T> Single<T> checkPermission(VertxPrincipal principal, String uri, String action) {
        if (principal == null || principal.getUserId() == null) {
            return Single.error(new AppException(ErrorCode.UNAUTHORIZED, "Chưa đăng nhập"));
        }
        
        // Subclass có thể override để check permission thực sự
        @SuppressWarnings("unchecked")
        T user = (T) principal.getSecurityUser();
        if (user == null) {
            return Single.error(new AppException(ErrorCode.UNAUTHORIZED, "User not found"));
        }
        
        return Single.just(user);
    }

    /**
     * Convert URI to resource code
     * "/contact/" -> "contact"
     * "/api/v1/users/" -> "users"
     */
    protected String uriToResource(String uri) {
        if (uri == null || uri.isEmpty()) {
            return "";
        }
        
        String cleaned = uri.replaceAll("^/+|/+$", "");
        cleaned = cleaned.replaceFirst("^api/v\\d+/", "");
        cleaned = cleaned.replace("/", "_");
        
        return cleaned.toLowerCase();
    }

    // ============================================
    // Response Helpers
    // ============================================

    /**
     * Wrap result in ApiResponse.success()
     */
    protected <T> ResponseEntity<ApiResponse<T>> okEntity(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * Get custom error as Single.error()
     */
    protected <T> Single<T> getCustomError(String message) {
        return Single.error(new AppException(ErrorCode.BAD_REQUEST, message));
    }

    /**
     * Get custom error with code
     */
    protected <T> Single<T> getCustomError(ErrorCode errorCode, String message) {
        return Single.error(new AppException(errorCode, message));
    }

    /**
     * Send success response (for cases where you need manual control)
     */
    protected void sendSuccess(RoutingContext ctx, Object data) {
        try {
            ctx.response()
                    .setStatusCode(200)
                    .putHeader("Content-Type", "application/json")
                    .end(objectMapper.writeValueAsString(ApiResponse.success(data)));
        } catch (Exception e) {
            ctx.response()
                    .setStatusCode(200)
                    .putHeader("Content-Type", "application/json")
                    .end(Json.encode(ApiResponse.success(data)));
        }
    }

    /**
     * Send error response
     */
    protected void sendError(RoutingContext ctx, Throwable error) {
        int statusCode = 500;
        int code = ErrorCode.INTERNAL_ERROR.getCode();
        String message = error.getMessage();

        if (error instanceof AppException appEx) {
            statusCode = appEx.getErrorCode().getHttpStatus();
            code = appEx.getErrorCode().getCode();
            message = appEx.getMessage();
        }

        log.error("API Error: {}", message, error);

        ctx.response()
                .setStatusCode(statusCode)
                .putHeader("Content-Type", "application/json")
                .end(String.format("""
                        {"code":%d,"message":"%s","timestamp":"%s"}""",
                        code, message, java.time.Instant.now().toString()));
    }

    /**
     * Send error với status code
     */
    protected void sendError(RoutingContext ctx, int statusCode, String message) {
        ctx.response()
                .setStatusCode(statusCode)
                .putHeader("Content-Type", "application/json")
                .end(String.format("""
                        {"code":%d,"message":"%s","timestamp":"%s"}""",
                        statusCode, message, java.time.Instant.now().toString()));
    }

    // ============================================
    // Utility Methods
    // ============================================

    /**
     * Get principal from RoutingContext
     */
    protected VertxPrincipal getPrincipal(RoutingContext ctx) {
        return ctx.get("principal");
    }

    /**
     * Validate object - Override để custom validation
     */
    protected <T> Single<T> validate(T obj) {
        // Default: no validation
        return Single.just(obj);
    }
}
