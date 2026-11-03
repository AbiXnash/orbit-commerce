# Notification API

`notification-service` owns messages, templates, campaigns and delivery results.

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

## Notification Preference

Base path: `/notification-preferences`.

A per-customer channel preference, including quiet hours.

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
| `enabled` | boolean | True or false flag. |
| `quietHours` | string | Text value, at most 120 characters. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/notification-preferences` | Create a record. Returns `201`. |
| `GET` | `/api/notification-preferences/{id}` | Read one record by id. |
| `GET` | `/api/notification-preferences/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/notification-preferences?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/notification-preferences/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/notification-preferences/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/notification-preferences/all-active` | Every active record. |
| `GET` | `/api/notification-preferences/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/notification-preferences/{id}` | Replace the editable fields. |
| `POST` | `/api/notification-preferences/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/notification-preferences/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/notification-preferences/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/notification-preferences/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/notification-preferences/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/notification-preferences/stats/active` | Count of active records. |
| `GET` | `/api/notification-preferences/stats/archived` | Count of archived records. |
| `GET` | `/api/notification-preferences/stats/total` | Sum of active amounts. |
| `GET` | `/api/notification-preferences/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/notification-preferences \
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

## Channel

Base path: `/channels`.

Transport configuration for a provider, with a rate limit.

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
| `channelType` | string | Text value, at most 120 characters. |
| `provider` | string | Text value, at most 120 characters. |
| `enabled` | boolean | True or false flag. |
| `rateLimit` | integer | Whole number. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/channels` | Create a record. Returns `201`. |
| `GET` | `/api/channels/{id}` | Read one record by id. |
| `GET` | `/api/channels/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/channels?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/channels/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/channels/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/channels/all-active` | Every active record. |
| `GET` | `/api/channels/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/channels/{id}` | Replace the editable fields. |
| `POST` | `/api/channels/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/channels/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/channels/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/channels/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/channels/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/channels/stats/active` | Count of active records. |
| `GET` | `/api/channels/stats/archived` | Count of archived records. |
| `GET` | `/api/channels/stats/total` | Sum of active amounts. |
| `GET` | `/api/channels/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/channels \
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

## Bounced Email

Base path: `/bounced-emails`.

A hard or soft bounce for an address.

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
| `messageRef` | string | Text value, at most 120 characters. |
| `email` | string | Text value, at most 120 characters. |
| `bounceType` | string | Text value, at most 120 characters. |
| `occurredAt` | string | ISO-8601 instant in UTC. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/bounced-emails` | Create a record. Returns `201`. |
| `GET` | `/api/bounced-emails/{id}` | Read one record by id. |
| `GET` | `/api/bounced-emails/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/bounced-emails?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/bounced-emails/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/bounced-emails/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/bounced-emails/all-active` | Every active record. |
| `GET` | `/api/bounced-emails/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/bounced-emails/{id}` | Replace the editable fields. |
| `POST` | `/api/bounced-emails/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/bounced-emails/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/bounced-emails/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/bounced-emails/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/bounced-emails/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/bounced-emails/stats/active` | Count of active records. |
| `GET` | `/api/bounced-emails/stats/archived` | Count of archived records. |
| `GET` | `/api/bounced-emails/stats/total` | Sum of active amounts. |
| `GET` | `/api/bounced-emails/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/bounced-emails \
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

## Push Subscription

Base path: `/push-subscriptions`.

A device registration for push messages.

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
| `deviceToken` | string | Text value, at most 120 characters. |
| `platform` | string | Text value, at most 120 characters. |
| `active` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/push-subscriptions` | Create a record. Returns `201`. |
| `GET` | `/api/push-subscriptions/{id}` | Read one record by id. |
| `GET` | `/api/push-subscriptions/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/push-subscriptions?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/push-subscriptions/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/push-subscriptions/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/push-subscriptions/all-active` | Every active record. |
| `GET` | `/api/push-subscriptions/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/push-subscriptions/{id}` | Replace the editable fields. |
| `POST` | `/api/push-subscriptions/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/push-subscriptions/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/push-subscriptions/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/push-subscriptions/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/push-subscriptions/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/push-subscriptions/stats/active` | Count of active records. |
| `GET` | `/api/push-subscriptions/stats/archived` | Count of archived records. |
| `GET` | `/api/push-subscriptions/stats/total` | Sum of active amounts. |
| `GET` | `/api/push-subscriptions/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/push-subscriptions \
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

