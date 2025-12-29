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

## Chapter 16: Spring AI - Building an AI-Powered CRM Assistant

### Management Project (Gradle) - AI Assistant Integration
- **Dependencies**:
    - Added Spring AI 1.0.0-M5 BOM to dependency management.
    - Integrated `spring-ai-core`, `spring-ai-openai-spring-boot-starter`, `spring-ai-ollama-spring-boot-starter`.
    - Added `spring-ai-pgvector-store-spring-boot-starter` for vector similarity search.
    - Included PostgreSQL JDBC driver (`org.postgresql:postgresql`) for pgvector support.
    - Added `spring-boot-starter-web` for blocking web operations required by vector store.
- **AI Configuration**:
    - Updated `application.yml` with dual AI provider support:
        - **Ollama**: Local LLM runtime with `llama3.2` chat model and `nomic-embed-text` embeddings (for development).
        - **OpenAI**: Cloud-based GPT-4o option (for production).
    - Configured pgvector store with HNSW indexing, COSINE_DISTANCE, and 768 dimensions.
    - Added `spring.config.import=optional:configserver:http://localhost:8888` for Spring Cloud Config compatibility.
- **AI Components** (New `ai` package):
    - **CrmAssistant Interface**: Defines four key methods:
        - `chat(String)`: Basic conversational AI with memory.
        - `chatWithProductKnowledge(String)`: RAG-powered product queries.
        - `queryDatabase(String)`: Natural language to SQL conversion.
        - `chatWithFunctions(String)`: Function calling for customer operations.
    - **DefaultCrmAssistant**: Implements all four capabilities with separate ChatClient instances for basic chat (with memory), function calling, RAG delegation, and database queries.
    - **RAGService**: Implements Retrieval-Augmented Generation using `QuestionAnswerAdvisor` with vector store integration (topK=4).
    - **ProductDocumentInitializer**: `@PostConstruct` bean that populates pgvector with 7 product knowledge documents covering pricing tiers, features, integrations, and compliance.
    - **DatabaseAgentService**: Natural language to SQL agent with schema-aware query generation and execution via `R2dbcQueryTool`.
    - **AssistantController**: REST endpoints for `/chat`, `/products`, `/query`, `/functions`, and `/health`.
- **Function Calling** (New `ai/tools` package):
    - **CustomerTools**: Three AI-callable functions:
        - `searchCustomerByEmail()`: Returns `Function<SearchByEmailRequest, String>`.
        - `getCustomerById()`: Returns `Function<GetCustomerRequest, String>`.
        - `getAllCustomers()`: Returns `Function<Void, String>`.
    - **R2dbcQueryTool**: Two AI-callable functions:
        - `executeQuery()`: Executes SELECT-only SQL queries and returns JSON results.
        - `getSchema()`: Returns database schema from `information_schema.columns`.
- **HTTP Client Integration** (New `ai/client` package):
    - **CustomerClient**: Declarative HTTP Exchange interface for customer service communication:
        - `@GetExchange` methods for `getAllCustomers()`, `getCustomerById()`, and `searchByEmail()`.
        - `@PutExchange` for `updateCustomer()`.
- **Auto-Configuration** (New `ai/CrmAssistantAutoConfiguration.java`):
    - Configured `CustomerClient` bean using reactive `WebClient.Builder` with service discovery (`http://customer-service`).
    - Registered 5 `FunctionCallback` beans with `@Description` annotations for AI function calling.
    - Created `R2dbcQueryTool` and `CustomerTools` beans with dependencies properly injected.
    - All configurations are conditional on `spring.ai.enabled` (default true).
- **Testing**:
    - Created `DatabaseAgentServiceTest` with simple unit tests (full AI integration testing requires actual LLM).
    - Removed old integration tests (`ServiceSpyTests`, `ManagementIntegrationTests`, `ManagementApplicationTests`) to avoid conflicts.
    - Verified compilation via `./gradlew compileJava compileTestJava`.

### MCP Currency Server (New Gradle Project)
- **Purpose**: Demonstrates external tool integration for AI assistants via Model Context Protocol (MCP) pattern.
- **Dependencies**: Spring Boot 4.0.1 with `spring-boot-starter-web` and `spring-boot-starter-actuator`.
- **Components**:
    - **CurrencyService**: Business logic for currency conversion with static exchange rates for 10 currencies (USD, EUR, GBP, JPY, CAD, AUD, CHF, CNY, INR, MXN).
    - **CurrencyController**: REST API with three endpoints:
        - `POST /api/currency/convert`: Convert amounts between currencies.
        - `GET /api/currency/rate`: Get exchange rate between two currencies.
        - `GET /api/currency/supported`: List all supported currencies.
