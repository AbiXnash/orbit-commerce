package com.orbit.common;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Conversion between millisecond counts and short, readable durations. */
public final class Durations {

    private static final Pattern SIMPLE = Pattern.compile("^(\\d+)(ms|s|m|h)$");

    private Durations() {
    }

    public static String humanize(long millis) {
        if (millis <= 0) {
            return "0ms";
        }
        if (millis < 1_000) {
            return millis + "ms";
        }
        if (millis < 60_000) {
            return (millis / 1_000) + "s";
        }
        if (millis < 3_600_000) {
            return (millis / 60_000) + "m";
        }
        return (millis / 3_600_000) + "h";
    }

    public static long parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("duration must not be blank");
        }
        Matcher matcher = SIMPLE.matcher(text.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("unsupported duration: " + text);
        }
        long value = Long.parseLong(matcher.group(1));
        return switch (matcher.group(2)) {
            case "ms" -> value;
            case "s" -> value * 1_000;
            case "m" -> value * 60_000;
            default -> value * 3_600_000;
        };
    }

    public static long seconds(long millis) {
        return millis / 1_000;
    }

    public static long minutes(long millis) {
        return millis / 60_000;
    }

    public static String iso(long millis) {
        return Duration.ofMillis(millis).toString();
    }
}
