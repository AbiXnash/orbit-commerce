package com.orbit.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PercentageTest {

    @Test
    void refusesValuesOutsideTheRange() {
        assertThrows(IllegalArgumentException.class, () -> Percentage.of("-1"));
        assertThrows(IllegalArgumentException.class, () -> Percentage.of("101"));
    }

    @Test
    void appliesToAnAmount() {
        assertEquals(new BigDecimal("15.00"), Percentage.of("10").applyTo(new BigDecimal("150")));
    }

    @Test
    void appliesToMoney() {
        Money discounted = Percentage.of("10").applyTo(Money.of("150", "USD"));
        assertEquals(Money.of("15.00", "USD"), discounted);
    }

    @Test
    void convertsToAFraction() {
        assertEquals(new BigDecimal("0.2500"), Percentage.of("25").asFraction());
    }

    @Test
    void addsWithinTheRange() {
        assertEquals(Percentage.of("30"), Percentage.of("10").plus(Percentage.of("20")));
    }

    @Test
    void refusesToAddBeyondOneHundred() {
        assertThrows(IllegalArgumentException.class, () -> Percentage.of("80").plus(Percentage.of("30")));
    }

    @Test
    void subtracts() {
        assertEquals(Percentage.of("20"), Percentage.of("30").minus(Percentage.of("10")));
    }

    @Test
    void reportsZero() {
        assertTrue(Percentage.zero().isZero());
    }

    @Test
    void equalityIgnoresScale() {
        assertEquals(Percentage.of("10.0"), Percentage.of("10"));
        assertEquals(Percentage.of("10.0").hashCode(), Percentage.of("10").hashCode());
    }

    @Test
    void rendersWithAPercentSign() {
        assertEquals("10%", Percentage.of("10.0").toString());
    }
}
