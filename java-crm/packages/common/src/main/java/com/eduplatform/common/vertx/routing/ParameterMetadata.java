package com.eduplatform.common.vertx.routing;

import com.eduplatform.common.vertx.annotation.*;
import com.eduplatform.common.vertx.model.Pageable;
import com.eduplatform.common.vertx.model.VertxPrincipal;
import io.vertx.ext.web.RoutingContext;
import lombok.Builder;
import lombok.Getter;

import java.lang.reflect.Parameter;
import java.util.UUID;

/**
 * ParameterMetadata - Cached metadata cho một parameter
 */
@Getter
@Builder
public class ParameterMetadata {
    
    public enum ParameterType {
        ROUTING_CONTEXT,
        PRINCIPAL,
        PAGEABLE,
        REQUEST_BODY,
        REQUEST_PARAM,
        PATH_VARIABLE,
        QUERY_PARAM  // Default fallback
    }
    
    private final String name;
    private final Class<?> type;
    private final ParameterType parameterType;
    
    // For @VertxRequestBody
    private final boolean bodyRequired;
    
    // For @VertxRequestParam
    private final String paramName;
    private final String defaultValue;
    private final boolean paramRequired;
    
    // For @VertxPathVariable
    private final String pathVarName;
    
    /**
     * Build từ Parameter
     */
    public static ParameterMetadata from(Parameter parameter) {
        Class<?> type = parameter.getType();
        String name = parameter.getName();
        
        ParameterMetadataBuilder builder = ParameterMetadata.builder()
                .name(name)
                .type(type);
        
        // RoutingContext
        if (type == RoutingContext.class) {
            return builder.parameterType(ParameterType.ROUTING_CONTEXT).build();
        }
        
        // VertxPrincipal
        if (type == VertxPrincipal.class) {
            return builder.parameterType(ParameterType.PRINCIPAL).build();
        }
        
        // Pageable
        if (type == Pageable.class) {
            return builder.parameterType(ParameterType.PAGEABLE).build();
        }
        
        // @VertxRequestBody
        VertxRequestBody requestBody = parameter.getAnnotation(VertxRequestBody.class);
        if (requestBody != null) {
            return builder
                    .parameterType(ParameterType.REQUEST_BODY)
                    .bodyRequired(requestBody.required())
                    .build();
        }
        
        // @VertxRequestParam
        VertxRequestParam requestParam = parameter.getAnnotation(VertxRequestParam.class);
        if (requestParam != null) {
            return builder
                    .parameterType(ParameterType.REQUEST_PARAM)
                    .paramName(requestParam.value())
                    .defaultValue(requestParam.defaultValue())
                    .paramRequired(requestParam.required())
                    .build();
        }
        
        // @VertxPathVariable
        VertxPathVariable pathVariable = parameter.getAnnotation(VertxPathVariable.class);
        if (pathVariable != null) {
            return builder
                    .parameterType(ParameterType.PATH_VARIABLE)
                    .pathVarName(pathVariable.value())
                    .build();
        }
        
        // Default: query param with parameter name
        return builder
                .parameterType(ParameterType.QUERY_PARAM)
                .paramName(name)
                .build();
    }
    
    /**
     * Convert string value to target type
     */
    public static Object convertValue(String value, Class<?> type) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        if (type == String.class) return value;
        if (type == Integer.class || type == int.class) return Integer.parseInt(value);
        if (type == Long.class || type == long.class) return Long.parseLong(value);
        if (type == Boolean.class || type == boolean.class) return Boolean.parseBoolean(value);
        if (type == UUID.class) return UUID.fromString(value);
        if (type == Double.class || type == double.class) return Double.parseDouble(value);
        if (type == Float.class || type == float.class) return Float.parseFloat(value);
        if (type == Short.class || type == short.class) return Short.parseShort(value);

        return value;
    }
}
