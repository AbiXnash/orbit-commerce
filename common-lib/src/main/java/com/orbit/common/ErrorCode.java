package com.orbit.common;

/** Every error a service can report to a client. */
public enum ErrorCode {
    NOT_FOUND("The item doesn't exist"),
    DUPLICATE_CODE("That code is already in use"),
    NOT_ACTIVE("The item is archived"),
    PAYMENT_DECLINED("The payment was declined"),
    OUT_OF_STOCK("The item is out of stock"),
    ;

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
