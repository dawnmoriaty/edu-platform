package com.eduplatform.shared.paging;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Order {
    private String property;
    private Direction direction;

    public Order() {
    }

    public Order(String property, Direction direction) {
        this.property = property;
        this.direction = direction;
    }

    public enum Direction {
        ASC, DESC
    }
}
