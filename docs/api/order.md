# Order API

`order-service` owns carts, checkout, orders, returns and repeat purchase.

Base path: `/api`. All payloads are JSON and all timestamps are ISO-8601
instants in UTC. See [API conventions](../api-conventions.md) for the rules
that apply to every resource.

## Resource shape

Every resource below shares the same identity and lifecycle. The common
fields are:

| Field | Type | Notes |
| --- | --- | --- |
| `id` | integer | Surrogate key. Never shared with another service. |
| `code` | string | Natural key, unique and uppercase, at most 40 characters. |
| `name` | string | Display name, at most 160 characters. |
| `status` | string | `ACTIVE`, `INACTIVE` or `ARCHIVED`. |
| `priority` | integer | Non-negative ordering hint; higher sorts first. |
| `amount` | number | Decimal with two places. |
| `createdAt` | string | Creation instant. |
| `updatedAt` | string | Last update instant. |

A create body carries `code`, `name`, `priority`, `amount` and the
resource-specific fields. A successful create returns `201` with the full
resource. A duplicate `code` returns `409 DUPLICATE_CODE`.

## Common endpoints

For a resource at `/api/<plural>`, the following endpoints always exist. The
`<plural>` value is shown in each section below.

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/<plural>` | Create a record. |
| `GET` | `/api/<plural>/{id}` | Read by id. |
| `GET` | `/api/<plural>/by-code/{code}` | Read by natural key. |
| `GET` | `/api/<plural>?text=` | Search by name. |
| `GET` | `/api/<plural>/top?limit=` | Top active records by priority. |
| `GET` | `/api/<plural>/high-priority?minimum=` | Records at or above a priority. |
| `GET` | `/api/<plural>/all-active` | Every active record. |
| `GET` | `/api/<plural>/exists/{code}` | Existence check. |
| `PUT` | `/api/<plural>/{id}` | Update the editable fields. |
| `POST` | `/api/<plural>/{id}/rename` | Rename. |
| `POST` | `/api/<plural>/{id}/amount` | Adjust the amount. |
| `POST` | `/api/<plural>/{id}/activate` | Activate. |
| `POST` | `/api/<plural>/{id}/deactivate` | Deactivate. |
| `DELETE` | `/api/<plural>/{id}` | Archive. |
| `GET` | `/api/<plural>/stats/active` | Active count. |
| `GET` | `/api/<plural>/stats/archived` | Archived count. |
| `GET` | `/api/<plural>/stats/total` | Active amount total. |
| `GET` | `/api/<plural>/stats/average` | Active amount average. |

An update against an archived record returns `409 NOT_ACTIVE`. An unknown id
returns `404 NOT_FOUND`. Archive is idempotent: a second archive keeps the
record archived and still returns `204`.

## Errors

| Status | Code | Cause |
| --- | --- | --- |
| `400` | validation message | Missing or invalid body field |
| `404` | `NOT_FOUND` | Unknown id or code |
| `409` | `DUPLICATE_CODE` | Natural key already exists |
| `409` | `NOT_ACTIVE` | Update against an archived record |

## Wishlist

Base path: `/wishlists`.

A saved set of items, private unless shared with a token.

### Fields

| Field | Type | Notes |
| --- | --- | --- |
| `id` | integer | Surrogate database key. Read-only. |
| `code` | string | Natural key, unique and uppercase, at most 40 characters. |
| `name` | string | Display name, at most 160 characters. |
| `status` | string | `ACTIVE`, `INACTIVE` or `ARCHIVED`. Read-only. |
| `priority` | integer | Ordering hint, non-negative, higher first. |
| `amount` | number | Decimal amount with two places. |
| `createdAt` | string | Creation instant in UTC. Read-only. |
| `updatedAt` | string | Last update instant in UTC. Read-only. |
| `customerRef` | string | Text value, at most 120 characters. |
| `itemCount` | integer | Whole number. |
| `shared` | boolean | True or false flag. |
| `shareToken` | string | Text value, at most 120 characters. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/wishlists` | Create a record. Returns `201`. |
| `GET` | `/api/wishlists/{id}` | Read one record by id. |
| `GET` | `/api/wishlists/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/wishlists?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/wishlists/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/wishlists/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/wishlists/all-active` | Every active record. |
| `GET` | `/api/wishlists/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/wishlists/{id}` | Replace the editable fields. |
| `POST` | `/api/wishlists/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/wishlists/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/wishlists/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/wishlists/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/wishlists/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/wishlists/stats/active` | Count of active records. |
| `GET` | `/api/wishlists/stats/archived` | Count of archived records. |
| `GET` | `/api/wishlists/stats/total` | Sum of active amounts. |
| `GET` | `/api/wishlists/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/wishlists \
  -H 'Content-Type: application/json' \
  -d '{"code":"SAMPLE-1","name":"Sample","priority":3,"amount":10.00}'
```

