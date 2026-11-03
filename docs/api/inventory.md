# Inventory API

`inventory-service` owns stock, locations, reservations, counts and replenishment.

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

## Bin Location

Base path: `/bin-locations`.

A shelf position inside a warehouse, with aisle, rack, shelf and capacity.

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
| `aisle` | string | Text value, at most 120 characters. |
| `rack` | string | Text value, at most 120 characters. |
| `shelf` | string | Text value, at most 120 characters. |
| `capacity` | integer | Whole number. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/bin-locations` | Create a record. Returns `201`. |
| `GET` | `/api/bin-locations/{id}` | Read one record by id. |
| `GET` | `/api/bin-locations/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/bin-locations?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/bin-locations/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/bin-locations/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/bin-locations/all-active` | Every active record. |
| `GET` | `/api/bin-locations/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/bin-locations/{id}` | Replace the editable fields. |
| `POST` | `/api/bin-locations/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/bin-locations/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/bin-locations/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/bin-locations/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/bin-locations/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/bin-locations/stats/active` | Count of active records. |
| `GET` | `/api/bin-locations/stats/archived` | Count of archived records. |
| `GET` | `/api/bin-locations/stats/total` | Sum of active amounts. |
| `GET` | `/api/bin-locations/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/bin-locations \
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

## Lot

Base path: `/lots`.

A traceable batch of stock with an expiry date and an optional quarantine flag.

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
| `lotNumber` | string | Text value, at most 120 characters. |
| `supplierCode` | string | Text value, at most 120 characters. |
| `expiryDate` | string | ISO-8601 instant in UTC. |
| `quarantine` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/lots` | Create a record. Returns `201`. |
| `GET` | `/api/lots/{id}` | Read one record by id. |
| `GET` | `/api/lots/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/lots?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/lots/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/lots/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/lots/all-active` | Every active record. |
| `GET` | `/api/lots/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/lots/{id}` | Replace the editable fields. |
| `POST` | `/api/lots/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/lots/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/lots/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/lots/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/lots/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/lots/stats/active` | Count of active records. |
| `GET` | `/api/lots/stats/archived` | Count of archived records. |
| `GET` | `/api/lots/stats/total` | Sum of active amounts. |
| `GET` | `/api/lots/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/lots \
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

## Serial Number

Base path: `/serial-numbers`.

An individually tracked unit with product and warranty context.

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
| `serial` | string | Text value, at most 120 characters. |
| `productCode` | string | Text value, at most 120 characters. |
| `warrantyEndsAt` | string | ISO-8601 instant in UTC. |
| `assigned` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/serial-numbers` | Create a record. Returns `201`. |
| `GET` | `/api/serial-numbers/{id}` | Read one record by id. |
| `GET` | `/api/serial-numbers/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/serial-numbers?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/serial-numbers/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/serial-numbers/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/serial-numbers/all-active` | Every active record. |
| `GET` | `/api/serial-numbers/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/serial-numbers/{id}` | Replace the editable fields. |
| `POST` | `/api/serial-numbers/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/serial-numbers/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/serial-numbers/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/serial-numbers/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/serial-numbers/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/serial-numbers/stats/active` | Count of active records. |
| `GET` | `/api/serial-numbers/stats/archived` | Count of archived records. |
| `GET` | `/api/serial-numbers/stats/total` | Sum of active amounts. |
| `GET` | `/api/serial-numbers/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/serial-numbers \
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

## Stock Alert

Base path: `/stock-alerts`.

A threshold that raises an alert when stock crosses it.

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
| `threshold` | integer | Whole number. |
| `channel` | string | Text value, at most 120 characters. |
| `lastTriggeredAt` | string | ISO-8601 instant in UTC. |
| `acknowledged` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/stock-alerts` | Create a record. Returns `201`. |
| `GET` | `/api/stock-alerts/{id}` | Read one record by id. |
| `GET` | `/api/stock-alerts/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/stock-alerts?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/stock-alerts/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/stock-alerts/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/stock-alerts/all-active` | Every active record. |
| `GET` | `/api/stock-alerts/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/stock-alerts/{id}` | Replace the editable fields. |
| `POST` | `/api/stock-alerts/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/stock-alerts/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/stock-alerts/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/stock-alerts/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/stock-alerts/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/stock-alerts/stats/active` | Count of active records. |
| `GET` | `/api/stock-alerts/stats/archived` | Count of archived records. |
| `GET` | `/api/stock-alerts/stats/total` | Sum of active amounts. |
| `GET` | `/api/stock-alerts/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/stock-alerts \
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

## Pick List

Base path: `/pick-lists`.

Work assigned to a picker for an order reference.

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
| `pickerName` | string | Text value, at most 120 characters. |
| `lineCount` | integer | Whole number. |
| `picked` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/pick-lists` | Create a record. Returns `201`. |
| `GET` | `/api/pick-lists/{id}` | Read one record by id. |
| `GET` | `/api/pick-lists/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/pick-lists?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/pick-lists/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/pick-lists/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/pick-lists/all-active` | Every active record. |
| `GET` | `/api/pick-lists/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/pick-lists/{id}` | Replace the editable fields. |
| `POST` | `/api/pick-lists/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/pick-lists/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/pick-lists/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/pick-lists/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/pick-lists/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/pick-lists/stats/active` | Count of active records. |
| `GET` | `/api/pick-lists/stats/archived` | Count of archived records. |
| `GET` | `/api/pick-lists/stats/total` | Sum of active amounts. |
| `GET` | `/api/pick-lists/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/pick-lists \
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

## Putaway Task

Base path: `/putaway-tasks`.

