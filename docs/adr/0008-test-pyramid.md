# ADR 0008: Test the rules, not the framework

## Status

Accepted.

## Context

Starting a Spring context for every test made the suite slow. Slow tests were
skipped, and skipped tests stopped protecting the rules. At the same time, a
few wiring bugs escaped because nothing exercised the HTTP boundary.

## Decision

Use a layered approach:

- Unit tests with mocked repositories for business rules.
- Standalone MockMvc tests for controllers, validation and error mapping.
- A small number of persistence tests for queries and mapping.
- A very small number of Spring Boot tests for wiring.

A rule test must fail when the rule is deleted. Framework behaviour is not
re-tested.

## Consequences

Benefits:

- The suite stays fast enough to run on every change.
- Failures point at the rule that broke.
- Controller contracts are covered without a database.

Costs:

- More discipline is needed to keep service tests honest.
- Some wiring bugs are caught later than a full-context test would catch them.
- Test helpers are duplicated per domain; consistency is preferred over a
  clever shared fixture.
