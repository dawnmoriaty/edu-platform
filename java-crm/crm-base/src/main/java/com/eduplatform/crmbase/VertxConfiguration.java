package com.eduplatform.crmbase;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VertxConfiguration {

    private Vertx vertx;

    @Bean
    public Vertx vertx() {
        vertx = Vertx.vertx();
        return vertx;
    }

    @Bean
    public EventBus eventBus(Vertx vertx) {
        return vertx.eventBus();
    }

    @Bean
    public Scheduler rxScheduler(Vertx vertx) {
        // Use io scheduler for Vert.x async operations
        return Schedulers.io();
    }

    public void cleanup() {
        if (vertx != null) {
            vertx.close();
        }
    }
}
