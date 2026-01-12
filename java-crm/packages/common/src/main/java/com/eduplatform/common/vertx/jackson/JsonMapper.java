package com.eduplatform.common.vertx.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JsonMapper - Static access to ObjectMapper (giống DefaultJackson của spring-vertx-core)
 * 
 * Cho phép access ObjectMapper từ bất cứ đâu:
 * - JsonMapper.toJson(object)
 * - JsonMapper.fromJson(json, Class)
 * - JsonMapper.objectMapper()
 * 
 * Note: Spring Boot tự động configure ObjectMapper với Java Time support
 * qua dependency spring-boot-starter-web hoặc spring-boot-starter-json
 */
@Slf4j
@Component
public class JsonMapper {

    private static ObjectMapper MAPPER;

    @Autowired(required = false)
    private ObjectMapper injectedMapper;

    @PostConstruct
    public void init() {
        if (injectedMapper != null) {
            MAPPER = injectedMapper;
            log.debug("JsonMapper using Spring-configured ObjectMapper");
        } else {
            MAPPER = createDefaultMapper();
            log.debug("JsonMapper created default ObjectMapper");
        }
    }

    private static ObjectMapper createDefaultMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Note: JavaTimeModule sẽ được add bởi Spring Boot nếu có dependency
        // Nếu không có Spring Boot, cần add dependency jackson-datatype-jsr310
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }

    public static ObjectMapper objectMapper() {
        if (MAPPER == null) {
            MAPPER = createDefaultMapper();
        }
        return MAPPER;
    }

    // ============================================
    // Serialize methods
    // ============================================

    public static String toJson(Object object) {
        try {
            return objectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object: {}", e.getMessage());
            return null;
        }
    }

    public static String toJsonPretty(Object object) {
        try {
            return objectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object: {}", e.getMessage());
            return null;
        }
    }

    // ============================================
    // Deserialize methods
    // ============================================

    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) return null;
        try {
            return objectMapper().readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize json: {}", e.getMessage());
            return null;
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> typeRef) {
        if (json == null || json.isEmpty()) return null;
        try {
            return objectMapper().readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize json: {}", e.getMessage());
            return null;
        }
    }

    public static <T> T fromJson(String json, JavaType javaType) {
        if (json == null || json.isEmpty()) return null;
        try {
            return objectMapper().readValue(json, javaType);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize json: {}", e.getMessage());
            return null;
        }
    }

    // ============================================
    // Convert methods (object to object)
    // ============================================

    public static <T> T convert(Object fromValue, Class<T> toValueType) {
        return objectMapper().convertValue(fromValue, toValueType);
    }

    public static <T> T convert(Object fromValue, TypeReference<T> toValueTypeRef) {
        return objectMapper().convertValue(fromValue, toValueTypeRef);
    }
}
