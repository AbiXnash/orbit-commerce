# Architecture

Orbit Commerce is a set of Spring Boot services that each own a slice of the
commerce domain. Services are independently buildable Maven modules under the
same repository. They communicate over HTTP with small, explicit contracts.

## System map

```
                       +-------------------+
                       |   storefront      |
                       +---------+---------+
                                 |
             +-------------------+-------------------+
             |                   |                   |
     +-------v-------+   +-------v-------+   +-------v--------+
     | catalog       |   | order         |   | customer       |
     | service       |   | service       |   | service        |
     +-------+-------+   +---+-------+---+   +-------+--------+
             |               |       |               |
             |       +-------v---+   |       +-------v--------+
             |       | payment   |   |       | consent,       |
             |       | service   |   |       | loyalty        |
             |       +-----+-----+   |       +----------------+
             |             |         |
     +-------v-------+     |   +-----v---------+
     | inventory     |<----+   | shipping      |
     | service       |         | service       |
     +-------+-------+         +-------+-------+
             |                         |
             +-----------+-------------+
                         |
                 +-------v--------+
                 | notification   |
                 | service        |
                 +----------------+
```

Every arrow is an HTTP call. A service never reaches into another service's
database. The shared library contains value types, error handling and text
helpers, not business rules.

## Module layout

```
orbit-commerce/
  pom.xml                 aggregator and dependency management
  common-lib/             shared kernel (no business entities)
  catalog-service/
  inventory-service/
  order-service/
  payment-service/
  customer-service/
  shipping-service/
  notification-service/
```

Each service follows the same package shape:

```
com.orbit.<service>/
  <service>Application.java
  <domain>/
    <Entity>.java
    <Entity>Repository.java
    <Entity>Service.java
    <Entity>Controller.java
    Create<Entity>Request.java
    <Entity>Response.java
  client/
    <Remote>Client.java
    <Remote>Gateway.java
```

A domain package is the unit of ownership. It contains the entity, its
persistence port, the rules, the transport adapter and its two DTOs.

## Request flow

A synchronous read looks like this:

1. The storefront calls `GET /api/products/{id}` on the catalog service.
2. `ProductController` validates path variables and delegates.
3. `ProductService` applies read-only rules and loads the entity.
4. `ProductRepository` reads from the catalog database.
5. `ProductResponse` is assembled. Entities never leave the service.

A synchronous write follows the same path with three additions: validation of
the request body, an explicit business-rule check, and a transactional
boundary on the service method.

A cross-service read goes through a gateway:

1. `OrderService` needs a product snapshot.
2. `OrderService` calls `ProductGateway`, not Feign directly.
3. `ProductGateway` adapts the remote DTO into a small local record.
4. A timeout and a retry budget are applied at the gateway.

Keeping the gateway as the only seam makes it possible to test order rules
without a catalog service and to replace the transport later.

## Data ownership

| Data | Owner |
| --- | --- |
| Product, variant, price book, promotion | catalog |
| Stock item, reservation, warehouse, lot | inventory |
| Cart, order, return, subscription | order |
| Payment, refund, ledger, settlement | payment |
| Customer, address, consent, loyalty | customer |
| Carrier, label, route, delivery | shipping |
| Message, template, campaign, webhook | notification |

A service may store a *reference* to another service's data (for example an
order keeps a product code) but never a mutable copy of the authoritative
record. References are resolved at the edge or refreshed through a gateway.

## Consistency

The default is per-service ACID inside one database. Across services the system
is eventually consistent.

- Writes that must be atomic are kept inside one service.
- A workflow that spans services is expressed as a sequence of idempotent
  commands.
- Each mutable entity carries a `@Version` column, so concurrent updates fail
  with a conflict instead of silently overwriting.
- A repeated command with the same natural key (usually `code`) returns the
  duplicate-code conflict rather than creating a second record.

There is no distributed transaction. If a later step fails, the earlier step
is compensated by an explicit command such as a refund or a stock release.

## Failure handling

| Failure | Behaviour |
| --- | --- |
| Unknown id | `404` with `NOT_FOUND` |
| Duplicate natural key | `409` with `DUPLICATE_CODE` |
| Update of archived record | `409` with `NOT_ACTIVE` |
| Declined payment | `409` with `PAYMENT_DECLINED` |
| Missing stock | `409` with `OUT_OF_STOCK` |
| Invalid request body | `400` from bean validation |
| Remote timeout | gateway raises a retryable failure; no partial write |

`ApiExceptionHandler` in `common-lib` turns the two domain exceptions into the
standard error body. Services add handlers only for failures unique to them.

## Scaling model

Services are stateless. Any instance can serve any request. State lives in the
database, in the outbox and in the notification queue. This allows horizontal
scaling behind a load balancer and simple rolling deployments.

The read path is optimised first: indexed natural keys, paged searches and
projection DTOs. Writes are deliberately small and transactional. Long work
such as sending a campaign or producing a settlement batch is represented as a
record with a status, not as a request that blocks.

## What is deliberately absent

- No shared database and no cross-service joins.
- No service discovery code in the repository; deployment supplies addresses.
- No business logic in `common-lib`.
- No entity is serialised directly to JSON.
- No hidden background threads inside request handlers.

These omissions keep the codebase easy to reason about and easy to test. They
are revisited in the ADRs when a requirement forces a change.
