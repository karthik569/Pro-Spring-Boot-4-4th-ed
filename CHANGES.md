# Changes / Memory

## Chapter 5: JDBC Client with Spring Boot

### Customer Project (Maven)
- **Persistence**: Refactored `CustomerRepository` to use the new `JdbcClient` (Spring 6.1+).
- **Compatibility**: Implemented a database-agnostic "Update then Insert" pattern in the `save` method to support both H2 (for tests/dev) and PostgreSQL (for production), avoiding dialect-specific syntax like `ON CONFLICT`.
- **Mapping**: Added a custom `RowMapper` implementation to demonstrate manual result mapping alongside automatic Record mapping.
- **Validation**:
    - Added `spring-boot-starter-validation` dependency.
    - Annotated `Customer` record with `@NotBlank` and `@Email`.
    - Enabled validation in `CustomerController` using `@Valid`.
- **Web**: Created `CustomerControllerAdvice` to handle `MethodArgumentNotValidException` and return standard RFC 7807 `ProblemDetail` responses.
- **Configuration**:
    - Implemented Spring Profiles:
        - `application.properties`: Sets `dev` as default.
        - `application-dev.properties`: Configured for H2 in-memory with console enabled.
        - `application-prod.properties`: Configured for PostgreSQL.
    - Added `schema.sql` for automatic table creation.
- **Infrastructure**: Added `docker-compose.yml` using `postgres:alpine`.
- **Testing**:
    - Added `CustomerRepositoryTest` using Mockito to mock the `JdbcClient` fluent API.
    - Verified all tests pass via `./mvnw test`.

### Management Project (Gradle)
- **Persistence**: Refactored all repositories (`Customer`, `Address`, `Company`, `Communication`) to use `JdbcClient`.
- **Batch Operations**: Added `saveAll` in `CustomerRepository` using `JdbcTemplate` to fulfill the "Comparison with JdbcTemplate" learning objective and demonstrate batch updates.
- **Typo Fixes & Refactoring**:
    - Renamed `Adress` to `Address` (including filename).
    - Fixed `firtName` to `firstName` in `Customer` record.
    - Renamed `customer` field to `customerId` in `Communication` record to align with database column names and fix mapping errors.
- **Compatibility**: Refactored all `save` methods to use the "Update then Insert" pattern for H2 compatibility during tests.
- **Validation**:
    - Added `spring-boot-starter-validation` to `build.gradle`.
    - Added validation annotations to all model records.
- **Service Layer**: Added `@Transactional` to `ManagementService` methods to ensure data integrity during multi-repository operations.
- **Configuration**:
    - Consolidated all settings into a multi-profile `application.yml`.
    - Configured `dev` (H2) and `prod` (PostgreSQL) profiles.
    - Added `schema.sql` covering all 4 tables with foreign key constraints.
- **Infrastructure**: Added `docker-compose.yml` using `postgres:alpine` (standardizing on PostgreSQL as requested).
- **Testing**:
    - Added `CustomerRepositoryTest` using Mockito.
    - Added and fixed `ManagementCustomerRepositoryTest` to correctly mock `JdbcClient.MappedQuerySpec.optional()`.
    - Fixed `ManagementApplicationTests` by resolving SQL grammar issues and mapping mismatches.

## Chapter 6: Spring Data with Spring Boot

### Customer Project (Maven) - Spring Data JDBC
- **Dependencies**: Replaced `spring-boot-starter-jdbc` with `spring-boot-starter-data-jdbc`.
- **Model**: 
    - Reverted `Customer` to a `record`.
    - Implemented `Persistable<UUID>` to explicitly handle the entity's "new" state (`id == null`).
    - Added `@Table("customer")` and `@Id` annotations.
- **Persistence**: 
    - Updated `CustomerRepository` to extend `ListCrudRepository<Customer, UUID>`.
    - Restored `schema.sql` (required for JDBC) with `UUID DEFAULT random_uuid()` for H2 compatibility.
