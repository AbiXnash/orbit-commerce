# Shipping service

`shipping-service` owns physical delivery: carriers, labels, routes, dispatch,
attempts, zones, rates and customs paperwork.

## Package

```
com.orbit.shipping
```

## Owned concepts

| Concept | Table | Notes |
| --- | --- | --- |
| Carrier | `carrier` | Delivery company |
| CarrierAccount | `carrier_account` | Our account with a carrier |
| ShippingLabel | `shipping_label` | Printable label |
| TrackingEvent | `tracking_event` | Scan event |
| Delivery | `delivery` | Delivery job |
| DeliveryAttempt | `delivery_attempt` | One attempt |
| Dispatch | `dispatch` | Load leaving a site |
| Manifest | `manifest` | Handover document |
| PickupPoint | `pickup_point` | Collection location |
| Route | `route` | Delivery route |
| RoutePlan | `route_plan` | Planned route |
| Zone | `zone` | Price and promise area |
| Rate | `rate` | Service price |
| FreightQuote | `freight_quote` | Quote for a shipment |
| PackagingType | `packaging_type` | Box or envelope |
| Vehicle | `vehicle` | Transport asset |
| CustomsDocument | `customs_document` | Cross-border paperwork |

## API

```
POST   /api/deliveries
GET    /api/deliveries/{id}
GET    /api/deliveries/by-code/{code}
GET    /api/deliveries?text=...
GET    /api/deliveries/stats/active
PUT    /api/deliveries/{id}
DELETE /api/deliveries/{id}
```

Carriers, labels, tracking events, zones, accounts, packaging, attempts,
dispatches, route plans, quotes and customs documents follow the same shape.

## Delivery flow

1. Order creates a fulfilment and asks shipping for a delivery.
2. Shipping selects a carrier, service and rate for the destination zone.
3. A label is produced and stored.
4. Dispatch records the handover and creates a manifest.
5. Tracking events update the public status.
6. A failed attempt creates a new attempt with an outcome.
7. Delivery completes when the carrier reports delivery.

## Interactions

| Dependency | Interaction |
| --- | --- |
| Order | Read order reference and lines |
| Inventory | Read warehouse and pick state |
| Notification | Send tracking updates |
| Carrier API | Label, pickup, tracking |

## Rules

- A label belongs to one shipment reference.
- Attempt numbers only increase.
- A dispatch's departed flag is monotonic.
- A manifest matches the deliveries handed over.
- A route's stops are ordered.
- A zone's postal range does not overlap another zone in the same region.
- A quote expires.
- A customs document records the declared value.
- Delivery status only moves forward.

## Failure modes

| Failure | Response |
| --- | --- |
| Missing delivery | `404 NOT_FOUND` |
| Duplicate label | `409 DUPLICATE_CODE` |
| Unknown zone | `400` validation |
| Carrier timeout | retry, then surface a delayed status |
| Update after archive | `409 NOT_ACTIVE` |

## Testing

`DeliveryServiceTest` covers delivery creation. `RateServiceTest` covers rate
selection. `RouteServiceTest` covers stop ordering. The generated domains add
coverage for labels, tracking, zones, accounts, attempts, dispatch and customs.

## Operations

- Tracking ingest is bursty; buffer at the edge if a carrier retries.
- A stuck dispatch usually means a missing manifest.
- Zone changes affect price; review before peak.
- Carrier outages are handled by switching the active account, not by editing
  history.

## Open work

- Add a carrier health check and automatic failover.
- Add persistence tests for the zone overlap rule.
- Add a bounded tracking timeline endpoint.
