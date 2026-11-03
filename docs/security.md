# Security

Commerce systems hold money, identity and location data. The defaults below
apply to every service.

## Principles

1. Collect the minimum data needed to operate.
2. Never store card data; store a provider token and the last four digits.
3. Log identifiers and outcomes, never secrets or full personal records.
4. Authenticate at the edge, authorise at the service.
5. Prefer short-lived credentials and rotation over long-lived secrets.
6. Fail closed: a missing permission is a denial, not a bypass.

## Authentication

The storefront authenticates customers at the edge. The edge forwards a signed
identity to services. Services trust the signature, not a header that a client
could set.

Internal calls use service credentials scoped to the calling service. A service
may only call the endpoints it needs.

## Authorisation

Authorization is checked in the service that owns the record:

| Actor | Allowed |
| --- | --- |
| Customer | Own profile, own orders, own returns |
| Support agent | Customer records for an open ticket, with audit |
| Operations | Inventory, fulfilment, shipping state |
| Finance | Payments, refunds, settlements, ledger |
| Admin | Configuration, not routine business data |

Every privileged read or write is audited with actor, action, target and time.
An audit record is append-only.

## Secrets

- Secrets come from the platform secret store, never from source.
- A secret is scoped to one service and one environment.
- Secrets are rotated on a schedule and immediately after exposure.
- A secret must never appear in logs, error messages, test fixtures or docs.
- The repository's tests use obvious dummy values, never real credentials.

If a secret is committed, treat it as compromised: rotate first, then remove it
from history. Removing the commit alone is not enough.

## Personal data

| Data | Class | Handling |
| --- | --- | --- |
| Name | Personal | Needed for fulfilment |
| Email | Personal | Needed for receipts and support |
| Phone | Personal | Needed for delivery |
| Address | Personal | Needed for delivery and tax |
| IP address | Personal | Short retention, security only |
| Card number | Prohibited | Never stored |
| Card token | Sensitive | Encrypted at rest |
| Password | Secret | Hashed with a memory-hard function |
| Consent | Sensitive | Retained as proof |

PII is masked in logs. Error messages name the field, not the value.

## Payments

- Card data is collected by the provider's hosted field, not by our services.
- Our services see a token and the last four digits.
- Payment requests carry an idempotency key.
- Refunds are checked against the captured amount.
- Ledger entries are append-only and balanced.
- Chargebacks and disputes are recorded with their evidence reference.

## Transport

- TLS everywhere, including service-to-service calls.
- Certificate validation is never disabled, including in tests.
- Internal endpoints are not exposed to the public network.
- Webhooks are signed; the receiver verifies the signature before acting.

## Input handling

- Validate length, range and format at the edge.
- Use parameterised queries only; no string-built SQL.
- Reject unknown enum values rather than defaulting.
- Bound list sizes and page sizes.
- Do not deserialise arbitrary classes.

## Dependency hygiene

- Dependencies are pinned by the parent BOM.
- A new dependency needs a reason and a licence check.
- Security advisories are reviewed weekly.
- A critical advisory is patched before the next feature release.

## Threat model summary

| Threat | Mitigation |
| --- | --- |
| Credential stuffing | Edge rate limits, provider bot defence |
| Card theft | No card data stored; provider tokens only |
| IDOR | Ownership check in the owning service |
| Replay of payment | Idempotency key and recorded result |
| Log leakage | Structured logging with masking |
| Supply chain | Pinned versions, advisory review |
| Insider misuse | Least privilege plus append-only audit |
| Data export abuse | Bounded pages, rate limits, audit |

## Incident response

1. Contain: revoke the credential or disable the path.
2. Assess: which records and which customers are affected.
3. Notify: follow the legal and contractual timeline.
4. Eradicate: remove the cause, not just the symptom.
5. Recover: restore service and verify integrity.
6. Review: blameless post-incident actions with owners.
