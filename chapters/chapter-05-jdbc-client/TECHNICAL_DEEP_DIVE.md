# Technical Deep Dive: Chapter 5 - Modern JdbcClient Fluent Data Access

## 1. Architectural Overview
Chapter 5 demonstrates the transition from legacy `JdbcTemplate` to Spring Framework 6.1+ **`JdbcClient`**, combining the efficiency of plain SQL with a fluent, type-safe builder interface.

```mermaid
graph TD
    JC[JdbcClient.create()] --> SQL[.sql(query)]
    SQL --> PARAM[.param(name, value)]
    PARAM --> QUERY[.query(Customer.class)]
    QUERY --> OPT[.optional() / .list()]
```

## 2. Technical Innovations
### Database-Agnostic "Update-then-Insert" Pattern
To avoid database dialect coupling (such as PostgreSQL `ON CONFLICT` vs H2 syntax), the repository implements a portable pattern:
```java
public Customer save(Customer customer) {
    int rows = jdbcClient.sql("UPDATE customer SET first_name = :first, last_name = :last WHERE id = :id")
        .param("first", customer.firstName())
        .param("last", customer.lastName())
        .param("id", customer.id())
        .update();
    if (rows == 0) {
        jdbcClient.sql("INSERT INTO customer (id, first_name, last_name, email) VALUES (:id, :first, :last, :email)")
            .param("id", customer.id())
            .param("first", customer.firstName())
            .param("last", customer.lastName())
            .param("email", customer.email())
            .update();
    }
    return customer;
}
```

### Automatic Record Reflection vs Custom RowMapper
- Supports zero-boilerplate automatic binding directly to Java records.
- Supports custom `RowMapper` instances for complex multi-table projections.

## 3. Build & Verification
```bash
# Customer (Maven)
cd chapters/chapter-05-jdbc-client/customer
mvn test

# Management (Gradle)
cd chapters/chapter-05-jdbc-client/management
bash ./gradlew test
```
