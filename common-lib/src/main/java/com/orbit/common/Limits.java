package com.orbit.common;

/** Limits shared by every service. */
public final class Limits {
    public static final int PAGE_SIZE = 100;
    public static final int MAX_RETRIES = 3;
    public static final long TIMEOUT_MILLIS = 2000;

    private Limits() {
    }
}