- **Configuration**: Updated `application-dev.properties` to enable SQL initialization (`spring.sql.init.mode=always`).
- **Testing**: 
    - Implemented `CustomerRepositoryTest` using `@SpringBootTest` and `@Transactional`.
    - Isolated repository tests using a unique DB name (`jdbc:h2:mem:customer_repo_test`) to avoid conflicts with `CustomerConfiguration`.
    - Updated `CustomerApplicationTests` to use Record accessors and verify creation logic.
    - Standardized `pom.xml` test dependencies.
    - Verified all tests pass via `./mvnw test`.

### Management Project (Gradle) - Spring Data JPA
    - Added `findByCustomerCustomerId(UUID)` method to `AddressRepository` and `CommunicationRepository`.
    - Added derived query `findByLastName(String)` and `@Query` (JPQL) `findByEmailDomain(String)` to `CustomerRepository`.
    - Added `findByEmailWithCompany(String)` using `JOIN FETCH` to demonstrate resolving N+1 issues in `CustomerRepository`.
    - Added `findByCompanyCompanyId(UUID, Pageable)` to `CustomerRepository` to demonstrate pagination and sorting.
    - Added `findByCityWithCustomer(String)` using `JOIN FETCH` to `AddressRepository`.
    - Removed custom `Repository` interface and `JdbcClient` implementations.
- **Service Layer**:
    - Refactored `ManagementService` to work with Entities and Relationships (setting object references instead of IDs).
    - Updated `getCustomerDetails` to traverse the object graph (`customer.getCompany()`).
    - Added `getCustomersByCompany(UUID, int, int)` to `ManagementService` with built-in `PageRequest` and `Sort` by `lastName`.
- **Configuration**:
    - Updated `application.yml` to set `jpa.hibernate.ddl-auto` (`create-drop` for dev, `update` for prod) and removed `schema.sql`.
- **Testing**:
    - Updated `ManagementApplicationTests` to use Entity getters and added integration test for `getCustomersByCompany`.
    - Implemented `ManagementCustomerRepositoryTest` using `@SpringBootTest` and `@Transactional` to verify JPA repository logic, including fetch joins and pagination.
    - Created `ManagementAddressRepositoryTest` to verify fetch join logic.

## Chapter 7: NoSQL with Spring Boot

### Customer Project (Maven) - MongoDB
- **Dependencies**: Replaced JPA/SQL dependencies with `spring-boot-starter-data-mongodb`. Added `spring-boot-testcontainers` and `testcontainers-mongodb` for testing.
- **Model**:
    - Annotated `Customer` record with `@Document("customers")`.
    - Refactored ID type to `String` for automatic MongoDB ID generation.
    - Added `Vector vector` field to the `Customer` record to support Vector Search (AI frontier).
- **Persistence**:
    - Updated `CustomerRepository` to extend `MongoRepository<Customer, String>`.
    - Implemented `findByVectorNear` with `@VectorSearch` and `Limit` parameter.
- **Configuration**:
    - Updated properties to use latest Spring Boot 4 `spring.mongodb.uri` (removing `data` prefix).
- **Infrastructure**: Updated `docker-compose.yml` to use `mongo:7` with authentication.
- **Testing**:
    - Implemented `CustomerRepositoryTest` using `@DataMongoTest` and Testcontainers.
    - Verified all tests pass via `./mvnw clean test`.

### Management Project (Gradle) - Polyglot Persistence (JPA + Redis + Neo4j)
- **Persistence Strategy**:
    - **PostgreSQL (JPA)**: Core CRM data (`Customer`, `Company`, `Address`, `Communication`).
    - **Redis**: Key-Value session storage (`CustomerSession`) using `ReactiveRedisOperations`.
    - **Neo4j**: Graph relationships (`CustomerNode`) using blocking repositories bridged via `Schedulers.boundedElastic()`.
- **Dependencies**: Integrated `spring-boot-starter-data-jpa`, `spring-boot-starter-data-redis-reactive`, and `spring-boot-starter-data-neo4j`.
- **Web Layer (Listing 7-8)**:
    - Implemented reactive `ManagementHandlers` using functional programming patterns.
    - Configured `RouterFunction` in `ManagementConfiguration` to expose `GET` and `POST` endpoints.
- **Service Layer (Listing 7-7)**:
    - Added `getCustomerWithSession(UUID customerId)`: Coordinates PostgreSQL data with Redis session status.
    - Added `addReferral(UUID customerId, UUID referrerId)`: Manages graph relationships in Neo4j.
