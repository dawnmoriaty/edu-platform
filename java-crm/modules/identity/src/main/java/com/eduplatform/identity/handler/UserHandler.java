package com.eduplatform.identity.handler;

import com.eduplatform.auth.rbac.annotation.RequirePermission;
import com.eduplatform.auth.rbac.model.SecurityUser;
import com.eduplatform.auth.rbac.util.SecurityUtils;
import com.eduplatform.common.domain.Action;
import com.eduplatform.common.response.ApiResponse;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * UserHandler - Vert.x Handler cho User operations
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserHandler {

    /**
     * GET /api/v1/users/me
     */
    public Handler<RoutingContext> getCurrentUser() {
        return ctx -> {
            SecurityUser user = SecurityUtils.getSecurityUser(ctx);
            if (user == null) {
                sendError(ctx, 401, "Unauthorized");
                return;
            }
            sendSuccess(ctx, user);
        };
    }

    /**
     * GET /api/v1/users
     * @RequirePermission(resource = "USER", action = Action.VIEW)
     */
    public Handler<RoutingContext> getUsers() {
        return ctx -> {
            int page = Integer.parseInt(ctx.request().getParam("page", "0"));
            int size = Integer.parseInt(ctx.request().getParam("size", "20"));
            
            // TODO: Implement with UserService
            sendSuccess(ctx, "List users with pagination: page=" + page + ", size=" + size);
        };
    }

    /**
     * GET /api/v1/users/:id
     */
    public Handler<RoutingContext> getUserById() {
        return ctx -> {
            String userId = ctx.pathParam("id");
            // TODO: Implement with UserService
            sendSuccess(ctx, "Get user by id: " + userId);
        };
    }

    private <T> void sendSuccess(RoutingContext ctx, T data) {
        ApiResponse<T> response = ApiResponse.success(data);
        ctx.response()
                .setStatusCode(200)
                .putHeader("Content-Type", "application/json")
                .end(Json.encode(response));
    }

    private void sendError(RoutingContext ctx, int statusCode, String message) {
        ctx.response()
                .setStatusCode(statusCode)
                .putHeader("Content-Type", "application/json")
                .end(String.format("{\"code\":%d,\"message\":\"%s\"}", statusCode, message));
    }
}