```json
{
  "id": 1,
  "code": "SAMPLE-1",
  "name": "Sample",
  "status": "ACTIVE",
  "priority": 3,
  "amount": 10.00,
  "createdAt": "2024-05-01T09:30:00Z",
  "updatedAt": "2024-05-01T09:30:00Z"
}
```

### Rules

- `code` is unique and is normalised to uppercase before storage.
- `priority` must not be negative.
- An archived record cannot be updated, renamed or adjusted.
- Activation and deactivation are explicit actions, never implicit writes.
- Amount changes use a signed delta and are applied inside one transaction.

## Checkout Session

Base path: `/checkout-sessions`.

A single checkout attempt that expires and can complete once.

### Fields

| Field | Type | Notes |
| --- | --- | --- |
| `id` | integer | Surrogate database key. Read-only. |
| `code` | string | Natural key, unique and uppercase, at most 40 characters. |
| `name` | string | Display name, at most 160 characters. |
| `status` | string | `ACTIVE`, `INACTIVE` or `ARCHIVED`. Read-only. |
| `priority` | integer | Ordering hint, non-negative, higher first. |
| `amount` | number | Decimal amount with two places. |
| `createdAt` | string | Creation instant in UTC. Read-only. |
| `updatedAt` | string | Last update instant in UTC. Read-only. |
| `customerRef` | string | Text value, at most 120 characters. |
| `cartRef` | string | Text value, at most 120 characters. |
| `expiresAt` | string | ISO-8601 instant in UTC. |
| `completed` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/checkout-sessions` | Create a record. Returns `201`. |
| `GET` | `/api/checkout-sessions/{id}` | Read one record by id. |
| `GET` | `/api/checkout-sessions/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/checkout-sessions?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/checkout-sessions/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/checkout-sessions/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/checkout-sessions/all-active` | Every active record. |
| `GET` | `/api/checkout-sessions/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/checkout-sessions/{id}` | Replace the editable fields. |
| `POST` | `/api/checkout-sessions/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/checkout-sessions/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/checkout-sessions/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/checkout-sessions/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/checkout-sessions/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/checkout-sessions/stats/active` | Count of active records. |
| `GET` | `/api/checkout-sessions/stats/archived` | Count of archived records. |
| `GET` | `/api/checkout-sessions/stats/total` | Sum of active amounts. |
| `GET` | `/api/checkout-sessions/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/checkout-sessions \
  -H 'Content-Type: application/json' \
  -d '{"code":"SAMPLE-1","name":"Sample","priority":3,"amount":10.00}'
