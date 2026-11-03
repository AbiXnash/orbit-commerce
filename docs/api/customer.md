# Customer API

`customer-service` owns customers, contact details, consent, loyalty and support.

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

## Contact

Base path: `/contacts`.

An email or phone contact for a customer, with a primary flag.

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
| `email` | string | Text value, at most 120 characters. |
| `phone` | string | Text value, at most 120 characters. |
| `primaryContact` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/contacts` | Create a record. Returns `201`. |
| `GET` | `/api/contacts/{id}` | Read one record by id. |
| `GET` | `/api/contacts/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/contacts?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/contacts/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/contacts/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/contacts/all-active` | Every active record. |
| `GET` | `/api/contacts/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/contacts/{id}` | Replace the editable fields. |
| `POST` | `/api/contacts/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/contacts/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/contacts/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/contacts/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/contacts/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/contacts/stats/active` | Count of active records. |
| `GET` | `/api/contacts/stats/archived` | Count of archived records. |
| `GET` | `/api/contacts/stats/total` | Sum of active amounts. |
| `GET` | `/api/contacts/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/contacts \
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

## Loyalty Transaction

Base path: `/loyalty-transactions`.

One points movement on a loyalty account.

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
| `accountRef` | string | Text value, at most 120 characters. |
| `transactionType` | string | Text value, at most 120 characters. |
| `points` | integer | Whole number. |
| `occurredAt` | string | ISO-8601 instant in UTC. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/loyalty-transactions` | Create a record. Returns `201`. |
| `GET` | `/api/loyalty-transactions/{id}` | Read one record by id. |
| `GET` | `/api/loyalty-transactions/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/loyalty-transactions?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/loyalty-transactions/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/loyalty-transactions/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/loyalty-transactions/all-active` | Every active record. |
| `GET` | `/api/loyalty-transactions/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/loyalty-transactions/{id}` | Replace the editable fields. |
| `POST` | `/api/loyalty-transactions/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/loyalty-transactions/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/loyalty-transactions/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/loyalty-transactions/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/loyalty-transactions/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/loyalty-transactions/stats/active` | Count of active records. |
| `GET` | `/api/loyalty-transactions/stats/archived` | Count of archived records. |
| `GET` | `/api/loyalty-transactions/stats/total` | Sum of active amounts. |
| `GET` | `/api/loyalty-transactions/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/loyalty-transactions \
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

## Customer Note

Base path: `/customer-notes`.

A support note attached to a customer, with its author.

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
| `author` | string | Text value, at most 120 characters. |
| `body` | string | Text value, at most 120 characters. |
| `pinned` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/customer-notes` | Create a record. Returns `201`. |
| `GET` | `/api/customer-notes/{id}` | Read one record by id. |
| `GET` | `/api/customer-notes/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/customer-notes?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/customer-notes/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/customer-notes/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/customer-notes/all-active` | Every active record. |
| `GET` | `/api/customer-notes/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/customer-notes/{id}` | Replace the editable fields. |
| `POST` | `/api/customer-notes/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/customer-notes/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/customer-notes/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/customer-notes/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/customer-notes/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/customer-notes/stats/active` | Count of active records. |
| `GET` | `/api/customer-notes/stats/archived` | Count of archived records. |
| `GET` | `/api/customer-notes/stats/total` | Sum of active amounts. |
| `GET` | `/api/customer-notes/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/customer-notes \
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

## Customer Group

Base path: `/customer-groups`.

A manual group of customers with a stable slug.

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
| `memberCount` | integer | Whole number. |
| `featured` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/customer-groups` | Create a record. Returns `201`. |
| `GET` | `/api/customer-groups/{id}` | Read one record by id. |
| `GET` | `/api/customer-groups/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/customer-groups?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/customer-groups/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/customer-groups/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/customer-groups/all-active` | Every active record. |
| `GET` | `/api/customer-groups/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/customer-groups/{id}` | Replace the editable fields. |
| `POST` | `/api/customer-groups/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/customer-groups/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/customer-groups/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/customer-groups/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/customer-groups/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/customer-groups/stats/active` | Count of active records. |
| `GET` | `/api/customer-groups/stats/archived` | Count of archived records. |
| `GET` | `/api/customer-groups/stats/total` | Sum of active amounts. |
| `GET` | `/api/customer-groups/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/customer-groups \
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

## Customer Tag

Base path: `/customer-tags`.

A label applied to customers, with a slug and colour.

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
| `colorHex` | string | Text value, at most 120 characters. |
| `description` | string | Text value, at most 120 characters. |
| `featured` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/customer-tags` | Create a record. Returns `201`. |
| `GET` | `/api/customer-tags/{id}` | Read one record by id. |
| `GET` | `/api/customer-tags/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/customer-tags?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/customer-tags/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/customer-tags/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/customer-tags/all-active` | Every active record. |
| `GET` | `/api/customer-tags/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/customer-tags/{id}` | Replace the editable fields. |
| `POST` | `/api/customer-tags/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/customer-tags/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/customer-tags/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/customer-tags/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/customer-tags/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/customer-tags/stats/active` | Count of active records. |
| `GET` | `/api/customer-tags/stats/archived` | Count of archived records. |
| `GET` | `/api/customer-tags/stats/total` | Sum of active amounts. |
| `GET` | `/api/customer-tags/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/customer-tags \
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

## Referral

