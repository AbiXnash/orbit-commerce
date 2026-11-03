# Catalog API

`catalog-service` owns products, variants, merchandising, prices and search metadata.

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
| `400` | `INVALID_REQUEST` | Missing, malformed or invalid request value |
| `404` | `NOT_FOUND` | Unknown id or code |
| `409` | `DUPLICATE_CODE` | Natural key already exists |
| `409` | `NOT_ACTIVE` | Update against an archived record |

## Product Variant

Base path: `/product-variants`.

A concrete option combination of a product, with its own SKU suffix, barcode and option values.

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
| `skuSuffix` | string | Text value, at most 120 characters. |
| `optionName` | string | Text value, at most 120 characters. |
| `optionValue` | string | Text value, at most 120 characters. |
| `barcode` | string | Text value, at most 120 characters. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/product-variants` | Create a record. Returns `201`. |
| `GET` | `/api/product-variants/{id}` | Read one record by id. |
| `GET` | `/api/product-variants/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/product-variants?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/product-variants/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/product-variants/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/product-variants/all-active` | Every active record. |
| `GET` | `/api/product-variants/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/product-variants/{id}` | Replace the editable fields. |
| `POST` | `/api/product-variants/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/product-variants/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/product-variants/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/product-variants/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/product-variants/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/product-variants/stats/active` | Count of active records. |
| `GET` | `/api/product-variants/stats/archived` | Count of archived records. |
| `GET` | `/api/product-variants/stats/total` | Sum of active amounts. |
| `GET` | `/api/product-variants/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/product-variants \
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

## Product Image

Base path: `/product-images`.

An ordered image attached to a product, with alt text and a content type.

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
| `url` | string | Text value, at most 120 characters. |
| `altText` | string | Text value, at most 120 characters. |
| `contentType` | string | Text value, at most 120 characters. |
| `position` | integer | Whole number. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/product-images` | Create a record. Returns `201`. |
| `GET` | `/api/product-images/{id}` | Read one record by id. |
| `GET` | `/api/product-images/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/product-images?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/product-images/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/product-images/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/product-images/all-active` | Every active record. |
| `GET` | `/api/product-images/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/product-images/{id}` | Replace the editable fields. |
| `POST` | `/api/product-images/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/product-images/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/product-images/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/product-images/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/product-images/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/product-images/stats/active` | Count of active records. |
| `GET` | `/api/product-images/stats/archived` | Count of archived records. |
| `GET` | `/api/product-images/stats/total` | Sum of active amounts. |
| `GET` | `/api/product-images/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/product-images \
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

## Product Tag

Base path: `/product-tags`.

A free-form label used for filtering and display, with a stable slug and colour.

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
| `slug` | string | Text value, at most 120 characters. |
| `description` | string | Text value, at most 120 characters. |
| `colorHex` | string | Text value, at most 120 characters. |
| `featured` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/product-tags` | Create a record. Returns `201`. |
| `GET` | `/api/product-tags/{id}` | Read one record by id. |
| `GET` | `/api/product-tags/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/product-tags?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/product-tags/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/product-tags/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/product-tags/all-active` | Every active record. |
| `GET` | `/api/product-tags/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/product-tags/{id}` | Replace the editable fields. |
| `POST` | `/api/product-tags/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/product-tags/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/product-tags/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/product-tags/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/product-tags/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/product-tags/stats/active` | Count of active records. |
| `GET` | `/api/product-tags/stats/archived` | Count of archived records. |
| `GET` | `/api/product-tags/stats/total` | Sum of active amounts. |
| `GET` | `/api/product-tags/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/product-tags \
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

## Price Book

Base path: `/price-books`.