```

```json
{
  "id": 1,
  "code": "SAMPLE-1",
  "name": "Sample",
  "status": "ACTIVE",
  "priority": 3,
  "amount": 10.00,
  "createdAt": "2024-05-01T09:30:00Z",
  "updatedAt": "2024-05-01T09:30:00Z"
}
```

### Rules

- `code` is unique and is normalised to uppercase before storage.
- `priority` must not be negative.
- An archived record cannot be updated, renamed or adjusted.
- Activation and deactivation are explicit actions, never implicit writes.
- Amount changes use a signed delta and are applied inside one transaction.

## Order Note

Base path: `/order-notes`.

An internal note attached to an order, with its author.

### Fields

| Field | Type | Notes |
| --- | --- | --- |
| `id` | integer | Surrogate database key. Read-only. |
| `code` | string | Natural key, unique and uppercase, at most 40 characters. |
| `name` | string | Display name, at most 160 characters. |
| `status` | string | `ACTIVE`, `INACTIVE` or `ARCHIVED`. Read-only. |
| `priority` | integer | Ordering hint, non-negative, higher first. |
| `amount` | number | Decimal amount with two places. |
| `createdAt` | string | Creation instant in UTC. Read-only. |
| `updatedAt` | string | Last update instant in UTC. Read-only. |
| `orderRef` | string | Text value, at most 120 characters. |
| `author` | string | Text value, at most 120 characters. |
| `body` | string | Text value, at most 120 characters. |
| `pinned` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/order-notes` | Create a record. Returns `201`. |
| `GET` | `/api/order-notes/{id}` | Read one record by id. |
| `GET` | `/api/order-notes/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/order-notes?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/order-notes/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/order-notes/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/order-notes/all-active` | Every active record. |
| `GET` | `/api/order-notes/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/order-notes/{id}` | Replace the editable fields. |
| `POST` | `/api/order-notes/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/order-notes/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/order-notes/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/order-notes/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/order-notes/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/order-notes/stats/active` | Count of active records. |
| `GET` | `/api/order-notes/stats/archived` | Count of archived records. |
| `GET` | `/api/order-notes/stats/total` | Sum of active amounts. |
| `GET` | `/api/order-notes/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/order-notes \
  -H 'Content-Type: application/json' \
  -d '{"code":"SAMPLE-1","name":"Sample","priority":3,"amount":10.00}'
```

```json
{
  "id": 1,
  "code": "SAMPLE-1",
  "name": "Sample",
  "status": "ACTIVE",
  "priority": 3,
  "amount": 10.00,
  "createdAt": "2024-05-01T09:30:00Z",
  "updatedAt": "2024-05-01T09:30:00Z"
}
```

### Rules

- `code` is unique and is normalised to uppercase before storage.
- `priority` must not be negative.
- An archived record cannot be updated, renamed or adjusted.
- Activation and deactivation are explicit actions, never implicit writes.
- Amount changes use a signed delta and are applied inside one transaction.

## Shipment Tracking

Base path: `/shipment-trackings`.

Customer-facing tracking for an order, with carrier and latest event.

### Fields

| Field | Type | Notes |
| --- | --- | --- |
| `id` | integer | Surrogate database key. Read-only. |
| `code` | string | Natural key, unique and uppercase, at most 40 characters. |
| `name` | string | Display name, at most 160 characters. |
| `status` | string | `ACTIVE`, `INACTIVE` or `ARCHIVED`. Read-only. |
| `priority` | integer | Ordering hint, non-negative, higher first. |
| `amount` | number | Decimal amount with two places. |
| `createdAt` | string | Creation instant in UTC. Read-only. |
| `updatedAt` | string | Last update instant in UTC. Read-only. |
| `orderRef` | string | Text value, at most 120 characters. |
| `trackingNumber` | string | Text value, at most 120 characters. |
| `carrier` | string | Text value, at most 120 characters. |
| `lastEventAt` | string | ISO-8601 instant in UTC. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/shipment-trackings` | Create a record. Returns `201`. |
| `GET` | `/api/shipment-trackings/{id}` | Read one record by id. |
| `GET` | `/api/shipment-trackings/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/shipment-trackings?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/shipment-trackings/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/shipment-trackings/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/shipment-trackings/all-active` | Every active record. |
| `GET` | `/api/shipment-trackings/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/shipment-trackings/{id}` | Replace the editable fields. |
| `POST` | `/api/shipment-trackings/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/shipment-trackings/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/shipment-trackings/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/shipment-trackings/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/shipment-trackings/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/shipment-trackings/stats/active` | Count of active records. |
| `GET` | `/api/shipment-trackings/stats/archived` | Count of archived records. |
| `GET` | `/api/shipment-trackings/stats/total` | Sum of active amounts. |
| `GET` | `/api/shipment-trackings/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/shipment-trackings \
  -H 'Content-Type: application/json' \
  -d '{"code":"SAMPLE-1","name":"Sample","priority":3,"amount":10.00}'
