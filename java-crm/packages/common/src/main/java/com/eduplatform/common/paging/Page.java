package com.eduplatform.common.paging;

import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class Page<T> {
    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean loadMoreAble;

    public Page(List<T> content, int page, int size, long totalElements) {
        this.content = content != null ? content : Collections.emptyList();
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        this.loadMoreAble = page < totalPages - 1;
    }

    public static <T> Page<T> of(List<T> content, Pageable pageable, long totalElements) {
        return new Page<>(content, pageable.getPage(), pageable.getSize(), totalElements);
    }

    public static <T> Page<T> empty() {
        return new Page<>(Collections.emptyList(), 0, 0, 0);
    }

    public static <T> Page<T> empty(Pageable pageable) {
        return new Page<>(Collections.emptyList(), pageable.getPage(), pageable.getSize(), 0);
    }

    public boolean hasContent() {
        return !content.isEmpty();
    }

    public boolean hasNext() {
        return loadMoreAble;
    }

    public boolean hasPrevious() {
        return page > 0;
    }

    public boolean isFirst() {
        return page == 0;
    }

    public boolean isLast() {
        return !loadMoreAble;
    }

    public <U> Page<U> map(Function<? super T, ? extends U> converter) {
        List<U> mappedContent = content.stream()
                .map(converter)
                .collect(Collectors.toList());
        return new Page<>(mappedContent, page, size, totalElements);
    }
}
