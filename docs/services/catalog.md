# Catalog service

`catalog-service` owns everything a customer can browse and buy: products,
their variants, merchandising, prices and metadata. It is the authority for the
price shown before checkout.

## Package

```
com.orbit.catalog
```

## Owned concepts

| Concept | Table | Notes |
| --- | --- | --- |
| Product | `product` | Root sellable item |
| ProductVariant | `product_variant` | Option combination, own SKU |
| ProductImage | `product_image` | Ordered media |
| ProductTag | `product_tag` | Free-form label |
| Category | `category` | Browsable tree node |
| Collection | `collection` | Curated merchandising set |
| Brand | `brand` | Manufacturer or label |
| Attribute | `attribute` | Reusable property definition |
| PriceList | `price_list` | Market-specific prices |
| PriceBook | `price_book` | Price version by channel |
| DiscountRule | `discount_rule` | Automatic discount |
| Promotion | `promotion` | Time-bound campaign |
| Bundle | `bundle` | Products sold together |
| Warranty | `warranty` | Coverage terms |
| Supplier | `supplier` | Source of goods |
| SupplierContract | `supplier_contract` | Agreement terms |
| Review | `review` | Customer opinion |
| SeoEntry | `seo_entry` | Search metadata |
| CatalogImport | `catalog_import` | Import job record |

## API

Every resource follows [the API conventions](../api-conventions.md). The
product resource is the reference:

```
POST   /api/products
GET    /api/products/{id}
GET    /api/products/by-code/{code}
GET    /api/products?text=jacket
GET    /api/products/top?limit=10
GET    /api/products/high-priority?minimum=2
GET    /api/products/all-active
GET    /api/products/exists/{code}
PUT    /api/products/{id}
POST   /api/products/{id}/rename?name=...
POST   /api/products/{id}/amount?delta=...
POST   /api/products/{id}/activate
POST   /api/products/{id}/deactivate
DELETE /api/products/{id}
GET    /api/products/stats/active
GET    /api/products/stats/archived
GET    /api/products/stats/total
GET    /api/products/stats/average
```

The same shape is used by variants, images, tags, price books, discount rules,
bundles, warranties, contracts, SEO entries and imports.

## Pricing flow

1. The storefront requests a product by code or id.
2. Catalog resolves the active price book for the customer's market.
3. A matching discount rule is applied in priority order.
4. The response carries the final amount and the currency context.
5. Order stores the amount as a snapshot; it does not re-price at payment time.

A price change after checkout does not change an existing order. A refund uses
the captured amount.

## Interactions

| Caller | Interaction |
| --- | --- |
| Order | Read product and price snapshot |
| Inventory | Read product code and status |
| Notification | Read product details for campaigns |
| Shipping | Read dimensions or weight where configured |

Catalog does not call other services on the read path. This keeps browsing
available when order or payment is degraded.

## Rules

- `code` is unique and uppercase.
- `priority` is non-negative; higher sorts first.
- Archiving is final for updates; activation is explicit.
- A price must have a currency and a non-negative amount.
- A discount percentage is between 0 and 100.
- An import records source, file and checksum before it changes a product.
- Reviews are moderated; an unmoderated review is not public.

## Failure modes

| Failure | Response |
| --- | --- |
| Duplicate code | `409 DUPLICATE_CODE` |
| Missing product | `404 NOT_FOUND` |
| Update after archive | `409 NOT_ACTIVE` |
| Invalid discount | `400` validation |
| Duplicate import | `409` with the checksum |

## Testing

`ProductServiceTest` covers the create, duplicate, missing and archived rules.
`ProductControllerTest` covers create, read, list, update, archive and the
not-found mapping. The generated domains follow the same pair.

## Operations

- Read-heavy; scale replicas before scaling writes.
- Import jobs are bounded and resumable.
- The search query is capped by `Limits.PAGE_SIZE`.
- A catalog outage blocks new carts but not existing orders.

## Open work

- Move search to a read model when the product table outgrows a simple query.
- Add a price history table if audit requires it.
- Add persistence tests for the natural-key and status queries.
