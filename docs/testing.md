# Testing guide

## Test pyramid

| Layer | Tool | Scope | Speed |
| --- | --- | --- | --- |
| Unit | JUnit 5, Mockito | One service class | Milliseconds |
| Web | MockMvc standalone | Controller + validation + errors | Milliseconds |
| Persistence | Spring Data test slice | Queries and mappings | Seconds |
| Integration | Spring Boot test | One service end to end | Seconds |
| Contract | HTTP stubs | Gateway behaviour | Seconds |

Most tests should be unit tests. A test that starts a full Spring context is
justified only when the thing under test is the wiring itself.

## Unit tests

A service test is constructed by hand:

```java
private final ProductRepository repository = mock(ProductRepository.class);
private final ProductService service = new ProductService(repository);
```

Rules:

- Mock the repository, never the class under test.
- One behaviour per test.
- The test name states the rule: `refusesADuplicateCode`.
- Assert the outcome, not the implementation.
- Verify that a rejected write never reaches the repository.
- Use realistic values; `BigDecimal.TEN` is fine, a random UUID is not.

A rule test must fail if the rule is removed. If deleting the `if` statement
leaves the test green, the test is not covering the rule.

## Web tests

Controllers are tested with standalone MockMvc:

```java
MockMvc mvc = MockMvcBuilders.standaloneSetup(new ProductController(service))
        .setControllerAdvice(new ApiExceptionHandler())
        .build();
```

Standalone setup is deliberate. It exercises request mapping, validation and
error translation without starting the application or a database.

Cover:

- the success status code
- at least one field in the response body
- the archive status (`204`)
- a not-found mapping (`404`)
- validation failure (`400`) where a body is accepted

## Persistence tests

Use a test slice and an in-memory database. Persistence tests should cover:

- the unique natural key
- each derived query used by a controller
- optimistic locking on a concurrent update
- the lifecycle timestamps

Do not test Hibernate itself. Test the queries this service relies on.

## Integration tests

An integration test may start the service when the behaviour depends on
wiring, serialisation or transactions. Keep the number small. Use a fixed
clock or a tolerance when asserting timestamps.

## Test data

- Build data in the test; do not share mutable fixtures.
- Use the public constructors and methods, not reflection.
- Keep helper methods at the top of the class.
- Never use real personal data or credentials.
- Avoid `Thread.sleep`; assert on state instead.

## Naming

```
createsWhenTheCodeIsNew
refusesADuplicateCode
reportsAMissingItem
refusesToUpdateAnArchivedItem
activatesAnArchivedItem
```

A failing test name should tell the reader what rule broke.

## Running

```bash
mvn test                                  # everything
mvn -pl catalog-service -am test          # one service
mvn -pl catalog-service -Dtest='*ServiceTest' test
mvn -pl common-lib test                   # shared kernel
```

## Coverage

Coverage is a guide, not a target. The important number is the fraction of
business rules with a test that fails when the rule is removed. Generated
getters and framework code do not need dedicated tests.

## Flaky tests

A flaky test is a defect. Do not retry it into silence. Common causes:

- dependence on wall-clock time
- shared mutable state in a static field
- assumptions about ordering without a sort
- a port that is already in use
- an asynchronous assertion without a deterministic wait

Fix the cause or delete the test with a reason.

## What not to test

- framework annotations
- plain getters and setters
- generated code
- the exact text of a log line
- private methods through reflection
