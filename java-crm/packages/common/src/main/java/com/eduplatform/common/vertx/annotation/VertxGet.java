package com.eduplatform.common.vertx.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * GET request mapping
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@VertxRequestMapping(method = VertxHttpMethod.GET)
public @interface VertxGet {

    @AliasFor(annotation = VertxRequestMapping.class, attribute = "path")
    String value() default "/";
}