- **Configuration**:
    - Explicitly defined `JpaTransactionManager` (Primary) and `Neo4jTransactionManager` to resolve polyglot transactional conflicts.
    - Updated `application.yml` with separate sections for all three data stores.
    - Replaced `ApplicationReadyEvent` with `CommandLineRunner` for reliable data initialization.
- **Infrastructure**: Updated `docker-compose.yml` to include `postgres:alpine`, `redis:7-alpine`, and `neo4j:5.26.0`.
- **Testing**:
    - Updated `ManagementApplicationTests` to use Testcontainers for all three databases simultaneously.
    - Verified all tests pass via `./gradlew clean test`.

## Chapter 9: Going Reactive with Spring Boot

### Customer Project (Maven) - Spring WebFlux & R2DBC
- **Dependencies**:
    - Replaced Web MVC with `spring-boot-starter-webflux`.
    - Added `spring-boot-starter-data-r2dbc`, `r2dbc-postgresql` (for CockroachDB wire compatibility), and `r2dbc-pool`.
    - Integrated `spring-boot-starter-validation`.
    - Added Testcontainers support: `spring-boot-testcontainers`, `cockroachdb`, and `junit-jupiter`.
- **Model**:
    - Annotated `Customer` record with `@Table("customers")` and `@Id`.
    - Implemented `Persistable<UUID>` with `@Transient boolean isNew` to explicitly control R2DBC insert vs. update logic.
    - Added validation annotations: `@NotBlank` and `@Email`.
- **Persistence**:
    - Updated `CustomerRepository` to extend `R2dbcRepository<Customer, UUID>`.
    - Added derived query method `findByLastName(String)`.
- **Web Layer**:
    - Refactored `CustomerController` to return reactive types (`Flux`, `Mono`).
    - Implemented retry logic using `retryWhen` for `R2dbcException` with SQL State `40001` (CockroachDB serialization failure).
    - Enabled request body validation with `@Valid`.
    - Created `CustomerControllerAdvice` to handle `WebExchangeBindException` and return RFC 7807 `ProblemDetail` responses.
- **Configuration**:
    - Updated `CustomerConfiguration` to seed initial data reactively using `deleteAll()` and `save()`.
    - Added `schema.sql` for automatic table creation in CockroachDB.
- **Testing**:
    - Updated `CustomerApplicationTests` to use `WebTestClient`.
    - Integrated Testcontainers with `CockroachContainer` (v25.4.0).
    - Implemented `shouldHandleConcurrentWritesWithVirtualThreads` using Java 21 `Executors.newVirtualThreadPerTaskExecutor()` to simulate high concurrency and verify non-blocking behavior.
    - Configured manual R2DBC properties via `DynamicPropertySource` to ensure reliable container connectivity.
    - Verified all tests pass via `./mvnw test`.

### Management Project (Gradle) - Functional Web & Reactive Caching
- **Dependencies**:
    - Integrated `spring-boot-starter-webflux` and `spring-boot-starter-data-r2dbc`.
    - Added `io.r2dbc:r2dbc-pool` and `org.postgresql:r2dbc-postgresql`.
    - Integrated `spring-boot-starter-validation`.
    - Added Testcontainers support with `cockroachdb` and `junit-jupiter`.
- **Model**:
    - Refactored `Customer`, `Company`, `Address`, and `Communication` records to use `@Table` and `@Id` for R2DBC mapping.
    - Added validation annotations (`@NotBlank`, `@Email`) to all domain entities.
- **Persistence**:
    - Converted all repositories to interfaces extending `R2dbcRepository`.
    - Added reactive query methods like `findAllByCustomerId(UUID)`.
- **Service Layer**:
    - Refactored `ManagementService` to use Project Reactor (`Mono`, `Flux`).
    - Implemented a **Reactive Cache-Aside pattern** using `ConcurrentHashMap` to optimize read performance.
    - Enhanced `createCustomerWithDetails` with `@Transactional` to atomically manage the creation of the full object graph.
