package com.orbit.common;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/** Date and time helpers that keep every service on UTC. */
public final class Dates {

    private Dates() {
    }

    public static Instant startOfDay(LocalDate date) {
        Objects.requireNonNull(date, "date");
        return date.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    public static Instant endOfDay(LocalDate date) {
        Objects.requireNonNull(date, "date");
        return date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().minusMillis(1);
    }

    public static long daysBetween(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        return ChronoUnit.DAYS.between(start, end);
    }

    public static boolean isBetween(Instant value, Instant start, Instant end) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        return !value.isBefore(start) && !value.isAfter(end);
    }

    public static LocalDate toLocalDate(Instant instant) {
        Objects.requireNonNull(instant, "instant");
        return instant.atZone(ZoneOffset.UTC).toLocalDate();
    }

    public static Instant parse(String text) {
        String value = Checks.requireNotBlank(text, "instant");
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException failure) {
            throw new IllegalArgumentException("invalid instant: " + text, failure);
        }
    }

    public static Instant now() {
        return Instant.now();
    }
}
