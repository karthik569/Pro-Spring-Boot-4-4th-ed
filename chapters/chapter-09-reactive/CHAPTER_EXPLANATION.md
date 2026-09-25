# Chapter 9 — Reactive Streams with R2DBC & WebFlux

The chapter demonstrates a **fully non-blocking, asynchronous reactive stack** using Spring Data R2DBC and Spring WebFlux, running on Project Reactor over Netty. Two parallel Maven/Gradle projects live under `chapters/chapter-09-reactive/`:

- `customer/` — a simple reactive CRUD service (Maven)
- `management/` — a richer reactive service with composed entities, functional routing, caching, and transactions (Gradle)

Both target Spring Boot 4.0.6, Java 21, and PostgreSQL via the R2DBC driver, with **CockroachDB Testcontainers** used in tests.

## Core Reactive Stack

Both projects share the same dependency set:

- `spring-boot-starter-webflux` — non-blocking HTTP on Netty
- `spring-boot-starter-data-r2dbc` — reactive repositories (`R2dbcRepository`)
- `spring-boot-starter-validation` — bean validation, raised as `WebExchangeBindException`
- `r2dbc-postgresql` + `r2dbc-pool` — async driver with a connection pool
- `reactor-test`, `spring-boot-testcontainers`, `testcontainers-cockroachdb` — verify behavior

The request flow is end-to-end reactive:

```
HTTP → Netty EventLoop → Flux/Mono pipeline → R2DBC driver → PostgreSQL/CockroachDB
```

No thread ever blocks waiting on I/O; demand propagates downstream as backpressure.

## Project 1 — `customer`

A textbook **annotation-based reactive controller** using `@RestController` plus a single-entity repository.

**Model** (`Customer.java`) — a Java 21 `record` that implements `Persistable<UUID>` so Spring Data can tell new from existing rows. A `@Transient boolean isNew` plus `@PersistenceCreator` discriminates between "insert" and "update" during `save()`.

**Repository** (`CustomerRepository.java`) — extends `R2dbcRepository<Customer, UUID>`, automatically returning `Mono<Customer>` for single results and `Flux<Customer>` for streams. A derived finder `findByLastName(String)` returns a `Flux`.

**Controller** (`CustomerController.java`) — exposes `/api/v1/customers` with `Flux<Customer> findAll()`, `Mono<ResponseEntity<Customer>> findById(...)`, `Mono<Customer> save(...)` (201 Created), and `Mono<Void> deleteById(...)`. The `save` operation demonstrates **transient error recovery**:

```java
return repository.save(customer)
    .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
        .filter(throwable -> throwable instanceof R2dbcException &&
            "40001".equals(((R2dbcException) throwable).getSqlState())));
```

SQL state `40001` is PostgreSQL's serialization failure, so the controller retries up to 3 times with exponential backoff — the canonical pattern for optimistic-concurrency retries on a reactive stream.

**Seeding** (`CustomerConfiguration.java`) — registers an `ApplicationListener<ApplicationReadyEvent>` that `deleteAll().thenMany(Flux.just(...)).flatMap(repository::save)` to seed sample data after startup.

**Error mapping** (`CustomerControllerAdvice.java`) — a `@RestControllerAdvice` that turns `WebExchangeBindException` into a Spring 6/7 `ProblemDetail` with a list of field errors.

**Test** (`CustomerApplicationTests.java`) — uses a `CockroachContainer` (CockroachDB speaks the PostgreSQL R2DBC protocol), wires its URL into `spring.r2dbc.*` via `@DynamicPropertySource`, then fires 10 concurrent inserts from a **virtual-thread-per-task executor** to verify non-blocking behavior.

## Project 2 — `management`

A more advanced variant that introduces **functional routing, multi-entity composition, transactions, and caching**.

**Domain model** — four records joined relationally:

- `Company(companyId, companyName, industry, website)`
- `Customer(customerId, firstName, lastName, jobTitle, email, phone, companyId)` (FK → companies)
- `Address(addressId, customerId, street, city, state, zip)` (FK → customers)
- `Communication(communicationId, customerId, communicationType, communicationValue)` (FK → customers)
- `CustomerDetailsDTO` — aggregates Customer + Company + List<Address> + List<Communication>

**Repositories** — each table gets its own `R2dbcRepository`. `AddressRepository` and `CommunicationRepository` add derived `findAllByCustomerId(UUID)` returning a `Flux`.

**Functional handler** (`ManagementHandlers.java`) — instead of `@RestController`, exposes a `ServerRequest → Mono<ServerResponse>` pair. It uses:

- `request.bodyToMono(Customer.class).flatMap(service::saveCustomer)` for POST
- `switchIfEmpty(ServerResponse.notFound().build())` to translate empty `Mono` into HTTP 404

**Router** (`ManagementConfiguration.java#managementRoutes`) — wires the handlers via the functional DSL:

```java
return route(GET("/api/v1/customers/{id}"), handlers::getCustomerDetails)
       .andRoute(POST("/api/v1/customers"), handlers::createCustomer);
```

**Service** (`ManagementService.java`) — orchestrates the reactive graph. Highlights:

- **`@Transactional` createCustomerWithDetails** — flatMaps `companyRepository.save` → `customerRepository.save` → `Mono.zip(addressRepository.save, communicationRepository.save)` to assemble the DTO, with the same SQL state `40001` retry strategy as the customer project.
- **In-memory cache** — a `ConcurrentHashMap<UUID, CustomerDetailsDTO>` populated on both write and first read, so subsequent `getCustomerDetails` calls short-circuit via `Mono.justOrEmpty(cache.get(id)).switchIfEmpty(...)`.

**Global error handling** (`GlobalErrorWebExceptionHandler.java`) — extends `AbstractErrorWebExceptionHandler` and registers at `@Order(-2)`. It returns a JSON error map with HTTP 400 + a `details` array for `WebExchangeBindException`, otherwise HTTP 500. This is the WebFlux-native counterpart to the customer project's `@RestControllerAdvice`.

**Test** (`ManagementApplicationTests.java`) — same CockroachDB Testcontainers setup, then POSTs one customer and fires **100 concurrent reads** for that same id through a virtual-thread executor. Because `getCustomerDetails` populates the cache on first hit, all 100 reads share a single DB round-trip after the first — the test demonstrates how the reactive + cache combo absorbs high read concurrency.

## Key Concepts the Chapter Teaches

- **R2DBC vs JDBC** — non-blocking database access; methods return `Mono`/`Flux` instead of `Optional`/`List`.
- **WebFlux functional vs annotation model** — both work; functional gives finer control and is used in the management app.
- **Backpressure & Mono/Flux composition** — operators like `flatMap`, `zip`, `switchIfEmpty`, `thenMany`, `Mono.justOrEmpty` are the building blocks of reactive business logic.
- **Reactive transactions** — `@Transactional` works on the reactive stack; the service methods are transactional Mono-returning functions.
- **Error semantics in the reactive world** — `Retry.backoff` filtered on SQL state for retryable failures, plus a global `ErrorWebExceptionHandler` for validation and unexpected errors.
- **Virtual-thread concurrency tests** — Java 21 `Executors.newVirtualThreadPerTaskExecutor()` is used to simulate many concurrent users and prove the non-blocking stack absorbs them cheaply.

## Build & Verify

```bash
cd chapters/chapter-09-reactive/customer   && mvn test
cd chapters/chapter-09-reactive/management && ./gradlew test
```

Each test boots a CockroachDB container, wires its R2DBC URL dynamically, and exercises the HTTP API with `WebTestClient`.
