# Operations

This guide covers running, observing and recovering the services. It assumes
the reader is on call and needs an answer quickly.

## Running a service

```bash
mvn -pl catalog-service -am spring-boot:run
mvn -pl order-service -am spring-boot:run
```

Each service reads `src/main/resources/application.yml`. The default profile
uses an in-memory H2 database, which is suitable for local development only.
Deployments supply the datasource URL, credentials and service addresses
through environment variables.

## Health

Every Spring Boot service exposes the standard actuator health endpoint when
the actuator starter is present:

```
GET /actuator/health
```

A healthy instance returns `{"status":"UP"}`. A readiness check must also
verify the database connection; a process that cannot reach its database is not
ready even if the HTTP listener is up.

## Logs

- Logs go to standard output; the platform collects them.
- The service name and instance id are part of the log context.
- State changes are logged at `INFO` with the natural key.
- Expected business rejections are not logged as errors.
- Unexpected exceptions are logged once, with the stack trace, at the
  boundary that owns the request.

Never log request bodies, tokens, card data or full customer records.

## Key metrics

| Metric | Meaning | Alert threshold |
| --- | --- | --- |
| `http.server.requests` p95 | Request latency | > 1s for 5 minutes |
| `http.server.requests` 5xx rate | Server failures | > 1% for 5 minutes |
| `http.server.requests` 4xx rate | Client failures | > 10% for 10 minutes |
| `hikaricp.connections.pending` | Connection starvation | > 0 for 5 minutes |
| `jvm.memory.used` | Heap pressure | > 85% after GC |
| `order.created` rate | Business volume | Drop > 50% vs same hour |
| `payment.declined` rate | Provider health | > 10% for 5 minutes |
| `notification.backlog` | Unsent messages | > 1000 or growing |

Business metrics are as important as infrastructure metrics. A healthy process
that has stopped accepting orders is still an incident.

## Request tracing

Every inbound request gets a correlation id. The id is propagated to downstream
calls in the `X-Correlation-Id` header. When a ticket includes an id, search
all service logs for it before reading individual stack traces.

## Deploying

1. Build and test the module.
2. Publish an immutable image tagged with the commit.
3. Deploy to one instance first.
4. Check health, error rate and latency for five minutes.
5. Roll out the rest.
6. Watch the business metric for the changed feature.

Rolling back means redeploying the previous image. Database changes must be
backwards compatible for one release so a rollback does not require a restore.

## Incident checklist

1. Declare the incident and assign a commander.
2. Identify the user-visible symptom, not the suspected cause.
3. Check the last deploy and the last configuration change.
4. Check dependencies: database, downstream services, payment provider.
5. Mitigate first: roll back, disable a feature flag or scale out.
6. Preserve evidence: correlation ids, log extracts, metric snapshots.
7. Write the timeline while it is fresh.
8. Follow up with a blameless review and concrete actions.

## Runbooks

- [High error rate](runbooks/high-error-rate.md)
- [Payment decline spike](runbooks/payment-decline-spike.md)
- [Stock reservation leak](runbooks/stock-reservation-leak.md)
- [Notification backlog](runbooks/notification-backlog.md)

## Maintenance windows

Schema changes that need a table rewrite are scheduled outside peak hours.
The release notes state the expected duration. During the window, writes to
the affected service are paused at the edge; reads continue from the old
schema.

## Capacity planning

Scale on latency and queue depth, not CPU alone. Each service has a known
requests-per-second profile at peak. When p95 latency rises while CPU is low,
look at the database and downstream calls before adding instances.
