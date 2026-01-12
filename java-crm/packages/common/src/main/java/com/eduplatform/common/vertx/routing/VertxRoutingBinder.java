package com.eduplatform.common.vertx.routing;

import com.eduplatform.common.constant.Action;
import com.eduplatform.common.constant.ErrorCode;
import com.eduplatform.common.exception.AppException;
import com.eduplatform.common.response.ApiResponse;
import com.eduplatform.common.vertx.VertxWrapper;
import com.eduplatform.common.vertx.annotation.*;
import com.eduplatform.common.vertx.binder.VertxRouterBinder;
import com.eduplatform.common.vertx.execution.VertxExecution;
import com.eduplatform.common.vertx.execution.WorkerPoolManager;
import com.eduplatform.common.vertx.model.Pageable;
import com.eduplatform.common.vertx.model.VertxPrincipal;
import com.eduplatform.common.vertx.security.VertxSecurityConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * VertxRoutingBinder - Tự động scan @VertxRestController và bind routes
 * 
 * Features:
 * - Scan tất cả bean có @VertxRestController annotation
 * - Cache metadata lúc khởi động (không dùng reflection mỗi request)
 * - Tự động detect reactive vs blocking return types
 * - Tự động wrap blocking code vào VertxExecution.blocking()
 * - Support @RequirePermission với wildcard
 */
@Slf4j
@Component
public class VertxRoutingBinder {

    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;
    
    // Metadata cache - populated at startup
    private final Map<String, RouteMetadata> routeCache = new ConcurrentHashMap<>();
    
    @Value("${app.base-package:com.eduplatform}")
    private String basePackage;
    
    @Autowired(required = false)
    private VertxSecurityConfig securityConfig;
    
    @Autowired(required = false)
    private WorkerPoolManager workerPoolManager;

    @Autowired
    public VertxRoutingBinder(ApplicationContext applicationContext,
                               @Autowired(required = false) ObjectMapper objectMapper) {
        this.applicationContext = applicationContext;
        // Nếu không có ObjectMapper bean, tự tạo
        this.objectMapper = objectMapper != null ? objectMapper : createDefaultObjectMapper();
    }

    private ObjectMapper createDefaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules(); // Auto-register JavaTimeModule nếu có
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    /**
     * Bind tất cả @VertxRestController vào router
     */
    public void bind(Router router, Vertx vertx) {
        // Init VertxWrapper for static access
        VertxWrapper.init(vertx);
        
        // Init WorkerPoolManager nếu có
        if (workerPoolManager != null) {
            workerPoolManager.initPools(vertx);
        }
        
        // Apply security config (BodyHandler với limits)
        if (securityConfig != null) {
            securityConfig.apply(router);
        } else {
            // Default security config
            VertxSecurityConfig.builder()
                    .bodyLimitMB(10)
                    .build()
                    .apply(router);
        }

        // Bind @VertxBeforeHandler beans first (sorted by order)
        bindBeforeHandlers(router);

        // Scan và cache metadata cho tất cả controllers
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(VertxRestController.class);
        
        int routeCount = 0;
        for (Object controller : controllers.values()) {
            routeCount += bindController(router, controller);
        }
        
        log.info("Bound {} routes from {} controllers (metadata cached)", routeCount, controllers.size());
    }
    
    /**
     * Bind all @VertxBeforeHandler beans (sorted by order)
     */
    private void bindBeforeHandlers(Router router) {
        Map<String, Object> handlers = applicationContext.getBeansWithAnnotation(VertxBeforeHandler.class);
        
        if (handlers.isEmpty()) return;
        
        // Sort by order annotation
        List<Object> sortedHandlers = handlers.values().stream()
                .filter(h -> h instanceof VertxRouterBinder)
                .sorted(Comparator.comparingInt(h -> {
                    VertxBeforeHandler ann = h.getClass().getAnnotation(VertxBeforeHandler.class);
                    return ann != null ? ann.order() : 100;
                }))
                .toList();
        
        for (Object handler : sortedHandlers) {
            ((VertxRouterBinder) handler).bind(router);
        }
        
        log.info("Bound {} before handlers", sortedHandlers.size());
    }

    private int bindController(Router router, Object controller) {
        Class<?> controllerClass = controller.getClass();
        
        // Get base path from @VertxRestController
        VertxRestController annotation = controllerClass.getAnnotation(VertxRestController.class);
        String basePath = annotation != null ? annotation.value() : "";
        
        int routeCount = 0;

        // Scan methods và build metadata
        for (Method method : controllerClass.getMethods()) {
            RouteMetadata metadata = RouteMetadata.from(controller, method, basePath);
            
            if (metadata == null) {
                continue;
            }
            
            // Cache metadata với key = "METHOD:path"
            String cacheKey = metadata.getHttpMethod() + ":" + metadata.getPath();
            routeCache.put(cacheKey, metadata);

            // Bind route với cached metadata
            router.route(metadata.getHttpMethod(), metadata.getPath())
                    .handler(ctx -> handleRequest(ctx, metadata));
            
            routeCount++;
        }
        
        return routeCount;
    }

