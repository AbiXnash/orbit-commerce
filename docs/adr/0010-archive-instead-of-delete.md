# ADR 0010: Archive instead of delete

## Status

Accepted.

## Context

Business records are referenced by other records, by reports and by audits. A
physical delete breaks those references and destroys the evidence needed to
explain a past decision. At the same time, operators need a way to remove
something from active use without a risky migration.

## Decision

The public `DELETE` operation archives a record. It sets `status` to
`ARCHIVED` and leaves the row in place.

- Archived records remain readable by id and by natural key.
- Updates, renames and amount changes against an archived record are rejected
  with `409 NOT_ACTIVE`.
- `activate` can move an archived record back to `ACTIVE`. This is deliberate:
  archiving is a business state, not a tombstone.
- Physical deletion is reserved for data-retention jobs that run under a
  documented policy, never for an API request.
- Lists that support it expose `all-active` and `stats/archived` so callers do
  not have to infer state.

## Consequences

Benefits:

- References stay valid.
- History and audit remain intact.
- A mistaken archive is reversible.
- No cascading deletes and no orphaned rows.

Costs:

- Tables grow; retention and archival-to-cold-storage jobs are required.
- Every query that means "in use" must filter on status.
- A unique natural key remains occupied after archive. Reusing a code requires
  an explicit decision, because two rows with the same business identity would
  confuse reports.

## Alternatives considered

- **Hard delete with cascades:** rejected because it destroys audit evidence
  and creates cross-service dangling references.
- **Soft delete with a boolean `deleted` flag:** rejected because it cannot
  express the difference between inactive and archived, and it tends to be
  forgotten in queries.
- **Archive to a separate history table:** rejected for the first version; it
  doubles the write path without adding a capability the business needs yet.
