package com.orbit.common;

/** Small text helpers. */
public final class Strings {
    private Strings() {
    }

    public static String normalise(String text) {
        return text == null ? "" : text.trim().toLowerCase();
    }
}
