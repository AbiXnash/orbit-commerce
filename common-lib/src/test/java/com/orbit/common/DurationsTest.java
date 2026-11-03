package com.orbit.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DurationsTest {

    @Test
    void humanizesMilliseconds() {
        assertEquals("0ms", Durations.humanize(0));
        assertEquals("999ms", Durations.humanize(999));
        assertEquals("1s", Durations.humanize(1_000));
        assertEquals("59s", Durations.humanize(59_999));
        assertEquals("1m", Durations.humanize(60_000));
        assertEquals("1h", Durations.humanize(3_600_000));
    }

    @Test
    void parsesShortDurations() {
        assertEquals(500, Durations.parse("500ms"));
        assertEquals(2_000, Durations.parse("2s"));
        assertEquals(180_000, Durations.parse("3m"));
        assertEquals(7_200_000, Durations.parse("2h"));
    }

    @Test
    void rejectsUnsupportedDurations() {
        assertThrows(IllegalArgumentException.class, () -> Durations.parse(null));
        assertThrows(IllegalArgumentException.class, () -> Durations.parse(" "));
        assertThrows(IllegalArgumentException.class, () -> Durations.parse("2 days"));
        assertThrows(IllegalArgumentException.class, () -> Durations.parse("s"));
    }

    @Test
    void convertsToWholeSecondsAndMinutes() {
        assertEquals(2, Durations.seconds(2_500));
        assertEquals(2, Durations.minutes(125_000));
    }

    @Test
    void rendersIsoDurations() {
        assertEquals("PT1.5S", Durations.iso(1_500));
    }
}
