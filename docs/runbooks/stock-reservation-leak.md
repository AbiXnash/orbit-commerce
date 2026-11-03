# Runbook: stock reservation leak

## Symptoms

- Available stock falls while on-hand stock is flat.
- Orders fail with `OUT_OF_STOCK` for products that look present.
- The number of open reservations grows without a matching drop.

## Understand the numbers

```
available = on_hand - open_reservations
```

A leak means open reservations that should have been released or fulfilled are
still open.

## Triage

1. Pick an affected product.
2. List its open reservations, oldest first.
3. Compare each reservation to its order state.
4. Check whether the fulfillment reached a terminal state.
5. Check the expiry job's last successful run.
6. Check for repeated retries that created more than one reservation.

## Common causes

| Cause | Evidence |
| --- | --- |
| Expiry job stopped | Last run is old; expired rows remain open |
| Order cancelled after reserve | Reservation still open |
| Duplicate reserve request | Two rows for the same order line |
| Fulfilment stuck | Reservation open, order not shipped |
| Clock issue | Expiry in the future by a large margin |

## Mitigation

- Restart the expiry job; it is idempotent.
- For a specific order, release the reservation through the service, not SQL.
- For a duplicate, release the extra row and record the reason.
- If the cause is a stuck fulfilment, fix the fulfilment first.

Direct SQL edits are a last resort and require a second reviewer.

## Verification

- Available stock matches on-hand minus genuine open reservations.
- A test order for the affected product can reserve and release.
- The expiry job's next run is scheduled.

## Follow-up

- Add a unique constraint for one open reservation per order line.
- Add an alert on open reservations older than their expiry.
- Add a persistence test for the expiry query.
