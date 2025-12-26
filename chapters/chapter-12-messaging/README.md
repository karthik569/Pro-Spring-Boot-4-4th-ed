# Chapter 12: Messaging with Spring Boot

This chapter demonstrates various messaging technologies and patterns with Spring Boot 4 and Spring Framework 7.

## Customer Service (Maven)

Acts as a **Producer** for various messaging technologies and provides real-time browser updates via WebSockets.

### Implemented Features:

1. **Spring Application Events**
   - `CustomerCreatedEvent` - Internal event published when customers are created
   - `CustomerAuditListener` - Async listener for audit logging
   - Test: `CustomerServiceEventTest` with `@RecordApplicationEvents`

2. **JMS with JmsClient (ActiveMQ Artemis)**
   - `CustomerValidationProducer` - Fluent JmsClient API for validation requests
   - `CustomerValidationListener` - JMS message consumer
   - Test: `CustomerJmsTest` with Testcontainers

3. **RabbitMQ AMQP**
   - `AmqpConfig` - Topic exchange and queue declarations
   - `CustomerEventProducer` - RabbitMQ producer
   - Test: `RabbitMqIntegrationTest` with Testcontainers

4. **Apache Pulsar**
   - `AuditProducer` - Publishes audit logs to Pulsar

5. **WebSockets with STOMP**
   - `WebSocketConfig` - Enables STOMP over WebSocket
   - `DashboardService` - Broadcasts real-time updates via `/topic/updates`
   - Test: `WebSocketTest` - Verifies WebSocket connection

6. **Spring Integration**
   - `FeedbackIntegrationConfig` - Integration flow with filter and transform
   - `FeedbackProcessor` - Service activator
   - Test: `FeedbackFlowTest` with MockIntegrationContext

## Management Service (Gradle)

Acts as a **Consumer** for event streaming platforms and provides RSocket for high-performance communication.

### Implemented Features:

1. **Apache Kafka Consumer**
   - `LegacyEventConsumer` - Consumes from `legacy-system-events` topic
   - Test: `KafkaConsumerTest` with Testcontainers

2. **Apache Pulsar Consumer**
   - `AuditConsumer` - Subscribes to `crm-audit-topic`
   - Test: `PulsarTest` with Testcontainers and Awaitility

3. **RSocket**
   - `ManagementRSocketController` - Streams system status updates
   - Test: `RSocketTest` with StepVerifier
   - Configuration: RSocket server on port 7000

## Technologies Summary

| Technology | Type | Customer | Management |
|-----------|------|----------|------------|
| **Application Events** | Internal | ✅ Producer/Listener | - |
| **JMS (Artemis)** | Queue/Topic | ✅ Producer/Consumer | - |
| **RabbitMQ** | AMQP Exchange | ✅ Producer | - |
| **Pulsar** | Unified Messaging | ✅ Producer | ✅ Consumer |
| **Kafka** | Event Streaming | - | ✅ Consumer |
| **WebSockets** | Real-time Web | ✅ STOMP Server | - |
| **RSocket** | Reactive Streams | - | ✅ Server |
| **Spring Integration** | EIP | ✅ Flow | - |

## Running the Projects

### Customer Service (Maven)
```bash
cd customer
./mvnw spring-boot:run
```

### Management Service (Gradle)
```bash
cd management
./gradlew bootRun
```

## Testing

All tests use **Testcontainers** for realistic integration testing with actual message brokers.

### Customer Service Tests
```bash
cd customer
./mvnw test
```

### Management Service Tests
```bash
cd management
./gradlew test
```

## Configuration Notes

- **Security has been removed** from both projects for Chapter 12 focus on messaging
- RSocket server configured on port **7000** in Management service
- All messaging brokers auto-configured via Spring Boot starters
- Testcontainers handle broker lifecycle during integration tests

## Key Learning Points

1. **Asynchronous Processing**: Using `@Async` and `@EventListener` for non-blocking operations
2. **Fluent APIs**: The new `JmsClient` API for cleaner code
3. **Declarative Infrastructure**: AMQP exchanges/queues defined as Spring beans
4. **Testcontainers**: Production-like testing with real brokers
5. **Reactive Streams**: RSocket with backpressure support
6. **Enterprise Integration Patterns**: Spring Integration DSL

## Next Steps

- Chapter 13: Spring Boot Actuator for monitoring these messaging systems
- Explore adding Dead Letter Queues (DLQ) for RabbitMQ
- Implement Schema Registry for Kafka/Pulsar
- Add Spring Cloud Stream for higher-level abstractions
