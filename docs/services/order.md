# Order service

`order-service` owns the buying workflow: carts, checkout, orders, fulfilments,
invoices, returns, subscriptions and the surrounding order actions.

## Package

```
com.orbit.order
```

## Owned concepts

| Concept | Table | Notes |
| --- | --- | --- |
| Cart | `cart` | Pre-checkout basket |
| CheckoutSession | `checkout_session` | One checkout attempt |
| Order | `orders` | Committed purchase |
| OrderLine | `order_line` | One product in an order |
| OrderNote | `order_note` | Internal note |
| OrderApproval | `order_approval` | Sensitive-order approval |
| Quote | `quote` | Priced proposal |
| Fulfilment | `fulfilment` | Work to ship |
| Invoice | `invoice` | Billing document |
| ReturnRequest | `return_request` | Return workflow |
| Exchange | `exchange` | Replacement workflow |
| CancelRequest | `cancel_request` | Cancellation workflow |
| Wishlist | `wishlist` | Saved items |
| GiftCard | `gift_card` | Stored value |
| Preorder | `preorder` | Order before release |
| Layaway | `layaway` | Instalment purchase |
| ShipmentTracking | `shipment_tracking` | Customer-facing tracking |
| Subscription | `subscription` | Repeat order |

## API

The order resource is the reference:

```
POST   /api/orders
GET    /api/orders/{id}
GET    /api/orders/by-code/{code}
GET    /api/orders?text=...
GET    /api/orders/top?limit=10
PUT    /api/orders/{id}
POST   /api/orders/{id}/activate
POST   /api/orders/{id}/deactivate
DELETE /api/orders/{id}
GET    /api/orders/stats/active
```

Carts, checkout sessions, lines, notes, approvals, quotes, fulfilments,
invoices, returns, exchanges, cancellations, wishlists, gift cards, preorders,
layaways, tracking and subscriptions use the same conventions.

## Checkout flow

1. The cart holds product references and quantities.
2. Checkout resolves prices from catalog and reserves stock from inventory.
3. Payment is authorised and captured.
4. The order is created with a snapshot of prices and lines.
5. A fulfilment is created and handed to shipping.
6. Notification sends the confirmation.

If a later step fails, the earlier steps are compensated explicitly: release
the reservation, refund the payment, mark the order cancelled. There is no
distributed transaction.

## Interactions

| Dependency | Interaction |
| --- | --- |
| Catalog | Price and product snapshot |
| Inventory | Reserve, release, availability |
| Payment | Authorise, capture, refund |
| Customer | Address and contact |
| Shipping | Create delivery, read tracking |
| Notification | Confirmation and status messages |

## Rules

- An order's total equals the sum of its priced lines plus tax and shipping.
- A line quantity is positive.
- An order note records its author.
- An approval decision is final.
- A return is approved before a refund.
- A gift card is redeemed once per order.
- A layaway's paid amount never exceeds its total.
- A subscription has a cadence and a next date.
- An archived order cannot be edited.
- Cancellation and exchange are workflows, not a status flip.

## Failure modes

| Failure | Response |
| --- | --- |
| Missing product | `404 NOT_FOUND` |
| Out of stock | `409 OUT_OF_STOCK` |
| Payment declined | `409 PAYMENT_DECLINED` |
| Duplicate order code | `409 DUPLICATE_CODE` |
| Edit after archive | `409 NOT_ACTIVE` |

## Testing

`OrderServiceTest` covers the order rules. `QuoteServiceTest` covers expiry and
conversion. The generated domains add service and web tests for each concept.

## Operations

- Order creation is the most important write path; alert on its rate.
- A drop in order creation is an incident even when latency looks healthy.
- Reconciliation runs daily against payment and inventory.
- Orphaned reservations are the usual cause of a stuck order.

## Open work

- Add an outbox for order events.
- Add idempotency keys to checkout submission.
- Split the order state machine into an explicit transition table.
