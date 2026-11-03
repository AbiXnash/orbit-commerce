# Payment API

`payment-service` owns payments, refunds, payouts, fees and the ledger.

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

## Payment Method

Base path: `/payment-methods`.

A stored method reference: type, last four digits and a token.

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
| `methodType` | string | Text value, at most 120 characters. |
| `last4` | string | Text value, at most 120 characters. |
| `holder` | string | Text value, at most 120 characters. |
| `token` | string | Text value, at most 120 characters. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/payment-methods` | Create a record. Returns `201`. |
| `GET` | `/api/payment-methods/{id}` | Read one record by id. |
| `GET` | `/api/payment-methods/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/payment-methods?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/payment-methods/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/payment-methods/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/payment-methods/all-active` | Every active record. |
| `GET` | `/api/payment-methods/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/payment-methods/{id}` | Replace the editable fields. |
| `POST` | `/api/payment-methods/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/payment-methods/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/payment-methods/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/payment-methods/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/payment-methods/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/payment-methods/stats/active` | Count of active records. |
| `GET` | `/api/payment-methods/stats/archived` | Count of archived records. |
| `GET` | `/api/payment-methods/stats/total` | Sum of active amounts. |
| `GET` | `/api/payment-methods/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/payment-methods \
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

## Merchant Account

Base path: `/merchant-accounts`.

An account that receives funds, with provider and settlement currency.

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
| `merchantRef` | string | Text value, at most 120 characters. |
| `provider` | string | Text value, at most 120 characters. |
| `settlementCurrency` | string | Text value, at most 120 characters. |
| `active` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/merchant-accounts` | Create a record. Returns `201`. |
| `GET` | `/api/merchant-accounts/{id}` | Read one record by id. |
| `GET` | `/api/merchant-accounts/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/merchant-accounts?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/merchant-accounts/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/merchant-accounts/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/merchant-accounts/all-active` | Every active record. |
| `GET` | `/api/merchant-accounts/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/merchant-accounts/{id}` | Replace the editable fields. |
| `POST` | `/api/merchant-accounts/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/merchant-accounts/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/merchant-accounts/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/merchant-accounts/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/merchant-accounts/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/merchant-accounts/stats/active` | Count of active records. |
| `GET` | `/api/merchant-accounts/stats/archived` | Count of archived records. |
| `GET` | `/api/merchant-accounts/stats/total` | Sum of active amounts. |
| `GET` | `/api/merchant-accounts/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/merchant-accounts \
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

## Gateway Transaction

Base path: `/gateway-transactions`.

A raw provider attempt with request id, response code and latency.

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
| `gatewayRef` | string | Text value, at most 120 characters. |
| `requestId` | string | Text value, at most 120 characters. |
| `responseCode` | string | Text value, at most 120 characters. |
| `latencyMs` | integer | Whole number. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/gateway-transactions` | Create a record. Returns `201`. |
| `GET` | `/api/gateway-transactions/{id}` | Read one record by id. |
| `GET` | `/api/gateway-transactions/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/gateway-transactions?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/gateway-transactions/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/gateway-transactions/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/gateway-transactions/all-active` | Every active record. |
| `GET` | `/api/gateway-transactions/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/gateway-transactions/{id}` | Replace the editable fields. |
| `POST` | `/api/gateway-transactions/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/gateway-transactions/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/gateway-transactions/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/gateway-transactions/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/gateway-transactions/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/gateway-transactions/stats/active` | Count of active records. |
| `GET` | `/api/gateway-transactions/stats/archived` | Count of archived records. |
| `GET` | `/api/gateway-transactions/stats/total` | Sum of active amounts. |
| `GET` | `/api/gateway-transactions/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/gateway-transactions \
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

## Fraud Check

Base path: `/fraud-checks`.

A risk decision for a payment, with a score and review state.

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
| `paymentRef` | string | Text value, at most 120 characters. |
| `decision` | string | Text value, at most 120 characters. |
| `reviewed` | boolean | True or false flag. |
| `score` | integer | Whole number. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/fraud-checks` | Create a record. Returns `201`. |
| `GET` | `/api/fraud-checks/{id}` | Read one record by id. |
| `GET` | `/api/fraud-checks/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/fraud-checks?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/fraud-checks/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/fraud-checks/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/fraud-checks/all-active` | Every active record. |
| `GET` | `/api/fraud-checks/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/fraud-checks/{id}` | Replace the editable fields. |
| `POST` | `/api/fraud-checks/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/fraud-checks/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/fraud-checks/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/fraud-checks/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/fraud-checks/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/fraud-checks/stats/active` | Count of active records. |
| `GET` | `/api/fraud-checks/stats/archived` | Count of archived records. |
| `GET` | `/api/fraud-checks/stats/total` | Sum of active amounts. |
| `GET` | `/api/fraud-checks/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/fraud-checks \
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

## Dispute

Base path: `/disputes`.

A customer dispute against a payment, resolved once.

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
| `paymentRef` | string | Text value, at most 120 characters. |
| `reason` | string | Text value, at most 120 characters. |
| `openedAt` | string | ISO-8601 instant in UTC. |
| `resolved` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/disputes` | Create a record. Returns `201`. |
| `GET` | `/api/disputes/{id}` | Read one record by id. |
| `GET` | `/api/disputes/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/disputes?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/disputes/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/disputes/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/disputes/all-active` | Every active record. |
| `GET` | `/api/disputes/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/disputes/{id}` | Replace the editable fields. |
| `POST` | `/api/disputes/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/disputes/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/disputes/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/disputes/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/disputes/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/disputes/stats/active` | Count of active records. |
| `GET` | `/api/disputes/stats/archived` | Count of archived records. |
| `GET` | `/api/disputes/stats/total` | Sum of active amounts. |
| `GET` | `/api/disputes/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/disputes \
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

