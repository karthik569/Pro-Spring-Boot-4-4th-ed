# Chapter 7: NoSQL with Spring Boot

This directory contains the source code for Chapter 7 of the book "Pro Spring Boot 4".

## Learning Objectives
- Explore how Spring Boot 4 and Spring Data provide a consistent programming model for NoSQL technologies.
- Refactor projects to leverage MongoDB, Redis, and Neo4j.
- Understand the trend of Vector Search for AI applications within NoSQL databases.

## Key Topics
- Document Persistence with Spring Data MongoDB:
  - `MongoRepository` and `MongoTemplate` usage
  - Best practices for records and ID management
  - Integration testing with Testcontainers
- Key-Value Persistence with Spring Data Redis:
  - Caching and session management
  - Reactive support with `ReactiveRedisTemplate`
  - Serialization strategies
- Graph Databases with Spring Data Neo4j:
  - Using `Neo4jClient` for fluent Cypher execution
  - Modeling corporate hierarchies
  - Polyglot persistence strategies
- The AI Frontier: Vector Search in NoSQL:
  - Semantic feedback analysis
  - Vectorization workflow and storage
- Comparing NoSQL database types (Document, Key-Value, Graph, Vector)
