package com.eduplatform.common.vertx.model;

import java.util.List;

/**
 * Page model cho pagination response
 */
public class Page<T> {
    
    private List<T> items;
    private Pageable pageable;

    public Page() {}

    public Page(Pageable pageable, List<T> items) {
        this.pageable = pageable;
        this.items = items;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public Pageable getPageable() {
        return pageable;
    }

    public void setPageable(Pageable pageable) {
        this.pageable = pageable;
    }

    public int getPage() {
        return pageable != null ? pageable.getPage() : 0;
    }

    public int getSize() {
        return pageable != null ? pageable.getSize() : 20;
    }

    public long getTotal() {
        return pageable != null ? pageable.getTotal() : 0;
    }

    public int getTotalPages() {
        if (pageable == null || pageable.getSize() == 0) return 0;
        return (int) Math.ceil((double) pageable.getTotal() / pageable.getSize());
    }
}
