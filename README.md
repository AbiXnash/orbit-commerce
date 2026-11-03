# Orbit Commerce

An online store built as Spring Boot microservices.

## Services

| Service | Responsibility | Package |
| --- | --- | --- |
| `catalog-service` | Products, variants, pricing, merchandising | `com.orbit.catalog` |
| `inventory-service` | Stock, warehouses, reservations, replenishment | `com.orbit.inventory` |
| `order-service` | Carts, checkout, orders, returns, subscriptions | `com.orbit.order` |
| `payment-service` | Payments, refunds, payouts, ledger, tax | `com.orbit.payment` |
| `customer-service` | Customers, addresses, consent, loyalty | `com.orbit.customer` |
| `shipping-service` | Carriers, labels, routes, delivery | `com.orbit.shipping` |
| `notification-service` | Messages, templates, campaigns, webhooks | `com.orbit.notification` |
| `common-lib` | Shared errors, value types and helpers | `com.orbit.common` |

## Build

```bash
mvn test          # run every service's tests
mvn -pl catalog-service -am test
mvn -pl catalog-service -am spring-boot:run
```

Java 21 and Maven 3.9+ are required.

## Documentation

Start at [docs/README.md](docs/README.md).

- [Architecture](docs/architecture.md)
- [Domain model](docs/domain-model.md)
- [API conventions](docs/api-conventions.md)
- [API reference](docs/api/README.md)
- [Development guide](docs/development.md)
- [Testing guide](docs/testing.md)
- [Operations](docs/operations.md)
- [Data and migrations](docs/data.md)
- [Security](docs/security.md)
- [Glossary](docs/glossary.md)
- [Architecture decisions](docs/adr/0001-record-architecture-decisions.md)

## Ownership

Owned by the platform team. See [CHANGELOG.md](CHANGELOG.md) for each release.
