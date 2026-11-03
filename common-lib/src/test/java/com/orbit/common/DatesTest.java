package com.orbit.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class DatesTest {

    @Test
    void findsTheStartOfADay() {
        assertEquals(Instant.parse("2024-01-01T00:00:00Z"), Dates.startOfDay(LocalDate.of(2024, 1, 1)));
    }

    @Test
    void findsTheEndOfADay() {
        assertEquals(
                Instant.parse("2024-01-01T23:59:59.999Z"),
                Dates.endOfDay(LocalDate.of(2024, 1, 1)));
    }

    @Test
    void countsDaysBetweenDates() {
        assertEquals(2, Dates.daysBetween(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 3)));
        assertEquals(-1, Dates.daysBetween(LocalDate.of(2024, 1, 3), LocalDate.of(2024, 1, 2)));
    }

    @Test
    void checksAnInclusiveRange() {
        Instant start = Instant.parse("2024-01-01T00:00:00Z");
        Instant end = Instant.parse("2024-01-31T00:00:00Z");
        assertTrue(Dates.isBetween(start, start, end));
        assertTrue(Dates.isBetween(end, start, end));
        assertFalse(Dates.isBetween(start.minusSeconds(1), start, end));
        assertFalse(Dates.isBetween(end.plusSeconds(1), start, end));
    }

    @Test
    void convertsBackToALocalDate() {
        assertEquals(
                LocalDate.of(2024, 2, 29),
                Dates.toLocalDate(Instant.parse("2024-02-29T12:00:00Z")));
    }

    @Test
    void parsesInstants() {
        assertEquals(Instant.parse("2024-01-01T00:00:00Z"), Dates.parse("2024-01-01T00:00:00Z"));
        assertThrows(IllegalArgumentException.class, () -> Dates.parse(" "));
        assertThrows(IllegalArgumentException.class, () -> Dates.parse("not-a-date"));
    }

    @Test
    void reportsTheCurrentInstant() {
        assertNotNull(Dates.now());
    }
}
