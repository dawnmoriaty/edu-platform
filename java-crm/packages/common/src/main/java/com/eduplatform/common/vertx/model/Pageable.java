package com.eduplatform.common.vertx.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Pageable model cho pagination
 */
public class Pageable {
    
    private int page = 0;
    private int size = 20;
    private long total = 0;
    private String sort;
    private String order = "asc";

    public Pageable() {}

    public Pageable(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public int getOffset() {
        return page * size;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("page", page);
        map.put("size", size);
        map.put("total", total);
        return map;
    }
}
