# Runbook: payment decline spike

## Symptoms

- `payment.declined` rises above 10% for five minutes.
- Order conversion drops at the payment step.
- Support tickets mention "card declined".

## First checks

1. Split the rate by provider. One provider or all of them?
2. Split by card brand and issuer country. A single issuer points upstream.
3. Check provider status and incident history.
4. Check recent deploys to payment or order.
5. Check for a configuration change to amounts, currencies or 3-D Secure.

## Interpretation

| Pattern | Likely cause |
| --- | --- |
| One provider, all cards | Provider outage |
| One issuer | Issuer policy or outage |
| All providers, one currency | Pricing or currency configuration |
| Spike after deploy | Regression in the request payload |
| High decline only for new customers | Risk rule change |

## Mitigation

- If one provider is down, route new attempts to a healthy provider if the
  routing flag supports it.
- If a deploy correlates, roll back.
- If a risk rule is too strict, lower it deliberately and record the decision.
- Never retry a hard decline automatically; it annoys the customer and does
  not improve the outcome.

## Verification

- Decline rate returns below the baseline.
- Successful payment rate and order conversion recover.
- No duplicate captures were created by retries.

## Follow-up

- Preserve a sample of provider response codes.
- Confirm that no card data reached the logs.
- Add a dashboards split by provider if one was missing.