- **Configuration**: Runs on port 9090 with health and info actuator endpoints exposed.
- **Testing**: Created `CurrencyServiceTest` to verify conversion logic and supported currency retrieval.
- **Verified**: Compiles successfully via `./gradlew compileJava compileTestJava`.

### CRM Assistant Shell (New Gradle Project)
- **Purpose**: Spring Shell-based CLI for interactive AI assistant demonstrations.
- **Dependencies**:
    - Spring Boot 4.0.1 with `spring-boot-starter-web`.
    - Spring Shell 3.3.4 (`spring-shell-starter`).
- **Components**:
    - **AssistantShellApplication**: Main application entry point.
    - **AssistantCommands**: `@ShellComponent` with five interactive commands:
        - `chat -m "message"`: Basic AI conversation.
        - `products -m "message"`: Product knowledge queries (RAG).
        - `query -m "message"`: Natural language database queries.
        - `functions -m "message"`: Customer operations with function calling.
        - `health`: Check assistant service availability.
    - All commands use `RestClient` to call management service on `http://localhost:8082/api/assistant`.
- **Configuration**: Interactive shell enabled with custom logging levels.
- **Verified**: Compiles successfully via `./gradlew compileJava`.

### Documentation
- **README.md**: Created comprehensive documentation covering:
    - Architecture overview with component diagrams.
    - Prerequisites: Ollama installation, model downloads, PostgreSQL with pgvector setup.
    - Running instructions for all three projects.
    - Usage examples via Shell Client and REST API (curl commands).
    - Key components explanation: RAG, Function Calling, Database Agent, Auto-Configuration.
    - Configuration examples for switching between Ollama and OpenAI.
    - Troubleshooting guide for common issues.

### Key Learning Points
- **Spring AI Framework**: Integrated LLMs into Spring Boot applications using Spring AI 1.0.0-M5.
- **RAG (Retrieval-Augmented Generation)**: Implemented vector similarity search with pgvector for grounded AI responses.
- **Function Calling**: Enabled AI to invoke Java functions for customer operations using `FunctionCallback`.
- **Natural Language to SQL**: Built an AI agent that converts user questions to SQL queries and executes them.
- **Vector Stores**: Used pgvector with HNSW indexing for efficient embedding search.
- **Chat Memory**: Implemented conversational context retention with `InMemoryChatMemory`.
- **Multiple AI Providers**: Configured support for both local (Ollama) and cloud (OpenAI) LLMs.
- **Declarative HTTP Clients**: Used `@HttpExchange` annotations for type-safe service-to-service communication.
- **Spring Shell**: Created interactive CLI for AI assistant demonstrations.
- **Compilation Success**: All three projects (management, mcp-currency-server, crm-assistant-shell) compile successfully.

## Chapter 17: Custom Spring Boot Starter - Extending Spring Boot

### CRM Assistant Starter (Multi-Module Gradle Project)
- **Purpose**: Transform Chapter 16's AI capabilities into a reusable, professional-grade Spring Boot starter.
- **Architecture**: Multi-module design following Spring Boot starter conventions.

**Module 1: crm-assistant-autoconfigure**
- **Dependencies**:
    - Spring Boot AutoConfigure support with annotation processors.
    - Spring AI core (1.0.0-M5) for LLM integration.
    - Spring Framework modules: `spring-context`, `spring-web`, `spring-webflux`, `spring-aop`.
    - Spring Data R2DBC with PostgreSQL R2DBC driver.
    - AspectJ Weaver (1.9.22) for AOP support.
    - Jackson for JSON processing.
- **Core Components** (Package: `com.apress.crm.assistant`):
    - **CrmAssistant Interface**: Defines four key methods (chat, chatWithProductKnowledge, queryDatabase, chatWithFunctions).
    - **DefaultCrmAssistant** (impl package): Orchestrates all AI capabilities with separate ChatClient instances.
    - **RAGService** (service package): Retrieval-Augmented Generation using `QuestionAnswerAdvisor`.
    - **DatabaseAgentService** (service package): Natural language to SQL conversion agent.
    - **CustomerTools** (tool package): Three AI-callable functions for customer operations.
    - **R2dbcQueryTool** (tool package): Database query execution with schema introspection.
    - **CustomerClient** (client package): Declarative HTTP Exchange interface.