Work to move received stock into a bin.

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
| `inboundRef` | string | Text value, at most 120 characters. |
| `binCode` | string | Text value, at most 120 characters. |
| `quantity` | integer | Whole number. |
| `completed` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/putaway-tasks` | Create a record. Returns `201`. |
| `GET` | `/api/putaway-tasks/{id}` | Read one record by id. |
| `GET` | `/api/putaway-tasks/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/putaway-tasks?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/putaway-tasks/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/putaway-tasks/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/putaway-tasks/all-active` | Every active record. |
| `GET` | `/api/putaway-tasks/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/putaway-tasks/{id}` | Replace the editable fields. |
| `POST` | `/api/putaway-tasks/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/putaway-tasks/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/putaway-tasks/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/putaway-tasks/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/putaway-tasks/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/putaway-tasks/stats/active` | Count of active records. |
| `GET` | `/api/putaway-tasks/stats/archived` | Count of archived records. |
| `GET` | `/api/putaway-tasks/stats/total` | Sum of active amounts. |
| `GET` | `/api/putaway-tasks/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/putaway-tasks \
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

## Inventory Audit

Base path: `/inventory-audits`.

A count session recording auditor, date, notes and variance.

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
| `auditor` | string | Text value, at most 120 characters. |
| `auditDate` | string | ISO-8601 instant in UTC. |
| `notes` | string | Text value, at most 120 characters. |
| `variance` | number | Decimal amount with two decimal places. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/inventory-audits` | Create a record. Returns `201`. |
| `GET` | `/api/inventory-audits/{id}` | Read one record by id. |
| `GET` | `/api/inventory-audits/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/inventory-audits?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/inventory-audits/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/inventory-audits/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/inventory-audits/all-active` | Every active record. |
| `GET` | `/api/inventory-audits/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/inventory-audits/{id}` | Replace the editable fields. |
| `POST` | `/api/inventory-audits/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/inventory-audits/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/inventory-audits/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/inventory-audits/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/inventory-audits/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/inventory-audits/stats/active` | Count of active records. |
| `GET` | `/api/inventory-audits/stats/archived` | Count of archived records. |
| `GET` | `/api/inventory-audits/stats/total` | Sum of active amounts. |
| `GET` | `/api/inventory-audits/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/inventory-audits \
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

## Backorder

Base path: `/backorders`.

Demand that cannot be met from current stock, with a promised date.

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
| `customerRef` | string | Text value, at most 120 characters. |
| `quantity` | integer | Whole number. |
| `promisedAt` | string | ISO-8601 instant in UTC. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/backorders` | Create a record. Returns `201`. |
| `GET` | `/api/backorders/{id}` | Read one record by id. |
| `GET` | `/api/backorders/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/backorders?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/backorders/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/backorders/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/backorders/all-active` | Every active record. |
| `GET` | `/api/backorders/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/backorders/{id}` | Replace the editable fields. |
| `POST` | `/api/backorders/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/backorders/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/backorders/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/backorders/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/backorders/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/backorders/stats/active` | Count of active records. |
| `GET` | `/api/backorders/stats/archived` | Count of archived records. |
| `GET` | `/api/backorders/stats/total` | Sum of active amounts. |
| `GET` | `/api/backorders/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/backorders \
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

## Stock Transfer Line

Base path: `/stock-transfer-lines`.

One product line inside a stock transfer, with sent and received quantities.

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
| `transferRef` | string | Text value, at most 120 characters. |
| `productCode` | string | Text value, at most 120 characters. |
| `quantity` | integer | Whole number. |
| `receivedQuantity` | integer | Whole number. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/stock-transfer-lines` | Create a record. Returns `201`. |
| `GET` | `/api/stock-transfer-lines/{id}` | Read one record by id. |
| `GET` | `/api/stock-transfer-lines/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/stock-transfer-lines?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/stock-transfer-lines/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/stock-transfer-lines/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/stock-transfer-lines/all-active` | Every active record. |
| `GET` | `/api/stock-transfer-lines/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/stock-transfer-lines/{id}` | Replace the editable fields. |
| `POST` | `/api/stock-transfer-lines/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/stock-transfer-lines/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/stock-transfer-lines/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/stock-transfer-lines/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/stock-transfer-lines/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/stock-transfer-lines/stats/active` | Count of active records. |
| `GET` | `/api/stock-transfer-lines/stats/archived` | Count of archived records. |
| `GET` | `/api/stock-transfer-lines/stats/total` | Sum of active amounts. |
| `GET` | `/api/stock-transfer-lines/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/stock-transfer-lines \
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

## Safety Stock

Base path: `/safety-stocks`.

Minimum and reorder levels for a product.

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
| `productCode` | string | Text value, at most 120 characters. |
| `minimumLevel` | integer | Whole number. |
| `reorderPoint` | integer | Whole number. |
| `leadDays` | integer | Whole number. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/safety-stocks` | Create a record. Returns `201`. |
| `GET` | `/api/safety-stocks/{id}` | Read one record by id. |
| `GET` | `/api/safety-stocks/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/safety-stocks?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/safety-stocks/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/safety-stocks/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/safety-stocks/all-active` | Every active record. |
| `GET` | `/api/safety-stocks/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/safety-stocks/{id}` | Replace the editable fields. |
| `POST` | `/api/safety-stocks/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/safety-stocks/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/safety-stocks/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/safety-stocks/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/safety-stocks/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/safety-stocks/stats/active` | Count of active records. |
| `GET` | `/api/safety-stocks/stats/archived` | Count of archived records. |
| `GET` | `/api/safety-stocks/stats/total` | Sum of active amounts. |
| `GET` | `/api/safety-stocks/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/safety-stocks \
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
mvn -pl inventory-service -am test
```
