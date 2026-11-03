package com.orbit.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ChecksTest {

    @Test
    void requiresACondition() {
        Checks.require(true, "never seen");
        IllegalArgumentException failure =
                assertThrows(IllegalArgumentException.class, () -> Checks.require(false, "broken"));
        assertEquals("broken", failure.getMessage());
    }

    @Test
    void requiresANonNullValue() {
        Object value = new Object();
        assertSame(value, Checks.requireNotNull(value, "value"));
        assertThrows(IllegalArgumentException.class, () -> Checks.requireNotNull(null, "value"));
    }

    @Test
    void requiresNonBlankText() {
        assertEquals("a", Checks.requireNotBlank("a", "text"));
        assertThrows(IllegalArgumentException.class, () -> Checks.requireNotBlank(" ", "text"));
        assertThrows(IllegalArgumentException.class, () -> Checks.requireNotBlank(null, "text"));
    }

    @Test
    void requiresNonNegativeNumbers() {
        assertEquals(0, Checks.requireNonNegative(0, "count"));
        assertEquals(0L, Checks.requireNonNegative(0L, "count"));
        assertEquals(BigDecimal.ZERO, Checks.requireNonNegative(BigDecimal.ZERO, "amount"));
        assertThrows(IllegalArgumentException.class, () -> Checks.requireNonNegative(-1, "count"));
        assertThrows(IllegalArgumentException.class, () -> Checks.requireNonNegative(-1L, "count"));
        assertThrows(
                IllegalArgumentException.class,
                () -> Checks.requireNonNegative(new BigDecimal("-1"), "amount"));
    }

    @Test
    void requiresPositiveNumbers() {
        assertEquals(1, Checks.requirePositive(1, "count"));
        assertEquals(1L, Checks.requirePositive(1L, "count"));
        assertThrows(IllegalArgumentException.class, () -> Checks.requirePositive(0, "count"));
        assertThrows(IllegalArgumentException.class, () -> Checks.requirePositive(0L, "count"));
    }

    @Test
    void requiresAValueInsideARange() {
        assertEquals(5, Checks.requireInRange(5, 1, 10, "rating"));
        assertThrows(IllegalArgumentException.class, () -> Checks.requireInRange(0, 1, 10, "rating"));
        assertThrows(IllegalArgumentException.class, () -> Checks.requireInRange(11, 1, 10, "rating"));
    }

    @Test
    void requiresOneOfTheAllowedValues() {
        assertEquals("A", Checks.requireOneOf("A", "status", "A", "B"));
        assertThrows(IllegalArgumentException.class, () -> Checks.requireOneOf("C", "status", "A", "B"));
    }
}
