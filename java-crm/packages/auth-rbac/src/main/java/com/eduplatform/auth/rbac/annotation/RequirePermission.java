package com.eduplatform.auth.rbac.annotation;

import com.eduplatform.common.domain.Action;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation để khai báo permission cho method
 * Interceptor sẽ tự động check permission dựa trên annotation này
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    /**
     * Resource code (VD: "CONTACT", "USER", "STUDENT")
     */
    String resource();

    /**
     * Action cần thực hiện
     */
    Action action();

    /**
     * Có filter data theo scope không?
     * Nếu true, sẽ filter data theo DataScope của user
     */
    boolean dataScope() default false;
}
