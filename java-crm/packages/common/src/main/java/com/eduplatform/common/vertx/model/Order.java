package com.eduplatform.common.vertx.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Order - Sort order cho pagination
 */
@Data
@Builder
public class Order {
    
    private String property;
    private Direction direction;

    public Order() {}

    public Order(String property, Direction direction) {
        this.property = property;
        this.direction = direction;
    }

    public static Order asc(String property) {
        return new Order(property, Direction.ASC);
    }

    public static Order desc(String property) {
        return new Order(property, Direction.DESC);
    }

    public enum Direction {
        ASC, DESC
    }
}
