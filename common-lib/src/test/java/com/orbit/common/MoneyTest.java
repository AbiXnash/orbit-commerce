package com.orbit.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void roundsToTwoDecimalPlaces() {
        assertEquals(new BigDecimal("10.13"), Money.of("10.126", "USD").amount());
    }

    @Test
    void normalisesTheCurrency() {
        assertEquals("USD", Money.of("1", "usd").currency());
    }

    @Test
    void rejectsABlankCurrency() {
        assertThrows(IllegalArgumentException.class, () -> Money.of("1", " "));
    }

    @Test
    void addsAmountsInTheSameCurrency() {
        Money total = Money.of("10.00", "USD").plus(Money.of("2.50", "USD"));
        assertEquals(Money.of("12.50", "USD"), total);
    }

    @Test
    void refusesToMixCurrencies() {
        Money usd = Money.of("10", "USD");
        Money eur = Money.of("10", "EUR");
        assertThrows(IllegalArgumentException.class, () -> usd.plus(eur));
    }

    @Test
    void subtractsAmounts() {
        assertEquals(Money.of("7.50", "USD"), Money.of("10", "USD").minus(Money.of("2.50", "USD")));
    }

    @Test
    void multipliesByAWholeNumber() {
        assertEquals(Money.of("30.00", "USD"), Money.of("10", "USD").times(3));
    }

    @Test
    void multipliesByAFraction() {
        assertEquals(Money.of("5.00", "USD"), Money.of("10", "USD").times(new BigDecimal("0.5")));
    }

    @Test
    void negatesAnAmount() {
        assertEquals(Money.of("-10.00", "USD"), Money.of("10", "USD").negate());
    }

    @Test
    void comparesAmounts() {
        assertTrue(Money.of("10", "USD").compareTo(Money.of("5", "USD")) > 0);
        assertEquals(0, Money.of("10.00", "USD").compareTo(Money.of("10", "USD")));
    }

    @Test
    void equalityIgnoresScale() {
        assertEquals(Money.of("10.00", "USD"), Money.of("10", "USD"));
        assertEquals(Money.of("10.00", "USD").hashCode(), Money.of("10", "USD").hashCode());
    }

    @Test
    void reportsSign() {
        assertTrue(Money.of("0", "USD").isZero());
        assertTrue(Money.of("-1", "USD").isNegative());
        assertTrue(Money.of("1", "USD").isPositive());
        assertFalse(Money.of("1", "USD").isZero());
    }

    @Test
    void rendersCurrencyAndAmount() {
        assertEquals("USD 10.00", Money.of("10", "USD").toString());
    }
}
