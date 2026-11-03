# ADR 0003: REST between services, hidden behind gateways

## Status

Accepted.

## Context

Services need to call each other for prices, stock, customers and payments.
Direct client calls inside business rules make those rules hard to test and
hard to change when a contract moves.

## Decision

Use synchronous HTTP with JSON for request/response interactions. Every remote
dependency is wrapped in a gateway in the calling service. Business rules
depend on the gateway's local interface, never on the generated client.

Gateways own:

- the remote path and payload mapping
- timeouts and retry policy
- translation of remote failures into local failures
- a small local record instead of a remote DTO

## Consequences

Benefits:

- Service rules are unit-testable with a mocked gateway.
- A transport change is local to one package.
- Timeouts and retries are visible in one place.

Costs:

- One more interface per dependency.
- A synchronous call still couples availability; the caller must decide what
  to do when the dependency is down.

## Alternatives considered

- **Direct Feign clients in services:** rejected because it leaks transport into
  business code and makes tests slow.
- **Messaging for every interaction:** rejected for reads, where freshness and
  simplicity matter more than decoupling.
