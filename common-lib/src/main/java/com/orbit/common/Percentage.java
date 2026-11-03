package com.orbit.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** A percentage between 0 and 100, used for discounts, tax and rates. */
public final class Percentage implements Comparable<Percentage> {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final int SCALE = 4;

    private final BigDecimal value;

    private Percentage(BigDecimal value) {
        this.value = value.stripTrailingZeros();
    }

    public static Percentage of(BigDecimal value) {
        Objects.requireNonNull(value, "value");
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(HUNDRED) > 0) {
            throw new IllegalArgumentException("percentage must be between 0 and 100");
        }
        return new Percentage(value);
    }

    public static Percentage of(String value) {
        return of(new BigDecimal(value));
    }

    public static Percentage zero() {
        return new Percentage(BigDecimal.ZERO);
    }

    public BigDecimal value() {
        return value;
    }

    public BigDecimal asFraction() {
        return value.divide(HUNDRED, SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal applyTo(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount");
        return amount.multiply(value).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    public Money applyTo(Money money) {
        Objects.requireNonNull(money, "money");
        return Money.of(applyTo(money.amount()), money.currency());
    }

    public Percentage plus(Percentage other) {
        Objects.requireNonNull(other, "other");
        return of(value.add(other.value));
    }

    public Percentage minus(Percentage other) {
        Objects.requireNonNull(other, "other");
        return of(value.subtract(other.value));
    }

    public boolean isZero() {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public int compareTo(Percentage other) {
        return value.compareTo(other.value);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Percentage that)) {
            return false;
        }
        return value.compareTo(that.value) == 0;
    }

    @Override
    public int hashCode() {
        return value.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return value.toPlainString() + "%";
    }
}
