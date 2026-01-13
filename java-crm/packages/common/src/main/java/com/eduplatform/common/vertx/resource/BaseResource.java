package com.eduplatform.common.vertx.resource;

import com.eduplatform.common.constant.ErrorCode;
import com.eduplatform.common.exception.AppException;
import com.eduplatform.common.response.ApiResponse;
import com.eduplatform.common.vertx.model.Page;
import com.eduplatform.common.vertx.model.Pageable;
import com.eduplatform.common.vertx.model.VertxPrincipal;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * BaseResource - Base class cho tất cả @VertxRestController
 * 
 * <h2>Cách dùng:</h2>
 * <pre>
 * @VertxRestController
 * public class CategoryResource extends BaseResource {
 *     
 *     private final CategoryService service;
 *     
 *     // ========== READ (không cần principal) ==========
 *     
 *     // Danh sách có phân trang
 *     @VertxGet("/api/v1/categories")
 *     @RequirePermission(resource = "CATEGORY", action = Action.VIEW)
 *     public Single&lt;Response&lt;Page&lt;Category&gt;&gt;&gt; list(Pageable pageable) {
 *         return page(() -> service.list(pageable), 
 *                     () -> service.count(), 
 *                     pageable);
 *     }
 *     
 *     // Chi tiết
 *     @VertxGet("/api/v1/categories/:id")
 *     @RequirePermission(resource = "CATEGORY", action = Action.VIEW)
 *     public Single&lt;Response&lt;Category&gt;&gt; getById(@VertxPathVariable("id") UUID id) {
 *         return query(() -> service.getById(id));
 *     }
 *     
 *     // ========== WRITE (cần principal để audit) ==========
 *     
 *     @VertxPost("/api/v1/categories")
 *     @RequirePermission(resource = "CATEGORY", action = Action.ADD)
 *     public Single&lt;Response&lt;Category&gt;&gt; create(
 *             VertxPrincipal principal,
 *             @VertxRequestBody CreateCategoryRequest request
 *     ) {
 *         return execute(principal, user -> service.create(request, user));
 *     }
 *     
 *     @VertxPut("/api/v1/categories/:id")
 *     @RequirePermission(resource = "CATEGORY", action = Action.UPDATE)
 *     public Single&lt;Response&lt;Category&gt;&gt; update(
 *             VertxPrincipal principal,
 *             @VertxPathVariable("id") UUID id,
 *             @VertxRequestBody UpdateCategoryRequest request
 *     ) {
 *         return execute(principal, user -> service.update(id, request, user));
 *     }
 *     
 *     @VertxDelete("/api/v1/categories/:id")
 *     @RequirePermission(resource = "CATEGORY", action = Action.DELETE)
 *     public Single&lt;Response&lt;Boolean&gt;&gt; delete(
 *             VertxPrincipal principal,
 *             @VertxPathVariable("id") UUID id
 *     ) {
 *         return execute(principal, user -> service.delete(id, user));
 *     }
 * }
 * </pre>
 */
@Slf4j
public abstract class BaseResource {

    // ============================================
    // READ Operations (không cần user)
    // ============================================
    
    /**
     * Query đơn giản - dùng cho GET by id, GET list không phân trang
     * <pre>
     * return query(() -> service.getById(id));
     * return query(() -> service.getAll());
     * </pre>
     */
    protected <T> Single<ResponseEntity<ApiResponse<T>>> query(Callable<T> dbCall) {
        return Single.fromCallable(dbCall)
                .subscribeOn(Schedulers.io())
                .map(this::ok);
    }
    
    /**
     * Query có phân trang
     * <pre>
     * return page(() -> service.list(pageable), () -> service.count(), pageable);
     * </pre>
     */
    protected <T> Single<ResponseEntity<ApiResponse<Page<T>>>> page(
            Callable<List<T>> listCall,
            Callable<Long> countCall,
            Pageable pageable
    ) {
        return Single.fromCallable(() -> {
                    List<T> items = listCall.call();
                    long total = countCall.call();
                    pageable.setTotal(total);
                    return new Page<>(pageable, items);
                })
                .subscribeOn(Schedulers.io())
                .map(this::ok);
    }

    // ============================================
    // WRITE Operations (cần user để audit)
    // ============================================
    
    /**
     * Execute với user - dùng cho CREATE, UPDATE, DELETE
     * <pre>
     * return execute(principal, (SecurityUser user) -> service.create(request, user));
     * </pre>
     */
    @SuppressWarnings("unchecked")
    protected <T, U> Single<ResponseEntity<ApiResponse<T>>> execute(
            VertxPrincipal principal,
            Function<U, T> action
    ) {
        return getUser(principal)
                .flatMap(user -> Single.fromCallable(() -> action.apply((U) user))
                        .subscribeOn(Schedulers.io()))
                .map(this::ok);
    }
    
    /**
     * Execute không cần return value
     * <pre>
     * return run(principal, (SecurityUser user) -> service.sendEmail(user));
     * </pre>
     */
    @SuppressWarnings("unchecked")
    protected <U> Single<ResponseEntity<ApiResponse<Boolean>>> run(
            VertxPrincipal principal,
            java.util.function.Consumer<U> action
    ) {
        return getUser(principal)
                .flatMap(user -> Single.fromCallable(() -> {
                    action.accept((U) user);
                    return true;
                }).subscribeOn(Schedulers.io()))
                .map(this::ok);
    }

    // ============================================
    // User Access
    // ============================================
    
    /**
     * Lấy user từ principal
     */
    @SuppressWarnings("unchecked")
    protected <T> Single<T> getUser(VertxPrincipal principal) {
        if (principal == null || principal.getUserId() == null) {
            return Single.error(new AppException(ErrorCode.UNAUTHORIZED, "Chưa đăng nhập"));
        }
        
        T user = (T) principal.getSecurityUser();
        if (user == null) {
            return Single.error(new AppException(ErrorCode.UNAUTHORIZED, "User not found"));
        }
        
        return Single.just(user);
    }
    
    /**
     * Lấy user ID
     */
    protected Single<UUID> getUserId(VertxPrincipal principal) {
        if (principal == null || principal.getUserId() == null) {
            return Single.error(new AppException(ErrorCode.UNAUTHORIZED, "Chưa đăng nhập"));
        }
        return Single.just(principal.getUserId());
    }

    // ============================================
    // Response Helpers
    // ============================================
    
    /**
     * Wrap thành ResponseEntity + ApiResponse
     */
    protected <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }
    
    /**
     * Error response
     */
    protected <T> Single<T> error(String message) {
        return Single.error(new AppException(ErrorCode.BAD_REQUEST, message));
    }
    
    protected <T> Single<T> error(ErrorCode code, String message) {
        return Single.error(new AppException(code, message));
    }
    
    protected <T> Single<T> notFound(String message) {
        return Single.error(new AppException(ErrorCode.NOT_FOUND, message));
    }
}