## Currency Rate

Base path: `/currency-rates`.

A timestamped conversion rate between two currencies.

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
| `baseCurrency` | string | Text value, at most 120 characters. |
| `quoteCurrency` | string | Text value, at most 120 characters. |
| `rate` | number | Decimal amount with two decimal places. |
| `quotedAt` | string | ISO-8601 instant in UTC. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/currency-rates` | Create a record. Returns `201`. |
| `GET` | `/api/currency-rates/{id}` | Read one record by id. |
| `GET` | `/api/currency-rates/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/currency-rates?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/currency-rates/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/currency-rates/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/currency-rates/all-active` | Every active record. |
| `GET` | `/api/currency-rates/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/currency-rates/{id}` | Replace the editable fields. |
| `POST` | `/api/currency-rates/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/currency-rates/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/currency-rates/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/currency-rates/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/currency-rates/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/currency-rates/stats/active` | Count of active records. |
| `GET` | `/api/currency-rates/stats/archived` | Count of archived records. |
| `GET` | `/api/currency-rates/stats/total` | Sum of active amounts. |
| `GET` | `/api/currency-rates/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/currency-rates \
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

## Payout Batch

Base path: `/payout-batchs`.

A group of payouts settled together.

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
| `batchRef` | string | Text value, at most 120 characters. |
| `payoutCount` | integer | Whole number. |
| `settled` | boolean | True or false flag. |
| `settledAt` | string | ISO-8601 instant in UTC. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/payout-batchs` | Create a record. Returns `201`. |
| `GET` | `/api/payout-batchs/{id}` | Read one record by id. |
| `GET` | `/api/payout-batchs/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/payout-batchs?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/payout-batchs/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/payout-batchs/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/payout-batchs/all-active` | Every active record. |
| `GET` | `/api/payout-batchs/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/payout-batchs/{id}` | Replace the editable fields. |
| `POST` | `/api/payout-batchs/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/payout-batchs/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/payout-batchs/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/payout-batchs/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/payout-batchs/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/payout-batchs/stats/active` | Count of active records. |
| `GET` | `/api/payout-batchs/stats/archived` | Count of archived records. |
| `GET` | `/api/payout-batchs/stats/total` | Sum of active amounts. |
| `GET` | `/api/payout-batchs/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/payout-batchs \
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

## Refund Line

Base path: `/refund-lines`.

One line of a refund, referencing the original payment.

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
| `refundRef` | string | Text value, at most 120 characters. |
| `paymentRef` | string | Text value, at most 120 characters. |
| `reason` | string | Text value, at most 120 characters. |
| `approved` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/refund-lines` | Create a record. Returns `201`. |
| `GET` | `/api/refund-lines/{id}` | Read one record by id. |
| `GET` | `/api/refund-lines/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/refund-lines?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/refund-lines/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/refund-lines/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/refund-lines/all-active` | Every active record. |
| `GET` | `/api/refund-lines/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/refund-lines/{id}` | Replace the editable fields. |
| `POST` | `/api/refund-lines/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/refund-lines/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/refund-lines/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/refund-lines/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/refund-lines/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/refund-lines/stats/active` | Count of active records. |
| `GET` | `/api/refund-lines/stats/archived` | Count of archived records. |
| `GET` | `/api/refund-lines/stats/total` | Sum of active amounts. |
| `GET` | `/api/refund-lines/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/refund-lines \
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

## Card Token

Base path: `/card-tokens`.

A tokenised card with expiry and active state. Never stores a card number.

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
| `tokenRef` | string | Text value, at most 120 characters. |
| `last4` | string | Text value, at most 120 characters. |
| `expiresAt` | string | ISO-8601 instant in UTC. |
| `active` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/card-tokens` | Create a record. Returns `201`. |
| `GET` | `/api/card-tokens/{id}` | Read one record by id. |
| `GET` | `/api/card-tokens/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/card-tokens?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/card-tokens/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/card-tokens/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/card-tokens/all-active` | Every active record. |
| `GET` | `/api/card-tokens/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/card-tokens/{id}` | Replace the editable fields. |
| `POST` | `/api/card-tokens/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/card-tokens/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/card-tokens/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/card-tokens/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/card-tokens/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/card-tokens/stats/active` | Count of active records. |
| `GET` | `/api/card-tokens/stats/archived` | Count of archived records. |
| `GET` | `/api/card-tokens/stats/total` | Sum of active amounts. |
| `GET` | `/api/card-tokens/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/card-tokens \
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

## Payment Fee

Base path: `/payment-fees`.

A provider or platform fee attached to a payment.

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
| `paymentRef` | string | Text value, at most 120 characters. |
| `feeType` | string | Text value, at most 120 characters. |
| `waived` | boolean | True or false flag. |
| `feeAmount` | number | Decimal amount with two decimal places. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/payment-fees` | Create a record. Returns `201`. |
| `GET` | `/api/payment-fees/{id}` | Read one record by id. |
| `GET` | `/api/payment-fees/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/payment-fees?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/payment-fees/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/payment-fees/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/payment-fees/all-active` | Every active record. |
| `GET` | `/api/payment-fees/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/payment-fees/{id}` | Replace the editable fields. |
| `POST` | `/api/payment-fees/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/payment-fees/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/payment-fees/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/payment-fees/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/payment-fees/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/payment-fees/stats/active` | Count of active records. |
| `GET` | `/api/payment-fees/stats/archived` | Count of archived records. |
| `GET` | `/api/payment-fees/stats/total` | Sum of active amounts. |
| `GET` | `/api/payment-fees/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/payment-fees \
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
mvn -pl payment-service -am test
```