```

```json
{
  "id": 1,
  "code": "SAMPLE-1",
  "name": "Sample",
  "status": "ACTIVE",
  "priority": 3,
  "amount": 10.00,
  "createdAt": "2024-05-01T09:30:00Z",
  "updatedAt": "2024-05-01T09:30:00Z"
}
```

### Rules

- `code` is unique and is normalised to uppercase before storage.
- `priority` must not be negative.
- An archived record cannot be updated, renamed or adjusted.
- Activation and deactivation are explicit actions, never implicit writes.
- Amount changes use a signed delta and are applied inside one transaction.

## Gift Card

Base path: `/gift-cards`.

Stored value with a card number, currency and expiry.

### Fields

| Field | Type | Notes |
| --- | --- | --- |
| `id` | integer | Surrogate database key. Read-only. |
| `code` | string | Natural key, unique and uppercase, at most 40 characters. |
| `name` | string | Display name, at most 160 characters. |
| `status` | string | `ACTIVE`, `INACTIVE` or `ARCHIVED`. Read-only. |
| `priority` | integer | Ordering hint, non-negative, higher first. |
| `amount` | number | Decimal amount with two places. |
| `createdAt` | string | Creation instant in UTC. Read-only. |
| `updatedAt` | string | Last update instant in UTC. Read-only. |
| `cardNumber` | string | Text value, at most 120 characters. |
| `currency` | string | Text value, at most 120 characters. |
| `expiresAt` | string | ISO-8601 instant in UTC. |
| `redeemed` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/gift-cards` | Create a record. Returns `201`. |
| `GET` | `/api/gift-cards/{id}` | Read one record by id. |
| `GET` | `/api/gift-cards/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/gift-cards?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/gift-cards/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/gift-cards/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/gift-cards/all-active` | Every active record. |
| `GET` | `/api/gift-cards/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/gift-cards/{id}` | Replace the editable fields. |
| `POST` | `/api/gift-cards/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/gift-cards/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/gift-cards/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/gift-cards/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/gift-cards/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/gift-cards/stats/active` | Count of active records. |
| `GET` | `/api/gift-cards/stats/archived` | Count of archived records. |
| `GET` | `/api/gift-cards/stats/total` | Sum of active amounts. |
| `GET` | `/api/gift-cards/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/gift-cards \
  -H 'Content-Type: application/json' \
  -d '{"code":"SAMPLE-1","name":"Sample","priority":3,"amount":10.00}'
```

```json
{
  "id": 1,
  "code": "SAMPLE-1",
  "name": "Sample",
  "status": "ACTIVE",
  "priority": 3,
  "amount": 10.00,
  "createdAt": "2024-05-01T09:30:00Z",
  "updatedAt": "2024-05-01T09:30:00Z"
}
```

### Rules

- `code` is unique and is normalised to uppercase before storage.
- `priority` must not be negative.
- An archived record cannot be updated, renamed or adjusted.
- Activation and deactivation are explicit actions, never implicit writes.
- Amount changes use a signed delta and are applied inside one transaction.

## Cancel Request

Base path: `/cancel-requests`.

A request to cancel an order, with reason and approval.

### Fields

