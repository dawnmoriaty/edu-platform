package com.eduplatform.common.paging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String property;
    private Direction direction = Direction.ASC;

    public enum Direction {
        ASC, DESC
    }

    public static Order asc(String property) {
        return new Order(property, Direction.ASC);
    }

    public static Order desc(String property) {
        return new Order(property, Direction.DESC);
    }
}
