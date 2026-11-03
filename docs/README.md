# Documentation

This directory holds the design, operating and contribution notes for Orbit
Commerce. It is organised so that a new engineer can move from the system map
to a single service without reading every source file.

## Start here

| Document | Read it when |
| --- | --- |
| [Architecture](architecture.md) | You need the system map and request flows |
| [Domain model](domain-model.md) | You need to know which service owns a concept |
| [API conventions](api-conventions.md) | You are adding or calling an endpoint |
| [Development guide](development.md) | You are setting up or adding a domain |
| [Testing guide](testing.md) | You are writing tests or debugging the suite |
| [Data and migrations](data.md) | You are changing persistence |
| [Security](security.md) | You handle credentials, PII or payments |
| [Operations](operations.md) | You are on call or deploying |
| [Glossary](glossary.md) | A term in a ticket is unfamiliar |
| [Roadmap](roadmap.md) | You are planning the next slice |

## API reference

- [API reference index](api/README.md)
- [Catalog](api/catalog.md)
- [Inventory](api/inventory.md)
- [Order](api/order.md)
- [Payment](api/payment.md)
- [Customer](api/customer.md)
- [Shipping](api/shipping.md)
- [Notification](api/notification.md)

## Service guides

- [Catalog](services/catalog.md)
- [Inventory](services/inventory.md)
- [Order](services/order.md)
- [Payment](services/payment.md)
- [Customer](services/customer.md)
- [Shipping](services/shipping.md)
- [Notification](services/notification.md)

## Runbooks

- [High error rate](runbooks/high-error-rate.md)
- [Payment decline spike](runbooks/payment-decline-spike.md)
- [Stock reservation leak](runbooks/stock-reservation-leak.md)
- [Notification backlog](runbooks/notification-backlog.md)

## Decisions

Architecture decisions live in [adr/](adr/). Each record states the context,
the decision and the consequences. Superseding an ADR means adding a new one
and linking both directions; old records are never rewritten.

## Keeping docs current

A change that alters a public endpoint, a stored field or an operational step
must update the matching document in the same pull request. Documentation is
reviewed like code. Generated history and incident notes belong outside this
directory.
