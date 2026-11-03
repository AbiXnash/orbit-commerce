# ADR 0002: One database per service

## Status

Accepted.

## Context

The services model separate business capabilities. A shared database would
make it cheap to join across capabilities, but it would also couple releases,
couple schemas and allow one team to break another team's queries.

## Decision

Each service owns its schema and its database user. No service reads another
service's tables. Cross-service data is exchanged through HTTP contracts.

## Consequences

Benefits:

- A schema change is local to its service.
- Failure isolation improves; one bad query cannot lock another service.
- Ownership is unambiguous.

Costs:

- Cross-service reporting needs a read model or an export.
- A workflow that spans services needs explicit compensation.
- Duplicate reference data must be kept deliberately, not accidentally.

## Alternatives considered

- **Single shared schema:** rejected because it recreates a distributed
  monolith with none of the benefits of either shape.
- **Read replicas shared across services:** rejected for the same ownership
  reason, even though it is operationally simpler.
