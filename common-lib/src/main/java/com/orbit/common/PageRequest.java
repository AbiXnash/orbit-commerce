package com.orbit.common;

/** A safe, zero-based request for one page of results. */
public record PageRequest(int page, int size) {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 200;

    public PageRequest {
        if (page < 0) {
            throw new IllegalArgumentException("page must not be negative");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new IllegalArgumentException("size must be between 1 and " + MAX_SIZE);
        }
    }

    public static PageRequest first() {
        return new PageRequest(0, DEFAULT_SIZE);
    }

    public static PageRequest first(int size) {
        return new PageRequest(0, size);
    }

    public int offset() {
        return page * size;
    }

    public int limit() {
        return size;
    }

    public PageRequest next() {
        return new PageRequest(page + 1, size);
    }

    public PageRequest previous() {
        return new PageRequest(Math.max(0, page - 1), size);
    }

    public PageRequest withSize(int newSize) {
        return new PageRequest(page, newSize);
    }
}
