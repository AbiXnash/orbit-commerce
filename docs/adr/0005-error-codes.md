# ADR 0005: Stable error codes mapped to HTTP

## Status

Accepted.

## Context

Clients need to distinguish "not found" from "archived" from "duplicate code"
without parsing prose. Different services were returning different shapes.

## Decision

Every business failure throws a domain exception carrying an `ErrorCode`.
A shared handler maps the exception to a stable body:

```json
{
  "code": "DUPLICATE_CODE",
  "message": "That code is already in use: SKU-001"
}
```

Mapping:

| Error code | HTTP |
| --- | --- |
| `NOT_FOUND` | 404 |
| `INVALID_REQUEST` | 400 |
| `DUPLICATE_CODE` | 409 |
| `NOT_ACTIVE` | 409 |
| `PAYMENT_DECLINED` | 409 |
| `OUT_OF_STOCK` | 409 |

Unexpected exceptions are not translated into business codes. They surface as
500 and are logged once.

## Consequences

Benefits:

- Clients branch on a stable code, not a message.
- The message can improve without breaking a client.
- Tests assert the code and the status.

Costs:

- New failures need a new code and a deliberate HTTP mapping.
- The enum grows; codes are never removed because clients may still read them.
