# Runbook: notification backlog

## Symptoms

- Unsent messages exceed the alert threshold and keep growing.
- Delivery reports stop arriving.
- Customers report missing confirmations.

## Triage

1. Is the backlog growing or draining? Check the rate, not only the depth.
2. Split by channel and provider.
3. Check provider status and error codes.
4. Check the suppression lookup: a broken lookup can block every send.
5. Check the worker count and recent deploys.
6. Check database saturation; a slow outbox can look like a provider problem.

## Mitigation

| Cause | Action |
| --- | --- |
| Provider outage | Pause the channel, preserve messages, retry later |
| Poison message | Move the message to a dead-letter store and continue |
| Worker crash | Restart workers one at a time |
| Suppression lookup failure | Fail closed for marketing, allow transactional with a recorded override |
| Database saturation | Reduce concurrency before scaling |

Never drop transactional messages to make the number look better.

## Verification

- Backlog is draining.
- Delivery reports are arriving for new sends.
- Transactional messages are under their latency target.
- No duplicate sends were produced by retries.

## Follow-up

- Add a dead-letter store if the poison message blocked a worker.
- Add an idempotency key to the send so retries cannot duplicate.
- Add an alert on delivery-report silence, not only backlog depth.
