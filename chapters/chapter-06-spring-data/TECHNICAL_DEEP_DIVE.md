# Technical Deep Dive: Chapter 6 - Spring Data JDBC vs Spring Data JPA

## 1. Architectural Overview
Chapter 6 contrasts two fundamental persistence paradigms in the Spring ecosystem:
1. **Spring Data JDBC**: Lightweight, direct DDD aggregate mapping without lazy-loading or proxy tracking.
2. **Spring Data JPA (Hibernate 6/7)**: Full-featured Object-Relational Mapping (ORM) with entity lifecycle states, fetch joins, and dynamic pagination.

```mermaid
graph LR
    subgraph Spring Data JDBC (Customer)
        C_ENT[Customer Record<br/>implements Persistable] --> C_REPO[ListCrudRepository]
        C_REPO --> DIRECT_SQL[Direct SQL Execution]
    end
    subgraph Spring Data JPA (Management)
        M_ENT[Customer Entity<br/>@Entity @ManyToOne] --> M_REPO[JpaRepository]
        M_REPO --> HIBERNATE[Hibernate Session / Dirty Tracking]
    end
```

## 2. Technical Mechanisms
- **`Persistable<ID>` in Spring Data JDBC**: Explicitly handles entity newness (`isNew()`) when IDs are pre-allocated client-side (e.g. UUIDs).
- **Solving N+1 Query Degradation in JPA**:
  ```java
  @Query("SELECT c FROM Customer c JOIN FETCH c.company WHERE c.email = :email")
  Optional<Customer> findByEmailWithCompany(@Param("email") String email);
  ```
- **Pagination & Sorting**: Implements `Pageable` and `PageRequest.of(page, size, Sort.by("lastName"))`.

## 3. Build & Verification
```bash
cd chapters/chapter-06-spring-data/customer && mvn test
cd ../management && bash ./gradlew test
```
