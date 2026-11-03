# Shipping API

`shipping-service` owns carriers, labels, routes, delivery and customs.

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

## Shipping Label

Base path: `/shipping-labels`.

A printable carrier label for a shipment reference.

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
| `shipmentRef` | string | Text value, at most 120 characters. |
| `labelUrl` | string | Text value, at most 120 characters. |
| `format` | string | Text value, at most 120 characters. |
| `printed` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/shipping-labels` | Create a record. Returns `201`. |
| `GET` | `/api/shipping-labels/{id}` | Read one record by id. |
| `GET` | `/api/shipping-labels/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/shipping-labels?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/shipping-labels/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/shipping-labels/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/shipping-labels/all-active` | Every active record. |
| `GET` | `/api/shipping-labels/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/shipping-labels/{id}` | Replace the editable fields. |
| `POST` | `/api/shipping-labels/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/shipping-labels/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/shipping-labels/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/shipping-labels/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/shipping-labels/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/shipping-labels/stats/active` | Count of active records. |
| `GET` | `/api/shipping-labels/stats/archived` | Count of archived records. |
| `GET` | `/api/shipping-labels/stats/total` | Sum of active amounts. |
| `GET` | `/api/shipping-labels/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/shipping-labels \
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

## Tracking Event

Base path: `/tracking-events`.

A carrier scan event with location and occurrence time.

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
| `trackingNumber` | string | Text value, at most 120 characters. |
| `location` | string | Text value, at most 120 characters. |
| `eventCode` | string | Text value, at most 120 characters. |
| `occurredAt` | string | ISO-8601 instant in UTC. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/tracking-events` | Create a record. Returns `201`. |
| `GET` | `/api/tracking-events/{id}` | Read one record by id. |
| `GET` | `/api/tracking-events/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/tracking-events?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/tracking-events/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/tracking-events/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/tracking-events/all-active` | Every active record. |
| `GET` | `/api/tracking-events/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/tracking-events/{id}` | Replace the editable fields. |
| `POST` | `/api/tracking-events/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/tracking-events/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/tracking-events/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/tracking-events/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/tracking-events/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/tracking-events/stats/active` | Count of active records. |
| `GET` | `/api/tracking-events/stats/archived` | Count of archived records. |
| `GET` | `/api/tracking-events/stats/total` | Sum of active amounts. |
| `GET` | `/api/tracking-events/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/tracking-events \
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

## Zone

Base path: `/zones`.

A pricing and delivery area defined by region and postal range.

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
| `zoneCode` | string | Text value, at most 120 characters. |
| `region` | string | Text value, at most 120 characters. |
| `postalRange` | string | Text value, at most 120 characters. |
| `surcharge` | number | Decimal amount with two decimal places. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/zones` | Create a record. Returns `201`. |
| `GET` | `/api/zones/{id}` | Read one record by id. |
| `GET` | `/api/zones/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/zones?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/zones/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/zones/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/zones/all-active` | Every active record. |
| `GET` | `/api/zones/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/zones/{id}` | Replace the editable fields. |
| `POST` | `/api/zones/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/zones/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/zones/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/zones/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/zones/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/zones/stats/active` | Count of active records. |
| `GET` | `/api/zones/stats/archived` | Count of archived records. |
| `GET` | `/api/zones/stats/total` | Sum of active amounts. |
| `GET` | `/api/zones/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/zones \
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

## Carrier Account

Base path: `/carrier-accounts`.

Our account with a carrier, with an opened date.

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
| `carrierCode` | string | Text value, at most 120 characters. |
| `accountNumber` | string | Text value, at most 120 characters. |
| `active` | boolean | True or false flag. |
| `openedAt` | string | ISO-8601 instant in UTC. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/carrier-accounts` | Create a record. Returns `201`. |
| `GET` | `/api/carrier-accounts/{id}` | Read one record by id. |
| `GET` | `/api/carrier-accounts/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/carrier-accounts?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/carrier-accounts/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/carrier-accounts/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/carrier-accounts/all-active` | Every active record. |
| `GET` | `/api/carrier-accounts/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/carrier-accounts/{id}` | Replace the editable fields. |
| `POST` | `/api/carrier-accounts/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/carrier-accounts/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/carrier-accounts/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/carrier-accounts/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/carrier-accounts/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/carrier-accounts/stats/active` | Count of active records. |
| `GET` | `/api/carrier-accounts/stats/archived` | Count of archived records. |
| `GET` | `/api/carrier-accounts/stats/total` | Sum of active amounts. |
| `GET` | `/api/carrier-accounts/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/carrier-accounts \
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

## Packaging Type

Base path: `/packaging-types`.

A box or envelope with dimensions and a maximum weight.

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
| `dimensions` | string | Text value, at most 120 characters. |
| `reusable` | boolean | True or false flag. |
| `maxWeight` | number | Decimal amount with two decimal places. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/packaging-types` | Create a record. Returns `201`. |
| `GET` | `/api/packaging-types/{id}` | Read one record by id. |
| `GET` | `/api/packaging-types/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/packaging-types?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/packaging-types/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/packaging-types/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/packaging-types/all-active` | Every active record. |
| `GET` | `/api/packaging-types/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/packaging-types/{id}` | Replace the editable fields. |
| `POST` | `/api/packaging-types/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/packaging-types/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/packaging-types/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/packaging-types/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/packaging-types/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/packaging-types/stats/active` | Count of active records. |
| `GET` | `/api/packaging-types/stats/archived` | Count of archived records. |
| `GET` | `/api/packaging-types/stats/total` | Sum of active amounts. |
| `GET` | `/api/packaging-types/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/packaging-types \
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

## Delivery Attempt

