package com.orbit.common;

import java.util.Locale;
import java.util.regex.Pattern;

/** Rules for the public identifiers used by every service. */
public final class Ids {

    private static final Pattern VALID = Pattern.compile("^[A-Z0-9][A-Z0-9-]{1,39}$");

    private Ids() {
    }

    public static String normalize(String code) {
        if (code == null) {
            return null;
        }
        return code.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[\\s_]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-+|-+$", "");
    }

    public static boolean isValid(String code) {
        return code != null && VALID.matcher(code).matches();
    }

    public static String requireValid(String code) {
        String normalized = normalize(code);
        if (!isValid(normalized)) {
            throw new IllegalArgumentException("invalid identifier: " + code);
        }
        return normalized;
    }

    public static String next(String prefix, long sequence) {
        Checks.requireNotBlank(prefix, "prefix");
        Checks.requirePositive(sequence, "sequence");
        return prefix.toUpperCase(Locale.ROOT) + "-" + String.format("%06d", sequence);
    }
}
