# Domain model

Each concept below has exactly one owning service. The owning service is the
only place allowed to change the record. Other services hold references.

## Catalog

| Concept | Purpose | Key invariants |
| --- | --- | --- |
| Product | Sellable item | Unique code, non-negative priority, amount in the catalog currency |
| ProductVariant | Size/colour/option of a product | Unique code, belongs to one product code |
| ProductImage | Image for a product | Has a URL and a content type |
| ProductTag | Free-form label | Slug is stable, colour is display-only |
| Category | Browsable grouping | Unique code, archive instead of delete |
| Collection | Curated set | Unique code, ordered by priority |
| Brand | Manufacturer or label | Unique code |
| Attribute | Reusable product property | Unique code, typed by convention |
| PriceList | Prices for a market | Currency and region are explicit |
| PriceBook | Price version for a channel | Has an effective-from instant |
| DiscountRule | Automatic discount | Percentage is 0-100, stacking is explicit |
| Promotion | Time-bound campaign | Unique code, archive when finished |
| Bundle | Set sold together | Item count is positive |
| Warranty | Coverage terms | Duration is non-negative |
| Supplier | Source of goods | Unique code |
| SupplierContract | Agreement with a supplier | Renewal notice is explicit |
| Review | Customer opinion | Rating and moderation follow service rules |
| SeoEntry | Search metadata | One entry per slug |
| CatalogImport | Import job record | Source, file and checksum are recorded |

Catalog owns the price a customer sees. Order and payment must not invent
prices; they carry a quote or snapshot produced by catalog.

## Inventory

| Concept | Purpose | Key invariants |
| --- | --- | --- |
| StockItem | Quantity on hand | One row per product and location |
| Warehouse | Physical location | Unique code, archive instead of delete |
| BinLocation | Shelf position | Capacity is non-negative |
| Lot | Traceable batch | Expiry is explicit, quarantine is explicit |
| SerialNumber | Individually tracked unit | Assigned flag is monotonic |
| Reservation | Promise of stock | Released or fulfilled exactly once |
| Adjustment | Manual correction | Requires a reason and approver |
| CycleCount | Scheduled count | Variance is recorded, not silently applied |
| InventoryAudit | Count session | Auditor and date are recorded |
| Replenishment | Suggested purchase | Supplier and lead time are explicit |
| SafetyStock | Minimum level | Reorder point at or above the minimum |
| Backorder | Unfilled demand | Promised date is explicit |
| Transfer | Stock movement between sites | Source and destination differ |
| StockTransferLine | One product in a transfer | Received quantity never exceeds sent |
| PickList | Work for a picker | Picked flag is monotonic |
| PutawayTask | Work for receiving | Completed flag is monotonic |
| Shipment | Outbound movement | Belongs to one order reference |

Stock is never decremented by a read. A reservation is a separate row so that
the reason for a shortfall remains visible.

## Order

| Concept | Purpose | Key invariants |
| --- | --- | --- |
| Cart | Pre-checkout basket | One active cart per customer |
| CheckoutSession | A checkout attempt | Expires; completion is monotonic |
| Order | Committed purchase | Immutable after confirmation except status |
| OrderLine | One product in an order | Quantity is positive |
| OrderNote | Internal note | Author is recorded |
| OrderApproval | Approval of a sensitive order | Decision is final |
| Quote | Priced proposal | Expires; converting creates an order |
| Fulfilment | Work to ship an order | One or more per order |
| Invoice | Billing document | Total equals the priced order |
| PaymentRef | Reference to a payment | Never stores card data |
| ReturnRequest | Request to return goods | Approved before refund |
| Exchange | Replacement of goods | Original and replacement are explicit |
| CancelRequest | Request to cancel | Approval is final |
| Wishlist | Saved items | Private unless shared |
| GiftCard | Stored value | Redemption is monotonic |
| Preorder | Order before release | Release date is explicit |
| Layaway | Instalment purchase | Paid amount never exceeds total |
| Subscription | Repeat order | Cadence and next date are explicit |
| ShipmentTracking | Customer-facing tracking | Latest event wins |

Order is the workflow owner. It calls catalog for prices, inventory for stock
and payment for money movement. It stores the outcome, not the remote state.

## Payment

