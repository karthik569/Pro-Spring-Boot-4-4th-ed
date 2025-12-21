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
    - Verified all tests pass via `./gradlew test`.

### General
- Adhered to Java 21 and Spring Boot 4 standards.
- Ensured both projects are fully functional and compile successfully.
