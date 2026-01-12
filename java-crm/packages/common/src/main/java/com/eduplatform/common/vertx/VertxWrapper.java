package com.eduplatform.common.vertx;

import io.vertx.core.Vertx;

/**
 * VertxWrapper - Static access to Vertx instance
 * 
 * Cho phép access Vertx từ bất cứ đâu mà không cần inject:
 * - VertxWrapper.vertx().executeBlocking(...)
 * - VertxWrapper.vertx().eventBus().send(...)
 * 
 * Được set bởi WebConfig khi khởi tạo Vertx
 */
public class VertxWrapper {

    private static Vertx VERTX;

    public static Vertx vertx() {
        if (VERTX == null) {
            throw new IllegalStateException("Vertx not initialized. Call VertxWrapper.init(vertx) first.");
        }
        return VERTX;
    }

    public static void init(Vertx vertx) {
        VERTX = vertx;
    }
    
    public static boolean isInitialized() {
        return VERTX != null;
    }
}
