package com.eduplatform.common.vertx.routing;

import com.eduplatform.common.constant.Action;
import com.eduplatform.common.vertx.annotation.*;
import io.vertx.core.http.HttpMethod;
import lombok.Builder;
import lombok.Getter;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * RouteMetadata - Cached metadata cho một route
 * 
 * Thay vì dùng reflection mỗi request, cache tất cả thông tin
 * lúc khởi động để truy xuất tức thì.
 */
@Getter
@Builder
public class RouteMetadata {
    
    // Route info
    private final String path;
    private final HttpMethod httpMethod;
    
    // Controller info
    private final Object controller;
    private final Method method;
    private final String controllerName;
    private final String methodName;
    
    // Permission info (null if no permission required)
    private final String resource;
    private final Action action;
    private final boolean requireDataScope;
    private final boolean requiresAuth;
    
    // Return type info
    private final Class<?> returnType;
    private final boolean isReactive;  // Single, Maybe, Completable
    private final boolean isResponseEntity;
    
    // Parameters (cached)
    private final List<ParameterMetadata> parameters;
    
    /**
     * Kiểm tra xem method có phải reactive hay không
     * Reactive types: Single, Maybe, Completable, Flowable, Observable
     */
    public static boolean isReactiveReturnType(Class<?> returnType) {
        if (returnType == null) return false;
        
        String typeName = returnType.getName();
        return typeName.startsWith("io.reactivex.rxjava3.core.") ||
               typeName.startsWith("io.reactivex.rxjava2.") ||
               typeName.equals("reactor.core.publisher.Mono") ||
               typeName.equals("reactor.core.publisher.Flux") ||
               typeName.equals("java.util.concurrent.CompletableFuture") ||
               typeName.equals("io.vertx.core.Future");
    }
    
    /**
     * Build metadata từ controller và method
     */
    public static RouteMetadata from(Object controller, Method method, String basePath) {
        VertxRequestMapping requestMapping = AnnotatedElementUtils.getMergedAnnotation(method, VertxRequestMapping.class);
        if (requestMapping == null) {
            return null;
        }
        
        String path = basePath + requestMapping.path();
        HttpMethod httpMethod = requestMapping.method().getVertxMethod();
        
        // Permission info
        RequirePermission requirePermission = method.getAnnotation(RequirePermission.class);
        String resource = null;
        Action action = null;
        boolean requireDataScope = false;
        boolean requiresAuth = false;
        
        if (requirePermission != null) {
            resource = requirePermission.resource();
            action = requirePermission.action();
            requireDataScope = requirePermission.dataScope();
            requiresAuth = true;
        }
        
        // Return type analysis
        Class<?> returnType = method.getReturnType();
        boolean isReactive = isReactiveReturnType(returnType);
        boolean isResponseEntity = org.springframework.http.ResponseEntity.class.isAssignableFrom(returnType);
        
        // Parameters
        List<ParameterMetadata> params = new ArrayList<>();
        for (Parameter param : method.getParameters()) {
            params.add(ParameterMetadata.from(param));
        }
        
        return RouteMetadata.builder()
                .path(path)
                .httpMethod(httpMethod)
                .controller(controller)
                .method(method)
                .controllerName(controller.getClass().getSimpleName())
                .methodName(method.getName())
                .resource(resource)
                .action(action)
                .requireDataScope(requireDataScope)
                .requiresAuth(requiresAuth)
                .returnType(returnType)
                .isReactive(isReactive)
                .isResponseEntity(isResponseEntity)
                .parameters(params)
                .build();
    }
    
    /**
     * Check nếu cần permission
     */
    public boolean hasPermissionRequired() {
        return resource != null && action != null;
    }
    
    /**
     * Get required permission string (e.g., "CONTACT:VIEW")
     */
    public String getRequiredPermission() {
        if (!hasPermissionRequired()) return null;
        return resource + ":" + action.getCode();
    }
}