Base path: `/referrals`.

A referral relationship between two customers, rewarded once.

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
| `referrerRef` | string | Text value, at most 120 characters. |
| `refereeRef` | string | Text value, at most 120 characters. |
| `referralCode` | string | Text value, at most 120 characters. |
| `rewarded` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/referrals` | Create a record. Returns `201`. |
| `GET` | `/api/referrals/{id}` | Read one record by id. |
| `GET` | `/api/referrals/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/referrals?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/referrals/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/referrals/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/referrals/all-active` | Every active record. |
| `GET` | `/api/referrals/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/referrals/{id}` | Replace the editable fields. |
| `POST` | `/api/referrals/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/referrals/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/referrals/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/referrals/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/referrals/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/referrals/stats/active` | Count of active records. |
| `GET` | `/api/referrals/stats/archived` | Count of archived records. |
| `GET` | `/api/referrals/stats/total` | Sum of active amounts. |
| `GET` | `/api/referrals/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/referrals \
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

## Feedback

Base path: `/feedbacks`.

Free-form customer feedback with an optional rating.

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
| `comment` | string | Text value, at most 120 characters. |
| `rating` | integer | Whole number. |
| `submittedAt` | string | ISO-8601 instant in UTC. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/feedbacks` | Create a record. Returns `201`. |
| `GET` | `/api/feedbacks/{id}` | Read one record by id. |
| `GET` | `/api/feedbacks/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/feedbacks?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/feedbacks/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/feedbacks/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/feedbacks/all-active` | Every active record. |
| `GET` | `/api/feedbacks/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/feedbacks/{id}` | Replace the editable fields. |
| `POST` | `/api/feedbacks/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/feedbacks/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/feedbacks/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/feedbacks/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/feedbacks/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/feedbacks/stats/active` | Count of active records. |
| `GET` | `/api/feedbacks/stats/archived` | Count of archived records. |
| `GET` | `/api/feedbacks/stats/total` | Sum of active amounts. |
| `GET` | `/api/feedbacks/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/feedbacks \
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

## Survey Response

Base path: `/survey-responses`.

One response to a survey, recorded once per customer.

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
| `surveyRef` | string | Text value, at most 120 characters. |
| `customerRef` | string | Text value, at most 120 characters. |
| `comment` | string | Text value, at most 120 characters. |
| `submittedAt` | string | ISO-8601 instant in UTC. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/survey-responses` | Create a record. Returns `201`. |
| `GET` | `/api/survey-responses/{id}` | Read one record by id. |
| `GET` | `/api/survey-responses/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/survey-responses?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/survey-responses/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/survey-responses/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/survey-responses/all-active` | Every active record. |
| `GET` | `/api/survey-responses/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/survey-responses/{id}` | Replace the editable fields. |
| `POST` | `/api/survey-responses/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/survey-responses/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/survey-responses/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/survey-responses/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/survey-responses/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/survey-responses/stats/active` | Count of active records. |
| `GET` | `/api/survey-responses/stats/archived` | Count of archived records. |
| `GET` | `/api/survey-responses/stats/total` | Sum of active amounts. |
| `GET` | `/api/survey-responses/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/survey-responses \
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

## Communication Preference

Base path: `/communication-preferences`.

A per-channel opt-in for a customer. Opt-out wins.

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
| `channel` | string | Text value, at most 120 characters. |
| `optedIn` | boolean | True or false flag. |
| `lastUpdatedAt` | string | ISO-8601 instant in UTC. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/communication-preferences` | Create a record. Returns `201`. |
| `GET` | `/api/communication-preferences/{id}` | Read one record by id. |
| `GET` | `/api/communication-preferences/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/communication-preferences?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/communication-preferences/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/communication-preferences/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/communication-preferences/all-active` | Every active record. |
| `GET` | `/api/communication-preferences/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/communication-preferences/{id}` | Replace the editable fields. |
| `POST` | `/api/communication-preferences/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/communication-preferences/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/communication-preferences/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/communication-preferences/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/communication-preferences/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/communication-preferences/stats/active` | Count of active records. |
| `GET` | `/api/communication-preferences/stats/archived` | Count of archived records. |
| `GET` | `/api/communication-preferences/stats/total` | Sum of active amounts. |
| `GET` | `/api/communication-preferences/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/communication-preferences \
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

## Identity Document

Base path: `/identity-documents`.

A verification artefact for a customer, with a monotonic verified flag.

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
| `documentType` | string | Text value, at most 120 characters. |
| `documentNumber` | string | Text value, at most 120 characters. |
| `verified` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/identity-documents` | Create a record. Returns `201`. |
| `GET` | `/api/identity-documents/{id}` | Read one record by id. |
| `GET` | `/api/identity-documents/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/identity-documents?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/identity-documents/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/identity-documents/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/identity-documents/all-active` | Every active record. |
| `GET` | `/api/identity-documents/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/identity-documents/{id}` | Replace the editable fields. |
| `POST` | `/api/identity-documents/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/identity-documents/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/identity-documents/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/identity-documents/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/identity-documents/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/identity-documents/stats/active` | Count of active records. |
| `GET` | `/api/identity-documents/stats/archived` | Count of archived records. |
| `GET` | `/api/identity-documents/stats/total` | Sum of active amounts. |
| `GET` | `/api/identity-documents/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/identity-documents \
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
mvn -pl customer-service -am test
```