## Event Log

Base path: `/event-logs`.

An append-only log of delivery events.

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
| `eventType` | string | Text value, at most 120 characters. |
| `source` | string | Text value, at most 120 characters. |
| `payload` | string | Text value, at most 120 characters. |
| `occurredAt` | string | ISO-8601 instant in UTC. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/event-logs` | Create a record. Returns `201`. |
| `GET` | `/api/event-logs/{id}` | Read one record by id. |
| `GET` | `/api/event-logs/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/event-logs?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/event-logs/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/event-logs/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/event-logs/all-active` | Every active record. |
| `GET` | `/api/event-logs/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/event-logs/{id}` | Replace the editable fields. |
| `POST` | `/api/event-logs/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/event-logs/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/event-logs/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/event-logs/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/event-logs/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/event-logs/stats/active` | Count of active records. |
| `GET` | `/api/event-logs/stats/archived` | Count of archived records. |
| `GET` | `/api/event-logs/stats/total` | Sum of active amounts. |
| `GET` | `/api/event-logs/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/event-logs \
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

## Newsletter

Base path: `/newsletters`.

A scheduled newsletter with a subject and audience.

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
| `subject` | string | Text value, at most 120 characters. |
| `audience` | string | Text value, at most 120 characters. |
| `scheduledAt` | string | ISO-8601 instant in UTC. |
| `sent` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/newsletters` | Create a record. Returns `201`. |
| `GET` | `/api/newsletters/{id}` | Read one record by id. |
| `GET` | `/api/newsletters/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/newsletters?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/newsletters/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/newsletters/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/newsletters/all-active` | Every active record. |
| `GET` | `/api/newsletters/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/newsletters/{id}` | Replace the editable fields. |
| `POST` | `/api/newsletters/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/newsletters/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/newsletters/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/newsletters/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/newsletters/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/newsletters/stats/active` | Count of active records. |
| `GET` | `/api/newsletters/stats/archived` | Count of archived records. |
| `GET` | `/api/newsletters/stats/total` | Sum of active amounts. |
| `GET` | `/api/newsletters/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/newsletters \
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

## Announcement

Base path: `/announcements`.

A broadcast notice that is published once.

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
| `title` | string | Text value, at most 120 characters. |
| `audience` | string | Text value, at most 120 characters. |
| `body` | string | Text value, at most 120 characters. |
| `published` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/announcements` | Create a record. Returns `201`. |
| `GET` | `/api/announcements/{id}` | Read one record by id. |
| `GET` | `/api/announcements/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/announcements?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/announcements/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/announcements/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/announcements/all-active` | Every active record. |
| `GET` | `/api/announcements/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/announcements/{id}` | Replace the editable fields. |
| `POST` | `/api/announcements/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/announcements/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/announcements/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/announcements/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/announcements/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/announcements/stats/active` | Count of active records. |
| `GET` | `/api/announcements/stats/archived` | Count of archived records. |
| `GET` | `/api/announcements/stats/total` | Sum of active amounts. |
| `GET` | `/api/announcements/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/announcements \
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

## Recipient List

Base path: `/recipient-lists`.

