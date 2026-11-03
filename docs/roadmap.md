# Roadmap

This is a working list, not a commitment. Items move when evidence changes.

## Now

- Keep every service's rules covered by a unit test that fails when the rule
  is removed.
- Add persistence tests for the natural-key and status queries.
- Replace remaining duplicated CRUD with a reviewed shared pattern without
  hiding business rules.
- Make every list endpoint bounded and explicitly paged.

## Next

- Outbox-based publication for order and payment events.
- Idempotency keys on payment capture, refund and stock reservation.
- A contract test suite that runs each gateway against a recorded stub.
- Structured audit records for privileged reads.
- Retention jobs for notification events and application logs.

## Later

- Split catalog search into a dedicated read model.
- Add a pricing service if promotion complexity outgrows catalog.
- Introduce a saga coordinator only if compensation logic becomes unmanageable
  in the owning services.
- Multi-tenant isolation for merchants with separate data boundaries.

## Explicitly not planned

- A shared database across services.
- Storing card data.
- A generic entity framework that hides SQL behind configuration.
- Distributed transactions.

## Measures of success

| Measure | Target |
| --- | --- |
| Service test suite duration | Under 5 minutes |
| p95 read latency | Under 200 ms |
| p95 write latency | Under 400 ms |
| Error budget burn | Within policy |
| Restore test | Quarterly, under 1 hour |
| Rule coverage | Every rule has a failing-without-change test |