A price version for a market, channel and effective date.

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
| `currency` | string | Text value, at most 120 characters. |
| `region` | string | Text value, at most 120 characters. |
| `channel` | string | Text value, at most 120 characters. |
| `effectiveFrom` | string | ISO-8601 instant in UTC. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/price-books` | Create a record. Returns `201`. |
| `GET` | `/api/price-books/{id}` | Read one record by id. |
| `GET` | `/api/price-books/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/price-books?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/price-books/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/price-books/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/price-books/all-active` | Every active record. |
| `GET` | `/api/price-books/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/price-books/{id}` | Replace the editable fields. |
| `POST` | `/api/price-books/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/price-books/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/price-books/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/price-books/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/price-books/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/price-books/stats/active` | Count of active records. |
| `GET` | `/api/price-books/stats/archived` | Count of archived records. |
| `GET` | `/api/price-books/stats/total` | Sum of active amounts. |
| `GET` | `/api/price-books/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/price-books \
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

## Discount Rule

Base path: `/discount-rules`.

An automatic discount with an optional coupon, percentage and stacking flag.

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
| `couponCode` | string | Text value, at most 120 characters. |
| `percentage` | number | Decimal amount with two decimal places. |
| `minQuantity` | integer | Whole number. |
| `stackable` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/discount-rules` | Create a record. Returns `201`. |
| `GET` | `/api/discount-rules/{id}` | Read one record by id. |
| `GET` | `/api/discount-rules/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/discount-rules?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/discount-rules/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/discount-rules/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/discount-rules/all-active` | Every active record. |
| `GET` | `/api/discount-rules/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/discount-rules/{id}` | Replace the editable fields. |
| `POST` | `/api/discount-rules/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/discount-rules/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/discount-rules/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/discount-rules/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/discount-rules/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/discount-rules/stats/active` | Count of active records. |
| `GET` | `/api/discount-rules/stats/archived` | Count of archived records. |
| `GET` | `/api/discount-rules/stats/total` | Sum of active amounts. |
| `GET` | `/api/discount-rules/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/discount-rules \
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

## Bundle

Base path: `/bundles`.

Several products sold together at a bundle price.

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
| `bundleType` | string | Text value, at most 120 characters. |
| `itemCount` | integer | Whole number. |
| `discountAmount` | number | Decimal amount with two decimal places. |
| `featured` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/bundles` | Create a record. Returns `201`. |
| `GET` | `/api/bundles/{id}` | Read one record by id. |
| `GET` | `/api/bundles/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/bundles?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/bundles/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/bundles/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/bundles/all-active` | Every active record. |
| `GET` | `/api/bundles/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/bundles/{id}` | Replace the editable fields. |
| `POST` | `/api/bundles/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/bundles/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/bundles/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/bundles/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/bundles/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/bundles/stats/active` | Count of active records. |
| `GET` | `/api/bundles/stats/archived` | Count of archived records. |
| `GET` | `/api/bundles/stats/total` | Sum of active amounts. |
| `GET` | `/api/bundles/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/bundles \
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

## Warranty

Base path: `/warrantys`.

Coverage terms offered with a product, including duration and registration rules.

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
| `provider` | string | Text value, at most 120 characters. |
| `durationMonths` | integer | Whole number. |
| `termsUrl` | string | Text value, at most 120 characters. |
| `registrationRequired` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/warrantys` | Create a record. Returns `201`. |
| `GET` | `/api/warrantys/{id}` | Read one record by id. |
| `GET` | `/api/warrantys/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/warrantys?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/warrantys/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/warrantys/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/warrantys/all-active` | Every active record. |
| `GET` | `/api/warrantys/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/warrantys/{id}` | Replace the editable fields. |
| `POST` | `/api/warrantys/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/warrantys/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/warrantys/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/warrantys/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/warrantys/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/warrantys/stats/active` | Count of active records. |
| `GET` | `/api/warrantys/stats/archived` | Count of archived records. |
| `GET` | `/api/warrantys/stats/total` | Sum of active amounts. |
| `GET` | `/api/warrantys/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/warrantys \
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

## Supplier Contract

Base path: `/supplier-contracts`.

