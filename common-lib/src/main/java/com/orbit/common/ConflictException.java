package com.orbit.common;

/** The request clashes with the current state. */
public class ConflictException extends RuntimeException {
    private final ErrorCode code;

    public ConflictException(ErrorCode code, String detail) {
        super(code.message() + ": " + detail);
        this.code = code;
    }

    public ErrorCode code() {
        return code;
    }
}