| Field | Type | Notes |
| --- | --- | --- |
| `id` | integer | Surrogate database key. Read-only. |
| `code` | string | Natural key, unique and uppercase, at most 40 characters. |
| `name` | string | Display name, at most 160 characters. |
| `status` | string | `ACTIVE`, `INACTIVE` or `ARCHIVED`. Read-only. |
| `priority` | integer | Ordering hint, non-negative, higher first. |
| `amount` | number | Decimal amount with two places. |
| `createdAt` | string | Creation instant in UTC. Read-only. |
| `updatedAt` | string | Last update instant in UTC. Read-only. |
| `orderRef` | string | Text value, at most 120 characters. |
| `reason` | string | Text value, at most 120 characters. |
| `requestedBy` | string | Text value, at most 120 characters. |
| `approved` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/cancel-requests` | Create a record. Returns `201`. |
| `GET` | `/api/cancel-requests/{id}` | Read one record by id. |
| `GET` | `/api/cancel-requests/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/cancel-requests?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/cancel-requests/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/cancel-requests/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/cancel-requests/all-active` | Every active record. |
| `GET` | `/api/cancel-requests/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/cancel-requests/{id}` | Replace the editable fields. |
| `POST` | `/api/cancel-requests/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/cancel-requests/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/cancel-requests/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/cancel-requests/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/cancel-requests/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/cancel-requests/stats/active` | Count of active records. |
| `GET` | `/api/cancel-requests/stats/archived` | Count of archived records. |
| `GET` | `/api/cancel-requests/stats/total` | Sum of active amounts. |
| `GET` | `/api/cancel-requests/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/cancel-requests \
  -H 'Content-Type: application/json' \
  -d '{"code":"SAMPLE-1","name":"Sample","priority":3,"amount":10.00}'
```

```json
{
  "id": 1,
  "code": "SAMPLE-1",
  "name": "Sample",
  "status": "ACTIVE",
  "priority": 3,
  "amount": 10.00,
  "createdAt": "2024-05-01T09:30:00Z",
  "updatedAt": "2024-05-01T09:30:00Z"
}
```

### Rules

- `code` is unique and is normalised to uppercase before storage.
- `priority` must not be negative.
- An archived record cannot be updated, renamed or adjusted.
- Activation and deactivation are explicit actions, never implicit writes.
- Amount changes use a signed delta and are applied inside one transaction.

## Exchange

Base path: `/exchanges`.

A replacement workflow referencing the original and replacement SKU.

### Fields

| Field | Type | Notes |
| --- | --- | --- |
| `id` | integer | Surrogate database key. Read-only. |
| `code` | string | Natural key, unique and uppercase, at most 40 characters. |
| `name` | string | Display name, at most 160 characters. |
| `status` | string | `ACTIVE`, `INACTIVE` or `ARCHIVED`. Read-only. |
| `priority` | integer | Ordering hint, non-negative, higher first. |
| `amount` | number | Decimal amount with two places. |
| `createdAt` | string | Creation instant in UTC. Read-only. |
| `updatedAt` | string | Last update instant in UTC. Read-only. |
| `orderRef` | string | Text value, at most 120 characters. |
| `originalSku` | string | Text value, at most 120 characters. |
| `replacementSku` | string | Text value, at most 120 characters. |
| `shipped` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/exchanges` | Create a record. Returns `201`. |
| `GET` | `/api/exchanges/{id}` | Read one record by id. |
| `GET` | `/api/exchanges/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/exchanges?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/exchanges/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/exchanges/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/exchanges/all-active` | Every active record. |
| `GET` | `/api/exchanges/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/exchanges/{id}` | Replace the editable fields. |
| `POST` | `/api/exchanges/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/exchanges/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/exchanges/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/exchanges/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/exchanges/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/exchanges/stats/active` | Count of active records. |
| `GET` | `/api/exchanges/stats/archived` | Count of archived records. |
| `GET` | `/api/exchanges/stats/total` | Sum of active amounts. |
| `GET` | `/api/exchanges/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/exchanges \
  -H 'Content-Type: application/json' \
  -d '{"code":"SAMPLE-1","name":"Sample","priority":3,"amount":10.00}'
```

```json
{
  "id": 1,
  "code": "SAMPLE-1",
  "name": "Sample",
  "status": "ACTIVE",
  "priority": 3,
  "amount": 10.00,
  "createdAt": "2024-05-01T09:30:00Z",
  "updatedAt": "2024-05-01T09:30:00Z"
}
```

