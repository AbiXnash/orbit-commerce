# Glossary

| Term | Meaning |
| --- | --- |
| **ADR** | Architecture decision record; a short document capturing a choice |
| **Aggregate** | A cluster of records changed together inside one service |
| **Archive** | Soft delete; the record stays readable with status `ARCHIVED` |
| **Backorder** | Demand that cannot be met from current stock |
| **Bin** | A physical shelf position inside a warehouse |
| **Bundle** | Several products sold as one unit |
| **Capture** | Taking authorised money from a payment method |
| **Carrier** | A company that physically delivers shipments |
| **Chargeback** | A card payment reversed by the issuer after a dispute |
| **Code** | The natural key of a record, unique inside its service |
| **Compensation** | An explicit action that undoes an earlier cross-service step |
| **Consent** | A recorded permission to process personal data |
| **Cycle count** | A partial stock count used to verify inventory |
| **Digest** | A batched notification summarising several events |
| **Dispatch** | The moment a load leaves a site |
| **Entity** | A persisted record with identity and lifecycle |
| **Fulfilment** | The work required to deliver an order |
| **Gateway** | The adapter that hides a remote service from local rules |
| **Idempotency key** | A client-supplied key that makes a retry safe |
| **Instant** | A point in time, stored and rendered in UTC |
| **Label** | The printable carrier document for a shipment |
| **Ledger** | An append-only record of money movements |
| **Lot** | A traceable batch of stock |
| **Manifest** | The document listing a handover to a carrier |
| **Natural key** | A business identifier such as `code` |
| **Optimistic locking** | Detecting concurrent writes with a version column |
| **Outbox** | A table of events to publish, written with the business change |
| **Payout** | Money sent from the platform to a seller |
| **Pick list** | The list of items a picker collects |
| **PII** | Personally identifiable information |
| **Preorder** | An order placed before a product is released |
| **Projection** | A response shape assembled from an entity without exposing it |
| **Promotion** | A time-bound commercial campaign |
| **Putaway** | Moving received stock to its storage location |
| **Reservation** | A promise of stock for an order |
| **Settlement** | A provider statement of money moved |
| **SKU** | Stock keeping unit; a sellable variant |
| **Slug** | A URL-safe text identifier |
| **Snapshot** | A copied value captured at a point in time |
| **Subscriber** | A contact who opted in to messages |
| **Suppression** | An address that must not be contacted |
| **Surrogate key** | The database `id`, never shared across services |
| **Tenant** | An isolated merchant or organisation |
| **Transfer** | Stock movement between two sites |
| **Variant** | A concrete option combination of a product |
| **Wallet** | A customer balance backed by ledger entries |
| **Webhook** | An HTTP callback sent to an external system |
| **Zone** | A geographic area used for shipping price and promise |