| Concept | Purpose | Key invariants |
| --- | --- | --- |
| Payment | Attempt to move money | Amount is positive, currency explicit |
| PaymentMethod | Stored method reference | Only a token and last four digits |
| CardToken | Tokenised card | Expiry is explicit, no PAN |
| MerchantAccount | Account receiving funds | Provider and currency explicit |
| GatewayTransaction | Raw gateway attempt | Request id is recorded |
| FraudCheck | Risk decision | Score and decision are recorded |
| Chargeback | Disputed payment | Resolved exactly once |
| Dispute | Customer dispute | Resolution is final |
| Refund | Money returned | Never exceeds the captured amount |
| RefundLine | One line of a refund | References the original payment |
| Ledger | Double-entry record | Debits equal credits |
| Settlement | Provider payout statement | Total matches its lines |
| Payout | Money sent to a seller | Batched, then settled |
| PayoutBatch | Group of payouts | Count and total are consistent |
| TaxRule | Tax calculation rule | Jurisdiction and rate are explicit |
| CurrencyRate | Conversion rate | Timestamped, never edited |
| PaymentFee | Fee charged | Fee amount is non-negative |
| Wallet | Customer balance | Balance changes only through ledger entries |

Payment is append-only where money is involved. Corrections are new entries,
never edits to a posted ledger row.

## Customer

| Concept | Purpose | Key invariants |
| --- | --- | --- |
| Customer | Person or organisation | Unique code, PII minimised |
| Contact | Email/phone for a customer | One primary contact |
| Address | Delivery or billing address | Belongs to one customer |
| Consent | Permission to process data | Granted and withdrawn are timestamped |
| Preference | Display or contact preference | One value per key |
| CommunicationPreference | Channel opt-in | Opt-out wins |
| Segment | Group of customers | Membership is derived or explicit |
| CustomerGroup | Manual group | Slug is stable |
| CustomerTag | Label | Slug is stable |
| LoyaltyAccount | Points balance | Balance changes are transactions |
| LoyaltyTransaction | Points movement | Never edited |
| Referral | Referral relationship | Reward is granted once |
| CustomerNote | Support note | Author is recorded |
| Feedback | Free-form feedback | Rating in range when present |
| SurveyResponse | Structured survey | One response per survey per customer |
| SupportTicket | Support request | Duplicate code rejected |
| IdentityDocument | Verification artefact | Verified flag is monotonic |

The customer service stores the minimum PII needed to operate. Payment card
data never appears here.

## Shipping

| Concept | Purpose | Key invariants |
| --- | --- | --- |
| Carrier | Delivery company | Unique code |
| CarrierAccount | Our account with a carrier | Account number is not a secret |
| ShippingLabel | Printable label | Belongs to a shipment reference |
| TrackingEvent | Scan event | Occurred-at is ordered |
| Delivery | Delivery job | One per shipment attempt sequence |
| DeliveryAttempt | One attempt | Attempt number increases |
| Dispatch | Load leaving a site | Departed flag is monotonic |
| Manifest | Handover document | Matches its deliveries |
| PickupPoint | Collection location | Address is complete |
| Route | Delivery route | Stops are ordered |
| RoutePlan | Planned route | Stop count is positive |
| Zone | Pricing/delivery area | Postal range is explicit |
| Rate | Price for a service | Currency and amount explicit |
| FreightQuote | Quote for a shipment | Expires |
| PackagingType | Box or envelope | Max weight is positive |
| Vehicle | Transport asset | Capacity is explicit |
| CustomsDocument | Cross-border paperwork | Declared value is explicit |

Shipping owns delivery state. Order reads it and mirrors the latest public
status; it does not drive carrier state.

## Notification

| Concept | Purpose | Key invariants |
| --- | --- | --- |
| Message | One outbound message | One channel, one recipient |
| Template | Reusable content | Variables are declared |
| Campaign | Batch of messages | Audience is frozen at send time |
| Subscriber | Opted-in contact | Unsubscribe is monotonic |
| NotificationPreference | Per-customer channel choice | Explicit opt-out wins |
| Channel | Transport configuration | Provider and rate limit explicit |
| Digest | Batched summary | Window is explicit |
| DeliveryReport | Provider result | Terminal state is recorded |
| SendAttempt | One provider call | Attempt number increases |
| BouncedEmail | Hard or soft bounce | Address and type recorded |
| PushSubscription | Device registration | Token is unique |
| Webhook | Outbound callback | Secret is not stored in clear |
| EventLog | Delivery event history | Append-only |
| Newsletter | Scheduled newsletter | Scheduled time is explicit |
| Announcement | Broadcast notice | Published flag is monotonic |
| RecipientList | Named audience | Member count is a snapshot |
| Suppression | Address that must not be contacted | Expiry is explicit |

Notification is downstream of every other service. It may retry delivery, but
it must not create or mutate business transactions.

## Cross-service identifiers

| Field | Meaning |
| --- | --- |
| `code` | Natural key, unique inside a service, uppercase |
| `id` | Surrogate database key, never shared across services |
| `*Ref` | Opaque reference to another service's natural key |
| `status` | `ACTIVE`, `INACTIVE` or `ARCHIVED` |
| `priority` | Ordering hint, non-negative, higher first |
| `amount` | Generic monetary field in the service's currency |

References are strings on purpose. A service that stores a typed foreign key
starts to assume the other service's schema, which breaks ownership.
