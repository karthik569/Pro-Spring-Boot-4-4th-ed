# Technical Deep Dive: Chapter 7 - NoSQL & Vector Embeddings with MongoDB

## 1. Architectural Overview
Chapter 7 covers NoSQL document storage with **MongoDB**, document relationships, geospatial querying, and cutting-edge **Vector Search** integrations for AI similarity matching.

```mermaid
graph TD
    DOC[Customer Document<br/>@Document] --> VEC[Vector Embeddings<br/>Float Array]
    VEC --> VSEARCH[@VectorSearch Query<br/>Cosine Similarity]
    DOC --> TEST[Testcontainers MongoDB<br/>Automated Testing]
```

## 2. Key Features
- **Document Mapping**: Models flexible JSON documents with `@Document("customers")` and `@Id String id`.
- **Vector Search Support**:
  ```java
  @VectorSearch(path = "vector", limit = 5)
  List<Customer> findByVectorNear(Vector vector, Limit limit);
  ```
- **Testcontainers Integration**: Executes integration tests against containerized MongoDB 7 instances via `@Container` and `@DynamicPropertySource`.

## 3. Build & Test
```bash
cd chapters/chapter-07-nosql/customer && mvn test
cd ../management && bash ./gradlew test
```
