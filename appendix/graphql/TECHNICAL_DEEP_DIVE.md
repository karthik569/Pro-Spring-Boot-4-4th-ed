# Technical Deep Dive: Appendix - Spring for GraphQL

## 1. Architectural Overview
Spring for GraphQL provides a flexible schema-first query language interface over Spring MVC / WebFlux, eliminating over-fetching and under-fetching.

## 2. Key Capabilities
- **Schema Mapping**: Annotated controller methods using `@QueryMapping`, `@MutationMapping`, and `@SubscriptionMapping`.
- **Batch Mapping**: Resolves $N+1$ query issues across GraphQL resolvers using `@BatchMapping` and `DataLoader`.
