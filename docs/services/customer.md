# Customer service

`customer-service` owns identity and relationship data: customers, addresses,
contact details, consent, loyalty, segments and support context.

## Package

```
com.orbit.customer
```

## Owned concepts

| Concept | Table | Notes |
| --- | --- | --- |
| Customer | `customer` | Person or organisation |
| Contact | `contact` | Email and phone |
| Address | `address` | Delivery or billing |
| Consent | `consent` | Permission to process data |
| Preference | `preference` | Display or contact choice |
| CommunicationPreference | `communication_preference` | Channel opt-in |
| Segment | `segment` | Derived or explicit group |
| CustomerGroup | `customer_group` | Manual group |
| CustomerTag | `customer_tag` | Label |
| LoyaltyAccount | `loyalty_account` | Points balance |
| LoyaltyTransaction | `loyalty_transaction` | Points movement |
| Referral | `referral` | Referral relationship |
| CustomerNote | `customer_note` | Support note |
| Feedback | `feedback` | Free-form feedback |
| SurveyResponse | `survey_response` | Structured survey |
| SupportTicket | `support_ticket` | Support request |
| IdentityDocument | `identity_document` | Verification artefact |

## API

```
POST   /api/customers
GET    /api/customers/{id}
GET    /api/customers/by-code/{code}
GET    /api/customers?text=...
GET    /api/customers/stats/active
PUT    /api/customers/{id}
DELETE /api/customers/{id}
```

Addresses, contacts, consent, preferences, segments, groups, tags, loyalty,
referrals, notes, feedback, surveys, tickets and identity documents follow the
same shape.

## Consent flow

1. A customer grants or withdraws consent for a purpose.
2. The service records the decision with a timestamp.
3. Notification checks consent before sending a marketing message.
4. Withdrawal takes effect immediately for new sends.
5. Consent history is retained as proof.

A missing consent record is treated as no consent for marketing.

## Loyalty flow

1. A qualifying order or referral creates a loyalty transaction.
2. The account balance is the sum of its transactions.
3. Transactions are append-only; corrections are new entries.
4. Redemption checks the available balance inside one transaction.

## Interactions

| Caller | Interaction |
| --- | --- |
| Order | Address and contact snapshot |
| Payment | Billing profile reference |
| Notification | Contact, channel preference, consent |
| Shipping | Delivery address |
| Support tools | Customer context and notes |

## Rules

- `code` is unique.
- A customer has at most one primary contact.
- Address and consent records belong to exactly one customer.
- Opt-out wins over opt-in for the same channel.
- A loyalty balance never goes negative through a redemption.
- A referral reward is granted once.
- A support ticket rejects a duplicate code.
- A support ticket update after archive is rejected.
- Identity verification is monotonic.

## Failure modes

| Failure | Response |
| --- | --- |
| Duplicate customer code | `409 DUPLICATE_CODE` |
| Duplicate support ticket | `409 DUPLICATE_CODE` |
| Missing customer | `404 NOT_FOUND` |
| Update after archive | `409 NOT_ACTIVE` |
| Withdrawn consent | message suppressed, not an error |

## Testing

`CustomerServiceTest` covers the customer rules. `ConsentServiceTest` covers
grant and withdraw. `SupportTicketServiceTest` covers duplicate rejection and
the negative-priority rule. The generated domains add coverage for contacts,
groups, tags, referrals, feedback, surveys and identity documents.

## Operations

- PII access is audited.
- Deletion requests anonymise rather than remove financial history.
- A support-ticket spike often follows a shipping or payment incident.
- Consent changes must propagate to notification before the next campaign.

## Open work

- Add a consent audit endpoint for support.
- Add a retention job for closed tickets.
- Add persistence tests for the primary-contact constraint.
