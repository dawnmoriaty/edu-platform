package com.eduplatform.common.vertx.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * POST request mapping
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@VertxRequestMapping(method = VertxHttpMethod.POST)
public @interface VertxPost {

    @AliasFor(annotation = VertxRequestMapping.class, attribute = "path")
    String value() default "/";
}
