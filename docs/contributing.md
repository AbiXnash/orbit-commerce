# Contributing

## Before you start

- Read [development.md](development.md) and [testing.md](testing.md).
- Check the ADRs for decisions that affect your change.
- For a public behaviour change, open an issue describing the contract.

## Workflow

1. Branch from `main` with a descriptive name.
2. Keep the change focused; split refactors from behaviour changes.
3. Write the test that fails without the change.
4. Implement the smallest change that makes it pass.
5. Run the module tests and the shared library tests.
6. Update the docs and changelog if behaviour changed.
7. Open a pull request with the problem, the approach and the risk.

## Branch names

```
feature/<area>-<short-description>
fix/<area>-<short-description>
docs/<short-description>
chore/<short-description>
```

## Commit messages

```
feat(catalog): add product variant management
fix(inventory): report missing replenishments as not found
test(common): add test support to the shared library
docs: describe the checkout flow
```

The subject is imperative and under 72 characters. The body explains *why* and
calls out migrations or rollout notes.

## Pull request checklist

- [ ] Tests cover the new rule and the failure path
- [ ] No entity is exposed from a controller
- [ ] No cross-service database access
- [ ] Error codes are stable and mapped
- [ ] Money uses `BigDecimal`, time uses `Instant`
- [ ] Logs contain no PII or secrets
- [ ] Docs and changelog updated
- [ ] Rollback is possible without a data restore

## Review etiquette

- Review the tests first.
- Ask for the reason behind a change, not just the change.
- Prefer a suggestion with an example over a vague objection.
- Approve when the risk is understood, not when the code is perfect.

## Definition of done

- The build is green.
- The behaviour is documented.
- The failure path is tested.
- An operator can observe the new behaviour.
- A rollback has been considered.
