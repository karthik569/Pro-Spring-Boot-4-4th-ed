# Chapter 4: JDBC with Spring Boot

This directory contains the source code for Chapter 4 of the book "Pro Spring Boot 4".

## Learning Objectives
- Understand Spring Boot's powerful JDBC (Java Database Connectivity) support.
- Implement a persistence layer to store data reliably in a relational database.
- Explore data access using both the classic `JdbcTemplate` and the modern, fluent `JdbcClient`.
- Set up realistic development environments using an in-memory database (H2) for rapid testing and a real PostgreSQL database for production-like simulation.

## Key Topics
- DataSource configuration
- Database initialization with schema.sql
- H2 in-memory database and console
- Production setup with PostgreSQL and Docker Compose
- Classic data access with `JdbcTemplate` and `RowMapper`
- Modern fluent data access with `JdbcClient`
- Transaction management basics
- Testing strategies:
  - Integration testing with `@SpringBootTest`
  - Sliced testing with `@JdbcTest`
  - Testing against production databases