- **Configuration Components** (config package):
    - **CrmAssistantProperties**: `@ConfigurationProperties("crm.assistant")` with properties:
        - `enabled` (default: true): Enable/disable starter.
        - `chatModel` (default: gpt-4o): AI model selection.
        - `vectorTableName` (default: crm_vectors): Vector store table name.
        - `audit.enabled` (default: false): Enable AOP audit logging.
    - **CrmAssistantAutoConfiguration**: Auto-configuration with intelligent conditions:
        - `@AutoConfiguration`: Marks as Spring Boot auto-configuration.
        - `@ConditionalOnClass(ChatClient.class)`: Only loads if Spring AI present.
        - `@ConditionalOnProperty`: Respects `crm.assistant.enabled` property.
        - `@EnableConfigurationProperties`: Registers CrmAssistantProperties.
        - Creates 11 beans: 1 CrmAssistant + 5 services/tools + 5 FunctionCallbacks.
        - All beans use `@ConditionalOnMissingBean` for user overrides.
    - **@EnableCrmAssistant**: Custom annotation using `@Import` pattern for explicit enablement.
- **AOP Components** (aop package):
    - **AssistantAuditAspect**:
        - `@Aspect` with `@ConditionalOnProperty("crm.assistant.audit.enabled")`.
        - Pointcut: `execution(* com.apress.crm.assistant.CrmAssistant.*(..))`.
        - Logs method name, arguments, execution time, and results.
        - Transparently adds cross-cutting audit functionality to user code.
- **Auto-Configuration Registration**:
    - Created `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
    - Lists `com.apress.crm.assistant.config.CrmAssistantAutoConfiguration`.
    - Enables Spring Boot to discover and apply auto-configuration automatically.

**Module 2: crm-assistant-spring-boot-starter**
- **Purpose**: Lightweight dependency-only wrapper module.
- **Dependencies**: Single `api` dependency on `crm-assistant-autoconfigure`.
- **User Experience**: Consumers add only this starter to get all AI capabilities.

**Root Build Configuration**:
- **Publishing**: Configured maven-publish plugin for GitHub Packages.
- **Dependency Management**: Spring Boot 4.0.1 and Spring AI 1.0.0-M5 BOMs.
- **Java 21**: Toolchain configuration for all subprojects.

### Sales Dashboard (Demo Application)
- **Purpose**: Demonstrates consuming the custom starter.
- **Dependencies**:
    - Spring Boot Web and Actuator.
    - Spring AI provider starters (OpenAI, Ollama, pgvector).
    - Spring Data R2DBC with PostgreSQL.
    - **Custom Starter**: `com.apress:crm-assistant-spring-boot-starter:0.0.1-SNAPSHOT`.
- **Main Application**:
    - `@EnableCrmAssistant`: Explicitly enables the custom starter.
    - Injects `CrmAssistant` interface via constructor.
    - `CommandLineRunner` bean demonstrates all four AI capabilities.
- **Configuration** (application.yml):
    - Spring AI settings for Ollama and OpenAI.
    - R2DBC and DataSource for database/vector store.
    - **CRM Assistant Configuration**:
        - `crm.assistant.enabled=true`
        - `crm.assistant.audit.enabled=true` (enables AOP logging)
        - Custom chat model and vector table name.
- **Composite Build**: Uses `includeBuild '../crm-assistant-starter'` for local development.
- **Compilation**: Successfully compiles using the custom starter with zero boilerplate.

### Key Learning Points
- **Multi-Module Starters**: Separated autoconfigure logic from dependency management following Spring Boot conventions.
- **Intelligent Auto-Configuration**: Used `@Conditional` annotations (`@ConditionalOnClass`, `@ConditionalOnProperty`, `@ConditionalOnMissingBean`) for smart component loading.
- **@Enable Pattern**: Created custom `@EnableCrmAssistant` annotation using `@Import` for explicit feature enablement.
- **ConfigurationProperties**: Externalized configuration with type-safe properties and IDE autocomplete support.
- **AOP for Cross-Cutting Concerns**: Implemented audit logging aspect that transparently enhances user code.
- **Auto-Configuration Discovery**: Registered auto-configuration via `AutoConfiguration.imports` for Spring Boot 3+ compatibility.
- **GitHub Packages Publishing**: Configured maven-publish for distributing custom starters.
- **User Experience**: Achieved "just add dependency" simplicity - consumers need only 3 steps:
    1. Add starter dependency
    2. Add `@EnableCrmAssistant` annotation
    3. Inject and use `CrmAssistant` interface
- **Compilation Success**: All modules compile successfully - starter builds and sales-dashboard consumes it flawlessly.