### Rules

- `code` is unique and is normalised to uppercase before storage.
- `priority` must not be negative.
- An archived record cannot be updated, renamed or adjusted.
- Activation and deactivation are explicit actions, never implicit writes.
- Amount changes use a signed delta and are applied inside one transaction.

## Preorder

Base path: `/preorders`.

An order placed before a product release, with a deposit.

### Fields

| Field | Type | Notes |
| --- | --- | --- |
| `id` | integer | Surrogate database key. Read-only. |
| `code` | string | Natural key, unique and uppercase, at most 40 characters. |
| `name` | string | Display name, at most 160 characters. |
| `status` | string | `ACTIVE`, `INACTIVE` or `ARCHIVED`. Read-only. |
| `priority` | integer | Ordering hint, non-negative, higher first. |
| `amount` | number | Decimal amount with two places. |
| `createdAt` | string | Creation instant in UTC. Read-only. |
| `updatedAt` | string | Last update instant in UTC. Read-only. |
| `orderRef` | string | Text value, at most 120 characters. |
| `productCode` | string | Text value, at most 120 characters. |
| `releaseDate` | string | ISO-8601 instant in UTC. |
| `deposit` | number | Decimal amount with two decimal places. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/preorders` | Create a record. Returns `201`. |
| `GET` | `/api/preorders/{id}` | Read one record by id. |
| `GET` | `/api/preorders/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/preorders?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/preorders/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/preorders/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/preorders/all-active` | Every active record. |
| `GET` | `/api/preorders/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/preorders/{id}` | Replace the editable fields. |
| `POST` | `/api/preorders/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/preorders/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/preorders/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/preorders/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/preorders/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/preorders/stats/active` | Count of active records. |
| `GET` | `/api/preorders/stats/archived` | Count of archived records. |
| `GET` | `/api/preorders/stats/total` | Sum of active amounts. |
| `GET` | `/api/preorders/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/preorders \
  -H 'Content-Type: application/json' \
  -d '{"code":"SAMPLE-1","name":"Sample","priority":3,"amount":10.00}'
```

```json
{
  "id": 1,
  "code": "SAMPLE-1",
  "name": "Sample",
  "status": "ACTIVE",
  "priority": 3,
  "amount": 10.00,
  "createdAt": "2024-05-01T09:30:00Z",
  "updatedAt": "2024-05-01T09:30:00Z"
}
```

### Rules

- `code` is unique and is normalised to uppercase before storage.
- `priority` must not be negative.
- An archived record cannot be updated, renamed or adjusted.
- Activation and deactivation are explicit actions, never implicit writes.
- Amount changes use a signed delta and are applied inside one transaction.

## Layaway

Base path: `/layaways`.

An instalment purchase with a paid amount that never exceeds the total.

### Fields

| Field | Type | Notes |
| --- | --- | --- |
| `id` | integer | Surrogate database key. Read-only. |
| `code` | string | Natural key, unique and uppercase, at most 40 characters. |
| `name` | string | Display name, at most 160 characters. |
| `status` | string | `ACTIVE`, `INACTIVE` or `ARCHIVED`. Read-only. |
| `priority` | integer | Ordering hint, non-negative, higher first. |
| `amount` | number | Decimal amount with two places. |
| `createdAt` | string | Creation instant in UTC. Read-only. |
| `updatedAt` | string | Last update instant in UTC. Read-only. |
| `customerRef` | string | Text value, at most 120 characters. |
| `orderRef` | string | Text value, at most 120 characters. |
| `installmentCount` | integer | Whole number. |
| `paidAmount` | number | Decimal amount with two decimal places. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/layaways` | Create a record. Returns `201`. |
| `GET` | `/api/layaways/{id}` | Read one record by id. |
| `GET` | `/api/layaways/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/layaways?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/layaways/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/layaways/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/layaways/all-active` | Every active record. |
| `GET` | `/api/layaways/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/layaways/{id}` | Replace the editable fields. |
| `POST` | `/api/layaways/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/layaways/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/layaways/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/layaways/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/layaways/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/layaways/stats/active` | Count of active records. |
| `GET` | `/api/layaways/stats/archived` | Count of archived records. |
| `GET` | `/api/layaways/stats/total` | Sum of active amounts. |
| `GET` | `/api/layaways/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/layaways \
  -H 'Content-Type: application/json' \
  -d '{"code":"SAMPLE-1","name":"Sample","priority":3,"amount":10.00}'
```

