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
    - Added `findByEmailWithCompany(String)` (JOIN FETCH) and `findByCompanyCompanyId(UUID, Pageable)` (Pagination) to `CustomerRepository`.
    - Added `findByCityWithCustomer(String)` (JOIN FETCH) to `AddressRepository`.
    - Removed custom `Repository` interface and `JdbcClient` implementations.
- **Service Layer**:
    - Refactored `ManagementService` to work with Entities and Relationships (setting object references instead of IDs).
    - Updated `getCustomerDetails` to traverse the object graph (`customer.getCompany()`).
- **Configuration**:
    - Updated `application.yml` to set `jpa.hibernate.ddl-auto` (`create-drop` for dev, `update` for prod) and removed `schema.sql`.
- **Testing**:
    - Updated `ManagementApplicationTests` to use Entity getters.
    - Implemented `ManagementCustomerRepositoryTest` using `@SpringBootTest` and `@Transactional` to verify JPA repository logic, including fetch joins and pagination.
    - Created `ManagementAddressRepositoryTest` to verify fetch joins.

### Key Learning Points
- **Spring Data JDBC** requires explicit `schema.sql` and manual handling of ID generation/state (e.g., `Persistable`). Supports derived queries and native SQL in `@Query`.
- **Spring Data JPA** provides automatic DDL generation and sophisticated object graph mapping. Supports JPQL and native SQL in `@Query`.
    - Verified all tests pass via `./gradlew test`.

### General
- Adhered to Java 21 and Spring Boot 4 standards.
- Ensured both projects are fully functional and compile successfully.