    private void handleRequest(RoutingContext ctx, RouteMetadata metadata) {
        try {
            // Check @RequirePermission if present (using cached metadata)
            if (metadata.hasPermissionRequired()) {
                VertxPrincipal principal = ctx.get("principal");
                if (principal == null) {
                    handleError(ctx, new AppException(ErrorCode.UNAUTHORIZED, "No principal found"));
                    return;
                }
                
                Set<String> userPermissions = principal.getPermissions();
                
                if (userPermissions == null || !hasPermission(userPermissions, metadata.getResource(), metadata.getAction())) {
                    handleError(ctx, new AppException(ErrorCode.PERMISSION_DENIED, 
                            "Missing permission: " + metadata.getRequiredPermission()));
                    return;
                }
                
                // Store permission info in context for dataScope filtering
                if (metadata.isRequireDataScope()) {
                    ctx.put("dataScope", true);
                    ctx.put("dataScopeResource", metadata.getResource());
                }
            }
            
            // Extract parameters using cached metadata
            Object[] args = extractParameters(ctx, metadata);
            
            // Invoke method với auto-blocking detection
            invokeMethod(ctx, metadata, args);
            
        } catch (Exception e) {
            log.error("Error handling request: {}", e.getMessage(), e);
            handleError(ctx, e);
        }
    }
    
