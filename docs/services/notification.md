# Notification service

`notification-service` owns outbound communication: messages, templates,
campaigns, digests, channels, delivery reports, webhooks and suppression.

## Package

```
com.orbit.notification
```

## Owned concepts

| Concept | Table | Notes |
| --- | --- | --- |
| Message | `message` | One outbound message |
| Template | `template` | Reusable content |
| Campaign | `campaign` | Batch of messages |
| Subscriber | `subscriber` | Opted-in contact |
| NotificationPreference | `notification_preference` | Per-customer choice |
| Channel | `channel` | Transport configuration |
| Digest | `digest` | Batched summary |
| DeliveryReport | `delivery_report` | Provider result |
| SendAttempt | `send_attempt` | One provider call |
| BouncedEmail | `bounced_email` | Hard or soft bounce |
| PushSubscription | `push_subscription` | Device registration |
| EventLog | `event_log` | Delivery history |
| Newsletter | `newsletter` | Scheduled newsletter |
| Announcement | `announcement` | Broadcast notice |
| RecipientList | `recipient_list` | Named audience |
| Suppression | `suppression` | Do-not-contact address |
| Webhook | `webhook` | Outbound callback |

## API

```
POST   /api/messages
GET    /api/messages/{id}
GET    /api/messages/by-code/{code}
GET    /api/messages?text=...
GET    /api/messages/stats/active
PUT    /api/messages/{id}
DELETE /api/messages/{id}
```

Templates, campaigns, subscribers, preferences, channels, digests, reports,
attempts, bounces, push subscriptions, event logs, newsletters, announcements,
recipient lists, suppressions and webhooks follow the same shape.

## Send flow

1. A domain event or a campaign produces a message.
2. The service resolves the recipient's preference and consent.
3. A suppression check runs before rendering.
4. A template is rendered with the message's variables.
5. A send attempt is recorded, then the provider is called.
6. The provider result becomes a delivery report.
7. A hard bounce adds a suppression.

## Interactions

| Caller | Interaction |
| --- | --- |
| Order | Confirmation, shipment, cancellation |
| Payment | Receipt, decline, refund |
| Customer | Preference and consent |
| Shipping | Tracking updates |
| Campaign tools | Create campaign, freeze audience |

Notification is downstream. It never creates an order or changes a payment.

## Rules

- A message has one channel and one recipient.
- Consent and suppression are checked before every send.
- An unsubscribe is monotonic.
- A hard bounce adds a suppression.
- An attempt number only increases.
- A campaign's audience is frozen when it starts.
- A published announcement is not unpublished.
- A webhook secret is never stored in clear.
- A digest has an explicit window.

## Failure modes

| Failure | Response |
| --- | --- |
| Missing template | `404 NOT_FOUND` |
| Suppressed address | message skipped, recorded |
| Provider timeout | retry with backoff, new attempt |
| Duplicate campaign | `409 DUPLICATE_CODE` |
| Invalid recipient | `400` validation |

## Testing

`MessageServiceTest` covers message creation. `CampaignServiceTest` covers the
audience freeze. `SubscriberServiceTest` covers unsubscribe. The generated
domains add coverage for preferences, channels, bounces, push, events,
newsletters, announcements, lists, attempts and suppression.

## Operations

- Backlog is the key metric; a growing backlog is an incident.
- Provider errors are retried with a bounded budget, then parked.
- Suppression lists are checked before retries too.
- A campaign send is throttled to protect the provider.

## Runbook

See [notification backlog](../runbooks/notification-backlog.md).

## Open work

- Add a dead-letter queue for permanently failed sends.
- Add persistence tests for the suppression lookup.
- Add a bounded event-log query for support.
