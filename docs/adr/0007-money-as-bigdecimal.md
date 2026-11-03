# ADR 0007: Money is BigDecimal with an explicit currency

## Status

Accepted.

## Context

Floating-point arithmetic cannot represent most decimal money values exactly.
Rounding drift becomes visible in totals, tax and settlements. A bare
`BigDecimal` does not say which currency it belongs to.

## Decision

- Store money as `numeric(14, 2)`.
- Use `BigDecimal` in Java, never `double` or `float`.
- Use the shared `Money` type when an amount crosses a boundary or is combined.
- Store the currency beside the amount when more than one currency is possible.
- Round half-up to two decimal places at the boundary where a value becomes
  payable.
- Never add amounts in different currencies without an explicit rate.

## Consequences

Benefits:

- Totals are exact at the supported scale.
- Currency mistakes fail loudly instead of being added silently.
- Ledger and settlement code share one arithmetic.

Costs:

- More verbose than a primitive.
- Scale must be normalised consistently; equality compares value, not scale.
- Multi-currency flows need a rate source and a recorded rate.