    /**
     * Invoke method với auto-detect blocking/reactive
     */
    private void invokeMethod(RoutingContext ctx, RouteMetadata metadata, Object[] args) {
        try {
            if (metadata.isReactive()) {
                // Reactive return type - chạy trực tiếp trên event loop
                Object result = metadata.getMethod().invoke(metadata.getController(), args);
                handleResult(ctx, result);
            } else {
                // Non-reactive return type - tự động wrap vào blocking
                VertxExecution.blocking(() -> {
                    try {
                        return metadata.getMethod().invoke(metadata.getController(), args);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).subscribe(
                        result -> handleResult(ctx, result),
                        error -> handleError(ctx, error)
                );
            }
        } catch (Exception e) {
            handleError(ctx, e);
        }
    }
    
    /**
     * Check if user has the required permission
     * Supports wildcard permissions like "CONTACT:*" or "*:VIEW"
     */
    private boolean hasPermission(Set<String> userPermissions, String resource, Action action) {
        String requiredPermission = resource + ":" + action.getCode();
        
        // Direct match
        if (userPermissions.contains(requiredPermission)) {
            return true;
        }
        
        // Wildcard on action: "CONTACT:*"
        if (userPermissions.contains(resource + ":*")) {
            return true;
        }
        
        // Wildcard on resource: "*:VIEW"
        if (userPermissions.contains("*:" + action.getCode())) {
            return true;
        }
        
        // Super admin wildcard: "*:*"
        if (userPermissions.contains("*:*")) {
            return true;
        }
        
        return false;
    }

    /**
     * Extract parameters using cached metadata (không dùng reflection mỗi request)
     */
    private Object[] extractParameters(RoutingContext ctx, RouteMetadata metadata) {
        List<ParameterMetadata> params = metadata.getParameters();
        Object[] args = new Object[params.size()];

        for (int i = 0; i < params.size(); i++) {
            args[i] = extractParameter(ctx, params.get(i));
        }

        return args;
    }

    private Object extractParameter(RoutingContext ctx, ParameterMetadata param) {
        return switch (param.getParameterType()) {
            case ROUTING_CONTEXT -> ctx;
            
            case PRINCIPAL -> ctx.get("principal");
            
            case PAGEABLE -> {
                Pageable pageable = new Pageable();
                String page = ctx.request().getParam("page");
                String size = ctx.request().getParam("size");
                String sort = ctx.request().getParam("sort");
                String order = ctx.request().getParam("order");
                
                if (page != null) pageable.setPage(Integer.parseInt(page));
                if (size != null) pageable.setSize(Integer.parseInt(size));
                if (sort != null) pageable.setSort(sort);
                if (order != null) pageable.setOrder(order);
                
                yield pageable;
            }
            
            case REQUEST_BODY -> {
                try {
                    JsonObject body = ctx.body().asJsonObject();
                    if (body == null && param.isBodyRequired()) {
                        throw new AppException(ErrorCode.BAD_REQUEST, "Request body is required");
                    }
                    if (body == null) {
                        yield null;
                    }
                    yield objectMapper.readValue(body.encode(), param.getType());
                } catch (AppException e) {
                    throw e;
                } catch (Exception e) {
                    throw new AppException(ErrorCode.BAD_REQUEST, "Failed to parse request body: " + e.getMessage());
                }
            }
            
            case REQUEST_PARAM -> {
                String value = ctx.request().getParam(param.getParamName());
                if (value == null || value.isEmpty()) {
                    value = param.getDefaultValue();
                }
                if ((value == null || value.isEmpty()) && param.isParamRequired()) {
                    throw new AppException(ErrorCode.BAD_REQUEST, 
                            "Request param " + param.getParamName() + " is required");
                }
                yield ParameterMetadata.convertValue(value, param.getType());
            }
            
            case PATH_VARIABLE -> {
                String value = ctx.pathParam(param.getPathVarName());
                yield ParameterMetadata.convertValue(value, param.getType());
            }
            
            case QUERY_PARAM -> {
                String value = ctx.request().getParam(param.getParamName());
                yield ParameterMetadata.convertValue(value, param.getType());
            }
        };
    }

    @SuppressWarnings("unchecked")
    private void handleResult(RoutingContext ctx, Object result) {
        if (result == null) {
            ctx.response()
                    .setStatusCode(204)
                    .end();
            return;
        }

        // Single<...> - RxJava reactive
        if (result instanceof Single<?> single) {
            single.subscribe(
                    res -> handleResult(ctx, res),
                    error -> handleError(ctx, (Throwable) error)
            );
            return;
        }
        
        // Maybe<...> - RxJava reactive (nullable)
        if (result instanceof Maybe<?> maybe) {
            maybe.subscribe(
                    res -> handleResult(ctx, res),
                    error -> handleError(ctx, (Throwable) error),
                    () -> ctx.response().setStatusCode(204).end()  // Empty = 204
            );
            return;
        }
        
        // Completable - RxJava reactive (void)
        if (result instanceof Completable completable) {
            completable.subscribe(
                    () -> ctx.response().setStatusCode(204).end(),
                    error -> handleError(ctx, (Throwable) error)
            );
            return;
        }

        // ResponseEntity
        if (result instanceof ResponseEntity<?> responseEntity) {
            Object body = responseEntity.getBody();
            
            ctx.response()
                    .setStatusCode(responseEntity.getStatusCode().value())
                    .putHeader("Content-Type", "application/json");
            
            if (body != null) {
                try {
                    ctx.response().end(objectMapper.writeValueAsString(body));
                } catch (Exception e) {
                    handleError(ctx, e);
                }
            } else {
                ctx.response().end();
            }
            return;
        }

        // Plain object - wrap in ApiResponse
        try {
            ApiResponse<?> response = ApiResponse.success(result);
            ctx.response()
                    .setStatusCode(200)
                    .putHeader("Content-Type", "application/json")
                    .end(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            handleError(ctx, e);
        }
    }

    private void handleError(RoutingContext ctx, Throwable error) {
        // Unwrap InvocationTargetException
        Throwable cause = error;
        while (cause.getCause() != null && 
               cause instanceof java.lang.reflect.InvocationTargetException) {
            cause = cause.getCause();
        }
        
        log.error("Request error: {}", cause.getMessage());

        int statusCode;
        int errorCode;
        String message = cause.getMessage();

        // Extract info using instanceof (không dùng string matching)
        if (cause instanceof AppException appEx) {
            statusCode = appEx.getHttpStatus();
            errorCode = appEx.getCode();
            message = appEx.getErrorMessage();
        } else if (cause instanceof IllegalArgumentException) {
            statusCode = 400;
            errorCode = ErrorCode.BAD_REQUEST.getCode();
        } else if (cause instanceof SecurityException) {
            statusCode = 403;
            errorCode = ErrorCode.FORBIDDEN.getCode();
        } else if (cause instanceof NullPointerException) {
            statusCode = 500;
            errorCode = ErrorCode.INTERNAL_ERROR.getCode();
            message = "Internal error: null reference";
        } else {
            statusCode = 500;
            errorCode = ErrorCode.INTERNAL_ERROR.getCode();
        }

        ApiResponse<?> response = ApiResponse.error(errorCode, message);
        
        try {
            ctx.response()
                    .setStatusCode(statusCode)
                    .putHeader("Content-Type", "application/json")
                    .end(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            ctx.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "application/json")
                    .end("{\"code\":5001,\"message\":\"Internal Server Error\"}");
        }
    }
}
