package com.orbit.common;

/** How long to wait before trying a failed call again. */
public final class RetryPolicy {
    private RetryPolicy() {
    }

    public static long delayMillis(int attempt) {
        return 100L * (1L << Math.min(attempt, 6));
    }
}
