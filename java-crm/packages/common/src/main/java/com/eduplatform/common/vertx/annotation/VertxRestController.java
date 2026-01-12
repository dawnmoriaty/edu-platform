package com.eduplatform.common.vertx.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Đánh dấu một class là Vert.x REST Controller.
 * Sử dụng kết hợp với @VertxGet, @VertxPost, @VertxPut, @VertxDelete
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface VertxRestController {
    /**
     * Base path cho tất cả endpoints trong controller
     */
    String value() default "";
}
