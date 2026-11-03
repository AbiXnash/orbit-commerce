# ADR 0009: Bound every list read

## Status

Accepted. Rollout in progress.

## Context

Early list endpoints returned every matching row. That was fine with a few
hundred records and became a liability as catalogs and order histories grew.
An unbounded read consumes memory, holds a database connection for longer than
necessary and gives a client no way to resume.

Silently clamping a requested size is also harmful: a client that asks for
1000 and receives 20 cannot tell whether the data is complete.

## Decision

Every list endpoint that can grow returns a bounded page.

- `PageRequest` validates `page` (zero-based, non-negative) and `size`
  (between 1 and 200). An invalid value is a `400 INVALID_REQUEST`.
- `PageResult` carries `items`, `total`, `page` and `size`, plus helpers for
  navigation and mapping.
- The service translates the request into a Spring Data `Pageable` with a
  deterministic sort, usually priority descending and then id ascending.
- The unpaged list endpoint is kept during the transition for compatibility,
  but new clients use the paged endpoint.
- Search helpers that do not page still cap their result with
  `Limits.PAGE_SIZE`.

The first services to expose a paged read for their primary resource are
order, catalog and inventory. The remaining services follow the same shape.

## Consequences

Benefits:

- Memory and database work per request have a known upper bound.
- Clients can show a stable total and a next-page control.
- An invalid page is a clear error instead of a surprise truncation.
- The sort is explicit and stable across pages.

Costs:

- Every paged handler needs a `count` query as well as the page query.
- A large `count` on a hot table can be expensive; those endpoints may need a
  cached total or a keyset strategy later.
- Clients must handle an empty last page.

## Alternatives considered

- **Offset paging with a silent clamp:** rejected because it hides data loss.
- **Keyset (cursor) paging only:** attractive for very large tables, but it
  cannot jump to an arbitrary page and complicates the first release. Revisit
  when a table outgrows offset paging.
- **Streaming responses:** rejected for the storefront; the clients are JSON
  APIs, not file consumers.
