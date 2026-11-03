# ADR 0006: UTC everywhere

## Status

Accepted.

## Context

Orders, payments and deliveries cross time zones. Storing local times without
a zone produced ambiguous reports and duplicated records around daylight
saving changes.

## Decision

- Persist instants, not local date-times.
- Use `Instant` in Java and `timestamp with time zone` in the database.
- Render ISO-8601 with a trailing `Z`.
- Compute business-day boundaries in UTC unless a legal document names a
  different zone.
- Convert to a customer's zone only at the presentation edge.

## Consequences

Benefits:

- Comparisons and ordering are unambiguous.
- A daylight-saving change cannot duplicate or skip an hour of data.
- Logs from different regions can be merged directly.

Costs:

- A user-facing "today" needs an explicit zone at the edge.
- Reports that must follow a local calendar need a documented conversion.
