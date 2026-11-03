package com.orbit.common;

import java.math.BigDecimal;
import java.util.Objects;

/** Preconditions with messages that name the offending value. */
public final class Checks {

    private Checks() {
    }

    public static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    public static <T> T requireNotNull(T value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " must be provided");
        }
        return value;
    }

    public static String requireNotBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    public static int requireNonNegative(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
        return value;
    }

    public static long requireNonNegative(long value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
        return value;
    }

    public static BigDecimal requireNonNegative(BigDecimal value, String name) {
        Objects.requireNonNull(value, name);
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
        return value;
    }

    public static int requirePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }

    public static long requirePositive(long value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }

    public static int requireInRange(int value, int minimum, int maximum, String name) {
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException(
                    name + " must be between " + minimum + " and " + maximum);
        }
        return value;
    }

    @SafeVarargs
    public static <T> T requireOneOf(T value, String name, T... allowed) {
        for (T candidate : allowed) {
            if (Objects.equals(candidate, value)) {
                return value;
            }
        }
        throw new IllegalArgumentException(name + " has an unsupported value");
    }
}
