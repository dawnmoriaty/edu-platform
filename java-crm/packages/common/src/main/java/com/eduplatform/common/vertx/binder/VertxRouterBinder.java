package com.eduplatform.common.vertx.binder;

import io.vertx.ext.web.Router;

/**
 * VertxRouterBinder - Interface cho custom route binding
 * 
 * Implement interface này để register custom routes/middleware
 * 
 * Usage với @VertxBeforeHandler:
 * <pre>
 * @VertxBeforeHandler(order = 1)
 * public class CorsHandler implements VertxRouterBinder {
 *     @Override
 *     public void bind(Router router) {
 *         router.route().handler(CorsHandler.create("*"));
 *     }
 * }
 * </pre>
 */
public interface VertxRouterBinder {
    
    void bind(Router router);
}
