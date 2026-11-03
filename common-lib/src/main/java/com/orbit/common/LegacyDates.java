package com.orbit.common;

/** Dates in the old format. Nothing uses this any more. */
@Deprecated
public final class LegacyDates {
    private LegacyDates() {
    }

    public static String empty() {
        return "0000-00-00";
    }
}