An agreement with a supplier, including renewal notice and signature state.

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
| `contractRef` | string | Text value, at most 120 characters. |
| `renewalNoticeDays` | integer | Whole number. |
| `signed` | boolean | True or false flag. |
| `signedAt` | string | ISO-8601 instant in UTC. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/supplier-contracts` | Create a record. Returns `201`. |
| `GET` | `/api/supplier-contracts/{id}` | Read one record by id. |
| `GET` | `/api/supplier-contracts/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/supplier-contracts?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/supplier-contracts/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/supplier-contracts/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/supplier-contracts/all-active` | Every active record. |
| `GET` | `/api/supplier-contracts/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/supplier-contracts/{id}` | Replace the editable fields. |
| `POST` | `/api/supplier-contracts/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/supplier-contracts/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/supplier-contracts/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/supplier-contracts/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/supplier-contracts/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/supplier-contracts/stats/active` | Count of active records. |
| `GET` | `/api/supplier-contracts/stats/archived` | Count of archived records. |
| `GET` | `/api/supplier-contracts/stats/total` | Sum of active amounts. |
| `GET` | `/api/supplier-contracts/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/supplier-contracts \
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

## Seo Entry

Base path: `/seo-entrys`.

Search metadata for a slug: title, description and keywords.

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
| `slug` | string | Text value, at most 120 characters. |
| `metaTitle` | string | Text value, at most 120 characters. |
| `metaDescription` | string | Text value, at most 120 characters. |
| `keywords` | string | Text value, at most 120 characters. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/seo-entrys` | Create a record. Returns `201`. |
| `GET` | `/api/seo-entrys/{id}` | Read one record by id. |
| `GET` | `/api/seo-entrys/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/seo-entrys?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/seo-entrys/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/seo-entrys/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/seo-entrys/all-active` | Every active record. |
| `GET` | `/api/seo-entrys/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/seo-entrys/{id}` | Replace the editable fields. |
| `POST` | `/api/seo-entrys/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/seo-entrys/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/seo-entrys/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/seo-entrys/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/seo-entrys/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/seo-entrys/stats/active` | Count of active records. |
| `GET` | `/api/seo-entrys/stats/archived` | Count of archived records. |
| `GET` | `/api/seo-entrys/stats/total` | Sum of active amounts. |
| `GET` | `/api/seo-entrys/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/seo-entrys \
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

## Catalog Import

Base path: `/catalog-imports`.

A record of a catalog import job, with source, file and checksum.

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
| `source` | string | Text value, at most 120 characters. |
| `fileName` | string | Text value, at most 120 characters. |
| `rowCount` | integer | 64-bit whole number. |
| `checksum` | string | Text value, at most 120 characters. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/catalog-imports` | Create a record. Returns `201`. |
| `GET` | `/api/catalog-imports/{id}` | Read one record by id. |
| `GET` | `/api/catalog-imports/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/catalog-imports?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/catalog-imports/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/catalog-imports/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/catalog-imports/all-active` | Every active record. |
| `GET` | `/api/catalog-imports/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/catalog-imports/{id}` | Replace the editable fields. |
| `POST` | `/api/catalog-imports/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/catalog-imports/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/catalog-imports/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/catalog-imports/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/catalog-imports/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/catalog-imports/stats/active` | Count of active records. |
| `GET` | `/api/catalog-imports/stats/archived` | Count of archived records. |
| `GET` | `/api/catalog-imports/stats/total` | Sum of active amounts. |
| `GET` | `/api/catalog-imports/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/catalog-imports \
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


## Paged product search

`GET /api/products/page?page=0&size=20` returns a bounded page of products
ordered by priority, highest first. It is the preferred read for catalog list
screens.

```json
{
  "items": [],
  "total": 0,
  "page": 0,
  "size": 20
}
```

`page` is zero-based and `size` is capped at 200. A page or size outside the
allowed range returns `400 INVALID_REQUEST`.

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
mvn -pl catalog-service -am test
```
