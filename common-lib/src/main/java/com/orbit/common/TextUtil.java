package com.orbit.common;

/** Small text helpers. */
public final class TextUtil {
    private TextUtil() {
    }

    public static String normalise(String text) {
        return text == null ? "" : text.trim().toLowerCase();
    }
}
