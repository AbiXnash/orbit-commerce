# Runbook: high error rate

## Symptoms

- The service returns 5xx above the alert threshold.
- p95 latency rises at the same time.
- The error budget burns faster than the release cadence explains.

## Triage

1. Open the service dashboard. Is the error rate concentrated on one path?
2. Check the last deploy and the last configuration change.
3. Check the database: connection pool pending, slow queries, failover.
4. Check downstream dependencies; a timeout often surfaces as a 5xx.
5. Search the logs for the correlation id of one failing request.

## Mitigation

| Cause | Action |
| --- | --- |
| Bad deploy | Roll back to the previous image |
| Configuration change | Revert the change and restart |
| Database saturation | Reduce traffic or scale the database |
| Downstream outage | Enable the documented fallback or shed load |
| Traffic spike | Scale out; check the rate limit at the edge |
| Poison payload | Reject the payload at the edge; preserve it for analysis |

Do not restart every instance at once. Restart one, verify, continue.

## Verification

- 5xx rate is below 1% for ten minutes.
- p95 latency is back under the service target.
- The business metric for the affected path has recovered.
- No new errors in the logs for ten minutes.

## Follow-up

- Add a test that reproduces the failure.
- Add a metric or alert if detection was slow.
- Record the timeline and the customer impact.
