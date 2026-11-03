# Inventory service

`inventory-service` owns stock: what is on hand, where it is, what is promised,
what is moving and what must be reordered.

## Package

```
com.orbit.inventory
```

## Owned concepts

| Concept | Table | Notes |
| --- | --- | --- |
| StockItem | `stock_item` | Quantity per product and location |
| Warehouse | `warehouse` | Physical site |
| BinLocation | `bin_location` | Shelf position |
| Lot | `lot` | Traceable batch |
| SerialNumber | `serial_number` | Individually tracked unit |
| Reservation | `reservation` | Promise of stock |
| Adjustment | `adjustment` | Manual correction with reason |
| CycleCount | `cycle_count` | Partial count |
| InventoryAudit | `inventory_audit` | Count session |
| Replenishment | `replenishment` | Purchase suggestion |
| SafetyStock | `safety_stock` | Minimum and reorder point |
| Backorder | `backorder` | Unfilled demand |
| Transfer | `transfer` | Movement between sites |
| StockTransferLine | `stock_transfer_line` | Product line in a transfer |
| PickList | `pick_list` | Picker work |
| PutawayTask | `putaway_task` | Receiving work |
| Shipment | `shipment` | Outbound movement |

## API

All resources follow [the API conventions](../api-conventions.md):

```
POST   /api/stock-items
GET    /api/stock-items/{id}
GET    /api/stock-items/by-code/{code}
GET    /api/stock-items?text=...
GET    /api/stock-items/top?limit=10
GET    /api/stock-items/stats/active
PUT    /api/stock-items/{id}
DELETE /api/stock-items/{id}
```

Warehouses, bins, lots, serials, alerts, pick lists, putaway tasks, audits,
backorders, transfers, transfer lines and safety stock follow the same shape.

## Reservation flow

1. Order asks inventory to reserve a quantity for a product.
2. Inventory checks available stock, not total stock.
3. A reservation row is written with an expiry.
4. The reservation is later fulfilled or released exactly once.
5. Expired reservations are released by a scheduled job.

Available stock is on-hand minus open reservations. A stock adjustment never
edits a reservation; it changes on-hand and records the reason.

## Interactions

| Caller | Interaction |
| --- | --- |
| Order | Reserve, release, check availability |
| Shipping | Read pick list and shipment state |
| Catalog | Read product reference |
| Notification | Alert on low stock or delay |

Inventory does not price anything. It returns quantities and locations.

## Rules

- A quantity movement always has a reason.
- An adjustment names the approver.
- A transfer's source and destination differ.
- A received quantity never exceeds the sent quantity.
- A serial is assigned once.
- A quarantined lot is excluded from available stock.
- A reservation is released or fulfilled exactly once.
- A picked or completed flag is monotonic.

## Failure modes

| Failure | Response |
| --- | --- |
| Insufficient stock | `409 OUT_OF_STOCK` |
| Duplicate reservation | `409 DUPLICATE_CODE` |
| Missing item | `404 NOT_FOUND` |
| Update after archive | `409 NOT_ACTIVE` |
| Negative quantity | `400` validation |

## Testing

`AdjustmentServiceTest` covers create, duplicate, missing and archived rules.
`ReplenishmentServiceTest` additionally covers the missing-item path after the
`findById` fix. `StockItemServiceTest` covers the stock rules used by order.

## Operations

- Reservation expiry runs on a schedule and must be idempotent.
- A reservation leak shows up as declining available stock with flat on-hand.
- Cycle counts are read-only until approved; approval is audited.
- Warehouse and bin changes are low-volume and can be applied immediately.

## Runbook

See [stock reservation leak](../runbooks/stock-reservation-leak.md).

## Open work

- Add a unique constraint for one open reservation per order line.
- Add persistence tests for the expiry query.
- Expose a bounded available-stock endpoint for the storefront.