A named audience with a snapshot member count.

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
| `listName` | string | Text value, at most 120 characters. |
| `memberCount` | integer | Whole number. |
| `dynamic` | boolean | True or false flag. |
| `refreshedAt` | string | ISO-8601 instant in UTC. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/recipient-lists` | Create a record. Returns `201`. |
| `GET` | `/api/recipient-lists/{id}` | Read one record by id. |
| `GET` | `/api/recipient-lists/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/recipient-lists?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/recipient-lists/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/recipient-lists/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/recipient-lists/all-active` | Every active record. |
| `GET` | `/api/recipient-lists/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/recipient-lists/{id}` | Replace the editable fields. |
| `POST` | `/api/recipient-lists/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/recipient-lists/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/recipient-lists/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/recipient-lists/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/recipient-lists/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/recipient-lists/stats/active` | Count of active records. |
| `GET` | `/api/recipient-lists/stats/archived` | Count of archived records. |
| `GET` | `/api/recipient-lists/stats/total` | Sum of active amounts. |
| `GET` | `/api/recipient-lists/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/recipient-lists \
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

## Send Attempt

Base path: `/send-attempts`.

One provider call, with an increasing attempt number.

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
| `messageRef` | string | Text value, at most 120 characters. |
| `providerRef` | string | Text value, at most 120 characters. |
| `attemptNumber` | integer | Whole number. |
| `delivered` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/send-attempts` | Create a record. Returns `201`. |
| `GET` | `/api/send-attempts/{id}` | Read one record by id. |
| `GET` | `/api/send-attempts/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/send-attempts?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/send-attempts/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/send-attempts/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/send-attempts/all-active` | Every active record. |
| `GET` | `/api/send-attempts/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/send-attempts/{id}` | Replace the editable fields. |
| `POST` | `/api/send-attempts/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/send-attempts/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/send-attempts/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/send-attempts/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/send-attempts/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/send-attempts/stats/active` | Count of active records. |
| `GET` | `/api/send-attempts/stats/archived` | Count of archived records. |
| `GET` | `/api/send-attempts/stats/total` | Sum of active amounts. |
| `GET` | `/api/send-attempts/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/send-attempts \
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

## Suppression

Base path: `/suppressions`.

An address that must not be contacted until it expires.

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
| `email` | string | Text value, at most 120 characters. |
| `reason` | string | Text value, at most 120 characters. |
| `expiresAt` | string | ISO-8601 instant in UTC. |
| `active` | boolean | True or false flag. |

### Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/suppressions` | Create a record. Returns `201`. |
| `GET` | `/api/suppressions/{id}` | Read one record by id. |
| `GET` | `/api/suppressions/by-code/{code}` | Read one record by natural key. |
| `GET` | `/api/suppressions?text={text}` | Search by name, bounded by `Limits.PAGE_SIZE`. |
| `GET` | `/api/suppressions/top?limit={n}` | Highest-priority active records, capped by the caller's limit. |
| `GET` | `/api/suppressions/high-priority?minimum={n}` | Active or inactive records at or above a priority. |
| `GET` | `/api/suppressions/all-active` | Every active record. |
| `GET` | `/api/suppressions/exists/{code}` | `true` when the natural key exists. |
| `PUT` | `/api/suppressions/{id}` | Replace the editable fields. |
| `POST` | `/api/suppressions/{id}/rename?name={name}` | Change the display name. |
| `POST` | `/api/suppressions/{id}/amount?delta={amount}` | Add a signed delta to the amount. |
| `POST` | `/api/suppressions/{id}/activate` | Move the record to `ACTIVE`. |
| `POST` | `/api/suppressions/{id}/deactivate` | Move the record to `INACTIVE`. |
| `DELETE` | `/api/suppressions/{id}` | Archive the record. Returns `204`. |
| `GET` | `/api/suppressions/stats/active` | Count of active records. |
| `GET` | `/api/suppressions/stats/archived` | Count of archived records. |
| `GET` | `/api/suppressions/stats/total` | Sum of active amounts. |
| `GET` | `/api/suppressions/stats/average` | Mean of active amounts, two places. |

### Example

```bash
curl -X POST http://localhost:8080/api/suppressions \
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
mvn -pl notification-service -am test
```
