package com.orbit.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Small text helpers shared by every service. */
public final class Texts {

    private static final int MAX_INITIALS = 3;

    private Texts() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static String emptyIfNull(String value) {
        return value == null ? "" : value;
    }

    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String capitalize(String value) {
        if (isBlank(value)) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    public static String truncate(String value, int maxLength) {
        if (value == null || maxLength <= 0) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    public static String truncateWithEllipsis(String value, int maxLength) {
        if (value == null || maxLength <= 0) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        if (maxLength <= 3) {
            return ".".repeat(maxLength);
        }
        return value.substring(0, maxLength - 3) + "...";
    }

    public static String slugify(String value) {
        if (isBlank(value)) {
            return "";
        }
        String slug = value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return slug;
    }

    public static String initials(String value) {
        if (isBlank(value)) {
            return "";
        }
        String[] words = value.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String word : words) {
            if (initials.length() >= MAX_INITIALS) {
                break;
            }
            initials.append(Character.toUpperCase(word.charAt(0)));
        }
        return initials.toString();
    }

    public static String joinNonBlank(String delimiter, String... values) {
        List<String> parts = new ArrayList<>();
        if (values != null) {
            for (String value : values) {
                if (!isBlank(value)) {
                    parts.add(value.trim());
                }
            }
        }
        return String.join(delimiter, parts);
    }

    public static String mask(String value, int visible) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (visible <= 0) {
            return "*".repeat(value.length());
        }
        if (value.length() <= visible) {
            return value;
        }
        return "*".repeat(value.length() - visible) + value.substring(value.length() - visible);
    }

    public static int compareNullsLast(String left, String right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return left.compareToIgnoreCase(right);
    }
}
