package com.eduplatform.common.vertx.routing;

import com.eduplatform.common.constant.Action;
import com.eduplatform.common.response.ApiResponse;
import com.eduplatform.common.vertx.VertxWrapper;
import com.eduplatform.common.vertx.annotation.*;
import com.eduplatform.common.vertx.binder.VertxRouterBinder;
import com.eduplatform.common.vertx.model.Pageable;
import com.eduplatform.common.vertx.model.VertxPrincipal;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * VertxRoutingBinder - Tự động scan @VertxRestController và bind routes
 * 
 * Scan tất cả bean có @VertxRestController annotation,
 * tìm các method có @VertxGet, @VertxPost, @VertxPut, @VertxDelete
 * và tự động bind vào Vert.x Router
 */
@Slf4j
@Component
public class VertxRoutingBinder {

    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;
    
    @Value("${app.base-package:com.eduplatform}")
    private String basePackage;

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
        
        // Add body handler for all routes
        router.route().handler(BodyHandler.create());
        
        // Bind @VertxBeforeHandler beans first (sorted by order)
        bindBeforeHandlers(router);

        // Scan tất cả bean có @VertxRestController
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(VertxRestController.class);
        
        int routeCount = 0;
        for (Object controller : controllers.values()) {
            routeCount += bindController(router, controller);
        }
        
        log.info("Bound {} routes from {} controllers", routeCount, controllers.size());
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

        // Scan methods
        for (Method method : controllerClass.getMethods()) {
            VertxRequestMapping requestMapping = AnnotatedElementUtils.getMergedAnnotation(method, VertxRequestMapping.class);
            
            if (requestMapping == null) {
                continue;
            }

            String path = basePath + requestMapping.path();
            HttpMethod httpMethod = requestMapping.method().getVertxMethod();

            router.route(httpMethod, path).handler(ctx -> {
                handleRequest(ctx, controller, method);
            });
            
            routeCount++;
        }
        
        return routeCount;
    }

    private void handleRequest(RoutingContext ctx, Object controller, Method method) {
        try {
            // Check @RequirePermission if present
            RequirePermission requirePermission = method.getAnnotation(RequirePermission.class);
            if (requirePermission != null) {
                VertxPrincipal principal = ctx.get("principal");
                if (principal == null) {
                    handleError(ctx, new RuntimeException("Unauthorized: No principal found"));
                    return;
                }
                
                String resource = requirePermission.resource();
                Action action = requirePermission.action();
                
                // Check permission from principal's permissions set
                String requiredPermission = resource + ":" + action.getCode();
                Set<String> userPermissions = principal.getPermissions();
                
                if (userPermissions == null || !hasPermission(userPermissions, resource, action)) {
                    handleError(ctx, new RuntimeException("Forbidden: Missing permission " + requiredPermission));
                    return;
                }
                
                // Store permission info in context for dataScope filtering
                if (requirePermission.dataScope()) {
                    ctx.put("dataScope", true);
                    ctx.put("dataScopeResource", resource);
                }
            }
            
            // Extract parameters
            Object[] args = extractParameters(ctx, method);
            
            // Invoke method
            Object result = method.invoke(controller, args);
            
            // Handle result
            handleResult(ctx, result);
            
        } catch (Exception e) {
            log.error("Error handling request: {}", e.getMessage(), e);
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

    private Object[] extractParameters(RoutingContext ctx, Method method) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            args[i] = extractParameter(ctx, parameters[i]);
        }

        return args;
    }

    private Object extractParameter(RoutingContext ctx, Parameter parameter) {
        Class<?> type = parameter.getType();

        // RoutingContext
        if (type == RoutingContext.class) {
            return ctx;
        }

        // VertxPrincipal - từ context (đã được set bởi auth interceptor)
        if (type == VertxPrincipal.class) {
            return ctx.get("principal");
        }

        // Pageable
        if (type == Pageable.class) {
            Pageable pageable = new Pageable();
            String page = ctx.request().getParam("page");
            String size = ctx.request().getParam("size");
            String sort = ctx.request().getParam("sort");
            String order = ctx.request().getParam("order");
            
            if (page != null) pageable.setPage(Integer.parseInt(page));
            if (size != null) pageable.setSize(Integer.parseInt(size));
            if (sort != null) pageable.setSort(sort);
            if (order != null) pageable.setOrder(order);
            
            return pageable;
        }

        // @VertxRequestBody
        VertxRequestBody requestBody = parameter.getAnnotation(VertxRequestBody.class);
        if (requestBody != null) {
            try {
                JsonObject body = ctx.body().asJsonObject();
                if (body == null && requestBody.required()) {
                    throw new IllegalArgumentException("Request body is required");
                }
                if (body == null) {
                    return null;
                }
                return objectMapper.readValue(body.encode(), type);
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse request body", e);
            }
        }

        // @VertxRequestParam
        VertxRequestParam requestParam = parameter.getAnnotation(VertxRequestParam.class);
        if (requestParam != null) {
            String value = ctx.request().getParam(requestParam.value());
            if (value == null || value.isEmpty()) {
                value = requestParam.defaultValue();
            }
            if ((value == null || value.isEmpty()) && requestParam.required()) {
                throw new IllegalArgumentException("Request param " + requestParam.value() + " is required");
            }
            return convertValue(value, type);
        }

        // @VertxPathVariable
        VertxPathVariable pathVariable = parameter.getAnnotation(VertxPathVariable.class);
        if (pathVariable != null) {
            String value = ctx.pathParam(pathVariable.value());
            return convertValue(value, type);
        }

        // Default: try to get from query params based on parameter name
        String paramName = parameter.getName();
        String value = ctx.request().getParam(paramName);
        if (value != null) {
            return convertValue(value, type);
        }

        return null;
    }

    private Object convertValue(String value, Class<?> type) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        if (type == String.class) return value;
        if (type == Integer.class || type == int.class) return Integer.parseInt(value);
        if (type == Long.class || type == long.class) return Long.parseLong(value);
        if (type == Boolean.class || type == boolean.class) return Boolean.parseBoolean(value);
        if (type == UUID.class) return UUID.fromString(value);
        if (type == Double.class || type == double.class) return Double.parseDouble(value);

        return value;
    }

    @SuppressWarnings("unchecked")
    private void handleResult(RoutingContext ctx, Object result) {
        if (result == null) {
            ctx.response()
                    .setStatusCode(204)
                    .end();
            return;
        }

        // Single<ResponseEntity<...>> - RxJava reactive
        if (result instanceof Single) {
            ((Single<?>) result).subscribe(
                    res -> handleResult(ctx, res),
                    error -> handleError(ctx, (Throwable) error)
            );
            return;
        }

        // ResponseEntity
        if (result instanceof ResponseEntity) {
            ResponseEntity<?> responseEntity = (ResponseEntity<?>) result;
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
        log.error("Request error: {}", error.getMessage());

        int statusCode = 500;
        String message = error.getMessage();

        // Extract status code from exception if possible
        if (error.getMessage() != null && error.getMessage().contains("Unauthorized")) {
            statusCode = 401;
        } else if (error.getMessage() != null && error.getMessage().contains("Forbidden")) {
            statusCode = 403;
        } else if (error.getMessage() != null && error.getMessage().contains("not found")) {
            statusCode = 404;
        }

        ApiResponse<?> response = ApiResponse.error(statusCode, message);
        
        try {
            ctx.response()
                    .setStatusCode(statusCode)
                    .putHeader("Content-Type", "application/json")
                    .end(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            ctx.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "application/json")
                    .end("{\"code\":500,\"message\":\"Internal Server Error\"}");
        }
    }
}
