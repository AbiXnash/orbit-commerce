package com.orbit.common;

import java.util.List;
import java.util.function.Function;

/** One page of results plus enough information to ask for the next one. */
public record PageResult<T>(List<T> items, long total, int page, int size) {

    public PageResult {
        items = List.copyOf(items);
        if (total < 0) {
            throw new IllegalArgumentException("total must not be negative");
        }
        if (page < 0) {
            throw new IllegalArgumentException("page must not be negative");
        }
        if (size < 1) {
            throw new IllegalArgumentException("size must be positive");
        }
    }

    public static <T> PageResult<T> of(List<T> items, long total, PageRequest request) {
        return new PageResult<>(items, total, request.page(), request.size());
    }

    public static <T> PageResult<T> empty(PageRequest request) {
        return new PageResult<>(List.of(), 0, request.page(), request.size());
    }

    public int pageCount() {
        if (total == 0) {
            return 0;
        }
        return (int) ((total + size - 1) / size);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public boolean isFirst() {
        return page == 0;
    }

    public boolean isLast() {
        return page + 1 >= pageCount();
    }

    public boolean hasNext() {
        return page + 1 < pageCount();
    }

    public boolean hasPrevious() {
        return page > 0;
    }

    public <R> PageResult<R> map(Function<T, R> mapper) {
        return new PageResult<>(items.stream().map(mapper).toList(), total, page, size);
    }
}
