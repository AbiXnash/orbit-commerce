# API conventions

All services expose JSON over HTTP. The conventions below are mandatory for
new endpoints and are enforced by review.

## Base path

```
/api/<plural-resource>
```

Examples: `/api/products`, `/api/product-variants`, `/api/stock-items`.
Use kebab-case for multi-word resources. Never put a verb in the base path.

## Verbs

| Verb | Meaning | Success |
| --- | --- | --- |
| `GET` | Read, never mutates | `200` |
| `POST` | Create | `201` |
| `PUT` | Replace the editable fields | `200` |
| `DELETE` | Archive, never physically delete | `204` |

`DELETE` is an archive in this codebase. The record remains readable, but its
status becomes `ARCHIVED` and further updates are rejected.

## Standard endpoints

Every domain in a service follows the same shape:

```
POST   /api/products
GET    /api/products/{id}
GET    /api/products/by-code/{code}
GET    /api/products?text={text}
GET    /api/products/top?limit={n}
GET    /api/products/high-priority?minimum={n}
GET    /api/products/all-active
GET    /api/products/exists/{code}
PUT    /api/products/{id}
POST   /api/products/{id}/rename?name={name}
POST   /api/products/{id}/amount?delta={amount}
POST   /api/products/{id}/activate
POST   /api/products/{id}/deactivate
DELETE /api/products/{id}
GET    /api/products/stats/active
GET    /api/products/stats/archived
GET    /api/products/stats/total
GET    /api/products/stats/average
```

Actions use `POST` because they change state. Query parameters are used only
for simple scalars; a body is used when a request has more than two fields.

## Request bodies

Create and update share one request record. Absent optional fields keep their
current value; the code is not editable after creation.

```json
{
  "code": "SKU-001",
  "name": "Trail jacket",
  "priority": 5,
  "amount": 129.00,
  "brand": "North"
}
```

Validation happens before the service is called:

| Rule | Annotation |
| --- | --- |
| Required text | `@NotBlank` |
| Maximum length | `@Size(max = ...)` |
| Non-negative integer | `@Min(0)` |
| Body is present | `@Valid @RequestBody` |

A failed validation returns `400` with the standard error body.

## Response bodies

Responses are records, not entities. They always include:

- `id`
- `code`
- `name`
- `status`
- `priority`
- `amount`
- `createdAt`, `updatedAt`
- the domain-specific fields

Timestamps are ISO-8601 instants in UTC, for example
`2024-05-01T09:30:00Z`.

## Errors

The error body is stable:

```json
{
  "code": "DUPLICATE_CODE",
  "message": "That code is already in use: SKU-001"
}
```

| HTTP | Code | When |
| --- | --- | --- |
| `400` | `INVALID_REQUEST` | Malformed or invalid request |
| `404` | `NOT_FOUND` | No record for the id or code |
| `409` | `DUPLICATE_CODE` | Natural key already exists |
| `409` | `NOT_ACTIVE` | Record is archived |
| `409` | `PAYMENT_DECLINED` | Provider declined |
| `409` | `OUT_OF_STOCK` | Reservation cannot be met |

Never return a stack trace, an entity, or an internal class name. Error
messages name the value that failed, not the SQL that failed.

## Paging

List endpoints that can grow return a bounded page. The shared paging types
document the contract:

```json
{
  "items": [],
  "total": 0,
  "page": 0,
  "size": 20
}
```

`page` is zero-based. `size` is capped at 200. A request outside the range is a
`400`, not a silent clamp. Search helpers that use `Limits.PAGE_SIZE` cap their
result in the service layer.

## Idempotency

`GET`, `PUT` and `DELETE` are idempotent. `POST` is not, except for actions
whose implementation is naturally idempotent (activate, deactivate, archive).

Commands that move money or stock must accept an idempotency key at the edge
and record it before performing the write. A repeated key returns the original
result.

## Naming

- JSON fields are camelCase.
- Database columns are snake_case.
- Java packages are lowercase, with no separators inside a compound domain
  name (`loyaltyaccount`, not `loyalty_account`).
- Resource paths are kebab-case plural.
- Error codes are SCREAMING_SNAKE_CASE.

## Versioning

The path is unversioned while the storefront and services ship together. A
breaking change requires one of:

1. An additive field with a safe default, or
2. A new path such as `/api/v2/products`, with the old path kept for one
   release.

Removing a field is always breaking. Deprecate it in the changelog first.

## Compatibility rules

- Adding an optional request field is compatible.
- Adding a response field is compatible for JSON clients.
- Renaming a field is breaking.
- Changing a field's type is breaking.
- Changing a status code is breaking.

When in doubt, add and deprecate rather than rename.
