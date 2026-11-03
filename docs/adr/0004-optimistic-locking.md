# ADR 0004: Optimistic locking on mutable aggregates

## Status

Accepted.

## Context

Two support agents can open the same record. Two order flows can try to
reserve the same stock. Last-write-wins hides the conflict and corrupts state.

Pessimistic locking would serialise writers and hold database locks across a
request, which is expensive and failure-prone.

## Decision

Every mutable entity carries a `@Version` column. Hibernate increments it on
update. A stale write fails with an optimistic-lock exception, which the API
maps to a conflict response.

The client is expected to reload and retry the change against fresh state.

## Consequences

Benefits:

- Silent overwrites become visible conflicts.
- No long-held database locks.
- The version column also helps debugging and audit.

Costs:

- Clients must handle conflict responses.
- Hot records can produce retry storms; those paths need rate limiting and
  sometimes a queue instead of direct writes.

## Notes

Optimistic locking protects one row. It does not replace a uniqueness
constraint on the natural key, and it does not make a multi-row workflow
atomic.