- **Web Layer (Functional)**:
    - Implemented reactive handlers in `ManagementHandlers`.
    - Configured `RouterFunction` in `ManagementConfiguration` to expose `GET` and `POST` endpoints.
    - Implemented `GlobalErrorWebExceptionHandler` extending `AbstractErrorWebExceptionHandler` to provide structured JSON error responses for functional endpoints.
- **Configuration**:
    - Updated `ManagementConfiguration` to seed initial data reactively.
    - Added `schema.sql` defining the full schema for CockroachDB.
- **Testing**:
    - Updated `ManagementApplicationTests` to use `WebTestClient` and Testcontainers.
    - Implemented `shouldServeConcurrentReadsFromCache` using **Java 21 Virtual Threads** to simulate 100 concurrent users reading the same resource and verify cache efficiency.
    - Verified all tests pass via `./gradlew test`.

## Chapter 11: Security with Spring Boot

### Customer Project (Maven) - Database Auth, 2FA & mTLS
- **Security Configuration**:
    - Implemented a custom `UserDetailsService` (Listing 11-9) that loads users directly from `CustomerRepository`.
    - Enabled `httpBasic` and `formLogin` support.
    - Added OTT (One-Time Token) and MFA support using Spring Security 7 patterns (protected by `ott` and `mfa` profiles).
    - **mTLS Support**: 
        - Fixed `SecurityConfig` by replacing WebFlux `SecurityWebFilterChain` with Servlet `SecurityFilterChain` for the `mtls` profile.
        - Configured `x509()` authentication for the `mtls` profile.
        - Ensured default security is only active when the `mtls` profile is *not* present (`@Profile("!mtls")`).
- **Entity & Repository Updates**:
    - Added `password` field to `Customer` entity.
    - Implemented `findByEmail(String)` in `CustomerRepository` to support authentication.
- **Client-side Security (mTLS)**:
    - Implemented `ClientConfig` using Spring Boot `SslBundles` to configure a `RestClient` for mutual TLS.
    - Created `ManagementClient` (HTTP Interface) and `ManagementOrchestrator` to demonstrate secure inter-service communication.
- **New Features**:
    - **OTT (Magic Links)**: Created `CrmOttSuccessHandler` to handle magic link generation and delivery.
    - **MFA (TOTP)**: Integrated `googleauth` library and created `MfaService` for TOTP verification.
- **Configuration**:
    - Updated `CustomerConfiguration` to use `PasswordEncoder` for encoding initial test data.
- **Testing Refactor**:
    - **Security Isolation**: Refactored `BaseTest` to manually apply `springSecurity()` to `MockMvc` and bind `RestTestClient` to it.
    - **CSRF Handling**: Added default CSRF tokens to `MockMvc` requests.
    - **Updated Coverage**: Refactored all existing tests to provide correct security context (`ROLE_ADMIN`, `SCOPE_read`) and updated `Customer` constructor calls.
    - Verified all 42 tests pass via `./mvnw test`.

### Management Project (Gradle) - Reactive Security
- **Dependencies**: Added `spring-boot-starter-security` and `spring-security-test`.
- **Security Configuration**:
    - Implemented `SecurityConfig` in a new `security` package using `ServerHttpSecurity`.
    - Configured `MapReactiveUserDetailsService` with `admin` and `user` accounts.
    - Enabled Reactive Method Security (`@EnableReactiveMethodSecurity`).
- **Authorization**:
    - Created custom `@IsAdmin` security annotation.
    - Applied method-level security to `ManagementService` write operations.
- **Testing**:
    - Created `ManagementSecurityTests` using `WebTestClient` with `mutateWith(mockUser())`.
    - Updated `ManagementApplicationTests` and `ServiceSpyTests` to handle authentication.
- **Verified**: Project builds successfully via `./gradlew classes testClasses`.

### Key Learning Points
- **Custom UserDetailsService**: Transitioned from property-based auth to database-backed authentication.
- **Spring Security 7+ OTT/MFA/mTLS**: Explored new authentication patterns and certificate-based security.
- **SslBundles**: Leveraged the new Spring Boot SSL abstraction for client-side certificates.
- **Reactive vs Servlet Security**: Applied security patterns to both stack types using the latest Spring 7 idioms.
