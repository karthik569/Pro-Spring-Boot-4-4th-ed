# Technical Deep Dive: Chapter 1 - Spring Boot 4 Quick Start & Dual-Stack Bootstrap

## 1. Architectural Overview
Chapter 1 establishes the foundational Dual-Stack microservice architecture used throughout the book:
- **Customer Service (`customer`)**: Imperative Spring WebMVC over Apache Tomcat using Apache Maven.
- **Management Service (`management`)**: Non-blocking Reactive Spring WebFlux over Netty using Gradle 9.2+.

```mermaid
graph LR
    subgraph Customer Service (Maven / WebMVC)
        C_CTRL[CustomerController<br/>@RestController] --> C_REPO[CustomerRepository<br/>ConcurrentHashMap]
        C_REPO --> C_MODEL[Customer Record<br/>Immutable DTO]
    end
    subgraph Management Service (Gradle / WebFlux)
        M_ROUTER[ManagementHandlers<br/>Router Functions] --> M_SVC[ManagementService<br/>Reactive Flux/Mono]
        M_SVC --> M_REPO[Reactive Repositories]
    end
```

## 2. Core Implementation Details
### Customer Microservice (Maven / WebMVC)
- **Model**: Uses Java 21 `record Customer(UUID id, String firstName, String lastName, String email)` for immutable value semantics.
- **Repository**: In-memory thread-safe storage using `ConcurrentHashMap<UUID, Customer>`.
- **REST Endpoints**:
  - `GET /customer`: List all customers.
  - `GET /customer/{id}`: Single customer lookup.
  - `POST /customer`: Customer creation with auto-generated UUID.
  - `DELETE /customer/{id}`: Customer deletion.
- **Lifecycle Bootstrap**: Implements `ApplicationReadyEvent` listener in `CustomerConfiguration` to seed initial test records at application startup.

### Management Microservice (Gradle / WebFlux)
- **Functional Endpoints**: Declares routing table via `RouterFunctions.route()` in `ManagementConfiguration`.
- **Non-blocking Pipelines**: Handlers return `Mono<ServerResponse>` and stream collections via `Flux<CustomerDetailsDTO>`.

## 3. Build & Execution Commands
```bash
# Customer (Maven)
cd chapters/chapter-01-quick-start/customer
mvn clean compile -DskipTests
mvn spring-boot:run

# Management (Gradle)
cd chapters/chapter-01-quick-start/management
bash ./gradlew classes
bash ./gradlew bootRun
```

## 4. Key Takeaways & Best Practices
1. **Java Records**: Always leverage records for DTOs and immutable payload representations.
2. **Constructor Injection**: Omit `@Autowired` on single constructors; Spring Boot 4 automatically wires dependencies.
3. **Reactive vs Imperative**: Choose WebMVC for standard relational I/O; select WebFlux when building streaming, high-concurrency event aggregators.
