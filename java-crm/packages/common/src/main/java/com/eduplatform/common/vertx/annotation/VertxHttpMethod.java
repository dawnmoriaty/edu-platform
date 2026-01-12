package com.eduplatform.common.vertx.annotation;

import io.vertx.core.http.HttpMethod;

/**
 * HTTP methods cho Vert.x routing
 */
public enum VertxHttpMethod {
    GET(HttpMethod.GET),
    POST(HttpMethod.POST),
    PUT(HttpMethod.PUT),
    DELETE(HttpMethod.DELETE),
    PATCH(HttpMethod.PATCH);

    private final HttpMethod vertxMethod;

    VertxHttpMethod(HttpMethod vertxMethod) {
        this.vertxMethod = vertxMethod;
    }

    public HttpMethod getVertxMethod() {
        return vertxMethod;
    }
}
