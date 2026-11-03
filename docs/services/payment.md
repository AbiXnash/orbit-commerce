# Payment service

`payment-service` owns money movement: payments, refunds, payouts, fees, the
ledger, settlements, tax and stored method references.

## Package

```
com.orbit.payment
```

## Owned concepts

| Concept | Table | Notes |
| --- | --- | --- |
| Payment | `payment` | One attempt to move money |
| PaymentMethod | `payment_method` | Tokenised method reference |
| CardToken | `card_token` | Token and last four only |
| MerchantAccount | `merchant_account` | Receiving account |
| GatewayTransaction | `gateway_transaction` | Raw provider attempt |
| FraudCheck | `fraud_check` | Risk decision |
| Chargeback | `chargeback` | Reversed card payment |
| Dispute | `dispute` | Customer dispute |
| Refund | `refund` | Money returned |
| RefundLine | `refund_line` | Line of a refund |
| Ledger | `ledger` | Append-only double entry |
| Settlement | `settlement` | Provider statement |
| Payout | `payout` | Money to a seller |
| PayoutBatch | `payout_batch` | Group of payouts |
| TaxRule | `tax_rule` | Jurisdiction and rate |
| CurrencyRate | `currency_rate` | Timestamped conversion |
| PaymentFee | `payment_fee` | Provider or platform fee |
| Wallet | `wallet` | Customer balance |

## API

```
POST   /api/payments
GET    /api/payments/{id}
GET    /api/payments/by-code/{code}
GET    /api/payments?text=...
GET    /api/payments/stats/active
POST   /api/payments/{id}/activate
DELETE /api/payments/{id}
```

Refunds, payouts, settlements, chargebacks, tax rules, wallets and the newer
method, token, dispute, rate, fee and batch resources follow the same shape.

## Payment flow

1. Order sends an amount, currency and method token.
2. Payment records the attempt before calling the provider.
3. The gateway returns an authorisation or a decline.
4. A successful authorisation is captured, then posted to the ledger.
5. A failure is recorded with the provider code, not with card data.
6. Settlement groups captured payments and reconciles them against the
   provider statement.

## Ledger rules

- Every movement has a debit and a credit.
- Posted entries are never edited or deleted.
- A correction is a new compensating entry.
- A refund never exceeds the captured amount.
- A payout batch's total equals the sum of its payouts.
- Fees are recorded as their own line.
- Currency conversions record the rate used.

## Interactions

| Dependency | Interaction |
| --- | --- |
| Order | Create payment, refund |
| Customer | Method reference, billing profile |
| Notification | Receipt and failure messages |

Payment never calls catalog for a price. It uses the amount sent by order,
which is a snapshot from checkout.

## Failure modes

| Failure | Response |
| --- | --- |
| Declined | `409 PAYMENT_DECLINED` |
| Duplicate refund | `409 DUPLICATE_CODE` |
| Refund above capture | `409` conflict |
| Unknown payment | `404 NOT_FOUND` |
| Provider timeout | retry with the idempotency key |

## Testing

`PaymentServiceTest` covers creation and the decline path. `RefundServiceTest`
covers the refund ceiling. `LedgerServiceTest` covers the balanced-entry rule.
The generated domains add coverage for methods, tokens, fees, disputes and
payout batches.

## Operations

- A decline spike is usually a provider or issuer problem, not a code deploy.
- Reconciliation runs daily; unmatched entries are investigated before payout.
- Ledger imbalances page immediately.
- Card data must never appear in a log or an error body.

## Runbook

See [payment decline spike](../runbooks/payment-decline-spike.md).

## Open work

- Add idempotency storage at the payment boundary.
- Add a settlement line table for reconciliation detail.
- Add persistence tests for the ledger balance query.