Base path: `/delivery-attempts`.

One delivery attempt, with an outcome and increasing attempt number.

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
| `deliveryRef` | string | Text value, at most 120 characters. |
| `outcome` | string | Text value, at most 120 characters. |
| `attemptNumber` | integer | Whole number. |
| `attemptedAt` | string | ISO-8601 instant in UTC. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/delivery-attempts` | Create a record. Returns `201`. |
| `GET` | `/api/delivery-attempts/{id}` | Read one record by id. |
| `GET` | `/api/delivery-attempts/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/delivery-attempts?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/delivery-attempts/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/delivery-attempts/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/delivery-attempts/all-active` | Every active record. |
| `GET` | `/api/delivery-attempts/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/delivery-attempts/{id}` | Replace the editable fields. |
| `POST` | `/api/delivery-attempts/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/delivery-attempts/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/delivery-attempts/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/delivery-attempts/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/delivery-attempts/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/delivery-attempts/stats/active` | Count of active records. |
| `GET` | `/api/delivery-attempts/stats/archived` | Count of archived records. |
| `GET` | `/api/delivery-attempts/stats/total` | Sum of active amounts. |
| `GET` | `/api/delivery-attempts/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/delivery-attempts \
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

## Dispatch

Base path: `/dispatchs`.

A load leaving a site, with route, driver and vehicle.

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
| `routeRef` | string | Text value, at most 120 characters. |
| `driverName` | string | Text value, at most 120 characters. |
| `vehicleRef` | string | Text value, at most 120 characters. |
| `departed` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/dispatchs` | Create a record. Returns `201`. |
| `GET` | `/api/dispatchs/{id}` | Read one record by id. |
| `GET` | `/api/dispatchs/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/dispatchs?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/dispatchs/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/dispatchs/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/dispatchs/all-active` | Every active record. |
| `GET` | `/api/dispatchs/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/dispatchs/{id}` | Replace the editable fields. |
| `POST` | `/api/dispatchs/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/dispatchs/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/dispatchs/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/dispatchs/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/dispatchs/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/dispatchs/stats/active` | Count of active records. |
| `GET` | `/api/dispatchs/stats/archived` | Count of archived records. |
| `GET` | `/api/dispatchs/stats/total` | Sum of active amounts. |
| `GET` | `/api/dispatchs/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/dispatchs \
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

## Route Plan

Base path: `/route-plans`.

A planned route with an ordered stop count.

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
| `routeCode` | string | Text value, at most 120 characters. |
| `stopCount` | integer | Whole number. |
| `plannedFor` | string | ISO-8601 instant in UTC. |
| `optimized` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/route-plans` | Create a record. Returns `201`. |
| `GET` | `/api/route-plans/{id}` | Read one record by id. |
| `GET` | `/api/route-plans/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/route-plans?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/route-plans/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/route-plans/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/route-plans/all-active` | Every active record. |
| `GET` | `/api/route-plans/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/route-plans/{id}` | Replace the editable fields. |
| `POST` | `/api/route-plans/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/route-plans/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/route-plans/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/route-plans/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/route-plans/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/route-plans/stats/active` | Count of active records. |
| `GET` | `/api/route-plans/stats/archived` | Count of archived records. |
| `GET` | `/api/route-plans/stats/total` | Sum of active amounts. |
| `GET` | `/api/route-plans/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/route-plans \
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

## Freight Quote

Base path: `/freight-quotes`.

A quote for moving goods between an origin and a destination.

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
| `origin` | string | Text value, at most 120 characters. |
| `destination` | string | Text value, at most 120 characters. |
| `expiresAt` | string | ISO-8601 instant in UTC. |
| `accepted` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/freight-quotes` | Create a record. Returns `201`. |
| `GET` | `/api/freight-quotes/{id}` | Read one record by id. |
| `GET` | `/api/freight-quotes/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/freight-quotes?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/freight-quotes/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/freight-quotes/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/freight-quotes/all-active` | Every active record. |
| `GET` | `/api/freight-quotes/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/freight-quotes/{id}` | Replace the editable fields. |
| `POST` | `/api/freight-quotes/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/freight-quotes/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/freight-quotes/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/freight-quotes/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/freight-quotes/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/freight-quotes/stats/active` | Count of active records. |
| `GET` | `/api/freight-quotes/stats/archived` | Count of archived records. |
| `GET` | `/api/freight-quotes/stats/total` | Sum of active amounts. |
| `GET` | `/api/freight-quotes/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/freight-quotes \
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

## Customs Document

Base path: `/customs-documents`.

Cross-border paperwork with a declared value and clearance state.

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
| `shipmentRef` | string | Text value, at most 120 characters. |
| `documentType` | string | Text value, at most 120 characters. |
| `cleared` | boolean | True or false flag. |
| `declaredValue` | number | Decimal amount with two decimal places. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/customs-documents` | Create a record. Returns `201`. |
| `GET` | `/api/customs-documents/{id}` | Read one record by id. |
| `GET` | `/api/customs-documents/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/customs-documents?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/customs-documents/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/customs-documents/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/customs-documents/all-active` | Every active record. |
| `GET` | `/api/customs-documents/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/customs-documents/{id}` | Replace the editable fields. |
| `POST` | `/api/customs-documents/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/customs-documents/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/customs-documents/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/customs-documents/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/customs-documents/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/customs-documents/stats/active` | Count of active records. |
| `GET` | `/api/customs-documents/stats/archived` | Count of archived records. |
| `GET` | `/api/customs-documents/stats/total` | Sum of active amounts. |
| `GET` | `/api/customs-documents/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/customs-documents \
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
mvn -pl shipping-service -am test
```
