# API reference

Every service exposes JSON over HTTP under the `/api` prefix. The documents
below list each resource, its fields, its endpoints and the rules that apply.

| Service | Reference | Resources |
| --- | --- | --- |
| Catalog | [catalog.md](catalog.md) | Products, variants, media, pricing and SEO |
| Inventory | [inventory.md](inventory.md) | Stock, locations, reservations and counting |
| Order | [order.md](order.md) | Carts, checkout, orders, returns and repeat purchase |
| Payment | [payment.md](payment.md) | Payments, refunds, payouts and the ledger |
| Customer | [customer.md](customer.md) | Customers, contact, consent, loyalty and support |
| Shipping | [shipping.md](shipping.md) | Carriers, labels, routes, delivery and customs |
| Notification | [notification.md](notification.md) | Messages, templates, campaigns and delivery |

## Reading order

1. Read [API conventions](../api-conventions.md) once. It defines verbs,
   status codes, error bodies, paging and naming.
2. Open the document for the service you are calling.
3. Read the resource section for the exact fields and rules.
4. Use the example as a starting point, then replace the values.

## Base URLs

Local development uses the port configured in each service's
`application.yml`:

| Service | Local base URL |
| --- | --- |
| Catalog | `http://localhost:8081` |
| Inventory | `http://localhost:8082` |
| Order | `http://localhost:8083` |
| Payment | `http://localhost:8084` |
| Customer | `http://localhost:8085` |
| Shipping | `http://localhost:8086` |
| Notification | `http://localhost:8087` |

Deployments supply the real addresses. Client code should receive them as
configuration, not hard-code a port.

## Shared resource contract

Every resource follows the same contract. Knowing it once is enough to call
any of them.

### Identity

| Field | Type | Editable | Notes |
| --- | --- | --- | --- |
| `id` | integer | No | Surrogate key, unique inside the service |
| `code` | string | No | Natural key, unique, uppercase, max 40 |
| `name` | string | Yes | Display name, max 160 |
| `status` | string | Via actions | `ACTIVE`, `INACTIVE` or `ARCHIVED` |
| `priority` | integer | Yes | Non-negative, higher sorts first |
| `amount` | number | Via action | Decimal, two places |
| `createdAt` | string | No | ISO-8601 instant in UTC |
| `updatedAt` | string | No | ISO-8601 instant in UTC |

### Status transitions

```
create ──▶ ACTIVE ──deactivate──▶ INACTIVE
             ▲                      │
             └──────activate────────┘

ACTIVE or INACTIVE ──archive──▶ ARCHIVED
ARCHIVED ──activate──▶ ACTIVE
```

Archive is the delete operation. An archived record stays readable and keeps
its history. Updates are rejected while it is archived, with
`409 NOT_ACTIVE`.

### Standard endpoints

```
POST   /api/<plural>
GET    /api/<plural>/{id}
GET    /api/<plural>/by-code/{code}
GET    /api/<plural>?text={text}
GET    /api/<plural>/top?limit={n}
GET    /api/<plural>/high-priority?minimum={n}
GET    /api/<plural>/all-active
GET    /api/<plural>/exists/{code}
PUT    /api/<plural>/{id}
POST   /api/<plural>/{id}/rename?name={name}
POST   /api/<plural>/{id}/amount?delta={amount}
POST   /api/<plural>/{id}/activate
POST   /api/<plural>/{id}/deactivate
DELETE /api/<plural>/{id}
GET    /api/<plural>/stats/active
GET    /api/<plural>/stats/archived
GET    /api/<plural>/stats/total
GET    /api/<plural>/stats/average
```

A resource that needs an extra action adds it as a `POST` sub-path. A resource
that needs a different read adds a `GET` sub-path. The standard set is never
removed, because clients depend on it.

### Error body

```json
{
  "code": "DUPLICATE_CODE",
  "message": "That code is already in use: SKU-001"
}
```

| Status | Meaning |
| --- | --- |
| `400` | The request body failed validation |
| `404` | No record for the id or code |
| `409` | A business rule rejected the change |
| `500` | An unexpected failure; logged with a correlation id |

## Trying a request

```bash
# Create
curl -X POST http://localhost:8081/api/products \
  -H 'Content-Type: application/json' \
  -d '{"code":"SKU-001","name":"Trail jacket","priority":5,"amount":129.00}'

# Read
curl http://localhost:8081/api/products/by-code/SKU-001

# Search
curl 'http://localhost:8081/api/products?text=jacket'

# Archive
curl -X DELETE http://localhost:8081/api/products/1
```

## Contract changes

A change is compatible when it only adds an optional request field or a new
response field. Renaming, removing or retyping a field is breaking and needs a
new path plus a deprecation window. See
[ADR 0005](../adr/0005-error-codes.md) for the error-code rule and
[API conventions](../api-conventions.md) for the full compatibility list.
