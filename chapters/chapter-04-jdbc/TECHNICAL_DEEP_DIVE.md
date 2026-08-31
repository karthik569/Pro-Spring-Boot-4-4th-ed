# Technical Deep Dive: Chapter 4 - Core JDBC & Connection Pool Management

## 1. Architectural Overview
Chapter 4 covers relational data access fundamentals, connection pool management via **HikariCP**, transactional boundaries, and low-level SQL execution with `JdbcTemplate`.

```mermaid
graph LR
    APP[Customer Application] --> POOL[HikariCP Connection Pool]
    POOL --> DB[(PostgreSQL / H2 Database)]
    APP --> TX[@Transactional Boundary]
```

## 2. Core Technical Concepts
- **HikariCP Optimization**:
  - Configures `maximum-pool-size: 10`, `minimum-idle: 5`, and `connection-timeout: 30000ms`.
  - Validates fast connection acquisition under concurrent loads.
- **JdbcTemplate Callbacks**:
  - Demonstrates `RowMapper<Customer>` implementations and parameterized queries (`PreparedStatementSetter`) to prevent SQL injection vulnerabilities.
- **Declarative Transactions**:
  - Enforces rollback semantics on unchecked (`RuntimeException`) and configured checked exceptions using `@Transactional(rollbackFor = Exception.class)`.

## 3. Build & Test
```bash
cd chapters/chapter-04-jdbc/customer
mvn compile -DskipTests
```
