package com.eduplatform.shared.paging;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Pageable {
    private int page = 0;
    private int size = 20;
    private List<Order> orders = new ArrayList<>();

    public Pageable() {
    }

    public Pageable(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public Pageable(int page, int size, List<Order> orders) {
        this.page = page;
        this.size = size;
        this.orders = orders != null ? orders : new ArrayList<>();
    }

    public static Pageable of(int page, int size) {
        return new Pageable(page, size);
    }

    public static Pageable of(int page, int size, Order... orders) {
        return new Pageable(page, size, List.of(orders));
    }

    public long getOffset() {
        return (long) page * size;
    }

    public Pageable next() {
        return new Pageable(page + 1, size, orders);
    }

    public Pageable previous() {
        return page > 0 ? new Pageable(page - 1, size, orders) : this;
    }

    public Pageable first() {
        return new Pageable(0, size, orders);
    }

    public boolean hasPrevious() {
        return page > 0;
    }
}
