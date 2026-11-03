# Development guide

## Prerequisites

- Java 21
- Maven 3.9 or newer
- No database server: services use in-memory H2 by default

## First build

```bash
mvn test
```

The first run downloads dependencies. Later runs use the local Maven cache.
Build a single service with its dependencies:

```bash
mvn -pl order-service -am test
```

Run a service:

```bash
mvn -pl catalog-service -am spring-boot:run
```

The service starts on the port configured in its `application.yml`.

## Adding a domain

A domain is one package under `com.orbit.<service>`. Follow the existing shape
exactly; consistency is worth more than cleverness.

1. Create the entity with an identity, a unique `code`, a `name`, a `status`,
   a non-negative `priority`, an `amount`, `@Version`, and timestamps.
2. Add `@PrePersist` and `@PreUpdate` lifecycle methods.
3. Add a repository with derived queries for code, status and priority.
4. Add a request record with validation on `code`, `name` and `priority`.
5. Add a response record with a package-private `from(entity)` factory.
6. Add a service with `create`, `get`, `getByCode`, `search`, `top`, `update`,
   lifecycle actions and statistics.
7. Add a controller that only translates HTTP to service calls.
8. Add a unit test for the rules and a web test for the transport.

The generated domains in this repository are the reference implementation.

## Entity rules

- Protected no-argument constructor for JPA.
- Public constructor taking the natural key and name.
- No public setter for `id`, `code` or `status`.
- Status changes go through intention-revealing methods (`archive`,
  `activate`, `deactivate`), never `setStatus`.
- Money is `BigDecimal`, never `double`.
- Timestamps are `Instant`, never `LocalDateTime`.
- `@Version` is mandatory on mutable entities.
- `equals` and `hashCode` use the natural key `code`.

## Service rules

- One public method per use case.
- `@Transactional` on writes; `@Transactional(readOnly = true)` on reads.
- Business failures throw `ConflictException` or `NotFoundException` with an
  `ErrorCode`.
- Log state changes once, at info level, with the natural key.
- Never log PII, tokens, full card numbers or request bodies.
- A service method either completes or throws; no partial writes.
- Do not call a repository in a loop when a single query can do the work.

## Controller rules

- Controllers contain no business logic.
- Validate the body with `@Valid`.
- Return `201` for create and `204` for archive.
- Use `@ResponseStatus` rather than returning `ResponseEntity` for simple cases.
- Never expose an entity.

## Shared library rules

`common-lib` holds only cross-cutting, domain-free code:

- error types and the exception handler
- limits and retry policy
- value types such as `Money` and `Percentage`
- text, check, time and identifier helpers

If a type mentions a business concept (order, product, payment), it does not
belong in the shared library.

## Code style

- Four-space indentation, 120-character soft limit.
- Imports are explicit; no wildcard imports.
- Fields are `private final` unless JPA requires otherwise.
- Prefer records for DTOs and small value types.
- Prefer `List.of` and `Map.of` for constants.
- Comments explain *why*; the code explains *what*.
- Every public type has a one-line Javadoc.

## Commit hygiene

- One logical change per commit.
- Subject in the imperative mood: `add`, `fix`, `remove`, not `added`.
- Prefix with the area: `feat(catalog):`, `fix(inventory):`, `docs:`,
  `test(common):`.
- Tests travel with the change they cover.
- Do not mix formatting-only commits with behaviour changes.

## Pull request checklist

- [ ] The module builds: `mvn -pl <service> -am test`
- [ ] New rules have a unit test that fails without the change
- [ ] New endpoints have a web test
- [ ] No entity is returned from a controller
- [ ] No new cross-service database access
- [ ] Docs updated for any public behaviour change
- [ ] Changelog entry added for user-visible changes

## Common local problems

| Symptom | Cause | Fix |
| --- | --- | --- |
| Port already in use | Another service running | Stop it or change the port |
| H2 data disappears | In-memory database | Expected; use a file URL for a persistent local run |
| `404` from a Feign client | Route not exposed | Check the target controller mapping |
| Test passes alone, fails in suite | Shared static state | Remove the static cache |
| Slow suite | Per-test Spring context | Prefer a unit test or standalone MockMvc |
