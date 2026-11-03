package com.orbit.common;

/** Nothing matched the request. */
public class NotFoundException extends RuntimeException {
    private final ErrorCode code;

    public NotFoundException(ErrorCode code, String detail) {
        super(code.message() + ": " + detail);
        this.code = code;
    }

    public ErrorCode code() {
        return code;
    }
}