```json
{
  "id": 1,
  "code": "SAMPLE-1",
  "name": "Sample",
  "status": "ACTIVE",
  "priority": 3,
  "amount": 10.00,
  "createdAt": "2024-05-01T09:30:00Z",
  "updatedAt": "2024-05-01T09:30:00Z"
}
```

### Rules

- `code` is unique and is normalised to uppercase before storage.
- `priority` must not be negative.
- An archived record cannot be updated, renamed or adjusted.
- Activation and deactivation are explicit actions, never implicit writes.
- Amount changes use a signed delta and are applied inside one transaction.

## Order Approval

Base path: `/order-approvals`.

An approval decision for a sensitive order.

### Fields

| Field | Type | Notes |
| --- | --- | --- |
| `id` | integer | Surrogate database key. Read-only. |
| `code` | string | Natural key, unique and uppercase, at most 40 characters. |
| `name` | string | Display name, at most 160 characters. |
| `status` | string | `ACTIVE`, `INACTIVE` or `ARCHIVED`. Read-only. |
| `priority` | integer | Ordering hint, non-negative, higher first. |
| `amount` | number | Decimal amount with two places. |
| `createdAt` | string | Creation instant in UTC. Read-only. |
| `updatedAt` | string | Last update instant in UTC. Read-only. |
| `orderRef` | string | Text value, at most 120 characters. |
| `approver` | string | Text value, at most 120 characters. |
| `decidedAt` | string | ISO-8601 instant in UTC. |
| `approved` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/order-approvals` | Create a record. Returns `201`. |
| `GET` | `/api/order-approvals/{id}` | Read one record by id. |
| `GET` | `/api/order-approvals/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/order-approvals?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/order-approvals/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/order-approvals/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/order-approvals/all-active` | Every active record. |
| `GET` | `/api/order-approvals/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/order-approvals/{id}` | Replace the editable fields. |
| `POST` | `/api/order-approvals/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/order-approvals/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/order-approvals/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/order-approvals/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/order-approvals/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/order-approvals/stats/active` | Count of active records. |
| `GET` | `/api/order-approvals/stats/archived` | Count of archived records. |
| `GET` | `/api/order-approvals/stats/total` | Sum of active amounts. |
| `GET` | `/api/order-approvals/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/order-approvals \
  -H 'Content-Type: application/json' \
  -d '{"code":"SAMPLE-1","name":"Sample","priority":3,"amount":10.00}'
```

```json
{
  "id": 1,
  "code": "SAMPLE-1",
  "name": "Sample",
  "status": "ACTIVE",
  "priority": 3,
  "amount": 10.00,
  "createdAt": "2024-05-01T09:30:00Z",
  "updatedAt": "2024-05-01T09:30:00Z"
}
```

### Rules

- `code` is unique and is normalised to uppercase before storage.
- `priority` must not be negative.
- An archived record cannot be updated, renamed or adjusted.
- Activation and deactivation are explicit actions, never implicit writes.
- Amount changes use a signed delta and are applied inside one transaction.

## Consistency notes

- Writes are transactional. A failed write leaves no partial record.
- Each mutable row carries a version column. A concurrent update fails with a
  conflict instead of silently overwriting another writer.
- Reads are read-only transactions and never lock rows.
- A list response is bounded. Callers that need everything should page rather
  than request an unbounded list in production.
- Amounts are decimal values, never floating-point numbers.

## Testing this contract

Each resource has a service test for the rules and a standalone web test for
the transport. Run them with:

```bash
mvn -pl order-service -am test
```
