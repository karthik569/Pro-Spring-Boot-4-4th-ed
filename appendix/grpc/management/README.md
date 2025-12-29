# Management gRPC Client

This project demonstrates a **gRPC client** implementation using **Spring gRPC 1.0.1** with **Spring Boot 4** and **Java 21**.

## Overview

The Management gRPC Client consumes the **CustomerRiskService** from the Customer gRPC Server. It showcases:

- `@ImportGrpcClients` for automatic stub registration
- Blocking stub injection and usage
- gRPC client configuration via `application.yml`
- Virtual Threads support
- Error handling and observability

## Technology Stack

- **Spring Boot**: 4.0.1
- **Spring gRPC**: 1.0.1
- **Java**: 21 (with Virtual Threads)
- **Protocol Buffers**: 3.25.5
- **gRPC**: 1.69.0
- **Build Tool**: Gradle (Kotlin DSL)

## Project Structure

```
management/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/apress/prospringboot4/management/
│   │   │       ├── ManagementApplication.java       # Main application
│   │   │       ├── config/
│   │   │       │   └── GrpcClientConfig.java       # gRPC client configuration
│   │   │       └── service/
│   │   │           └── RiskAssessmentService.java  # Service using gRPC client
│   │   ├── proto/
│   │   │   └── customer-service.proto              # Protocol Buffer definition
│   │   └── resources/
│   │       └── application.yml                     # Configuration
│   └── test/
│       └── java/
│           └── com/apress/prospringboot4/management/
│               ├── ManagementApplicationTests.java
│               └── service/
│                   └── RiskAssessmentServiceTest.java
└── build.gradle.kts                                # Gradle build file
```

## Service Contract

The client uses the same `.proto` file as the server:

```protobuf
service CustomerRiskService {
  rpc GetRiskProfile (RiskRequest) returns (RiskResponse);
}
```

## Building the Project

### Generate Protocol Buffer Code

```bash
./gradlew generateProto
```

Generated files will be in `build/generated/source/proto/`.

### Compile the Project

```bash
./gradlew compileJava
```

### Run Tests

```bash
./gradlew test
```

### Build the Application

```bash
./gradlew build
```

## Running the Application

### Prerequisites

**The Customer gRPC Server must be running first!**

1. Start the customer server (in `../customer` directory):
   ```bash
   cd ../customer
   ./mvnw spring-boot:run
   ```

2. Wait for the server to start on port 9090

### Start the Client

```bash
./gradlew bootRun
```

### Expected Output

```
==========================================================
Management gRPC Client Demo
==========================================================

Assessing customer: CUST-001
  ├─ Risk Level: LOW
  └─ Score: 0.95

Assessing customer: CUST-002
  ├─ Risk Level: MEDIUM
  └─ Score: 0.65

Assessing customer: CUST-003
  ├─ Risk Level: HIGH
  └─ Score: 0.25

Assessing customer: CUST-999
  ├─ Risk Level: MEDIUM
  └─ Score: 0.50

==========================================================
Demo Complete!
==========================================================
```

## Configuration

The client is configured in `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: management-grpc-client
  threads:
    virtual:
      enabled: true
  grpc:
    client:
      channels:
        customer-service:
          address: 'static://localhost:9090'
          negotiation-type: plaintext
```

### Key Configuration Options

- **address**: The gRPC server address (`static://host:port`)
- **negotiation-type**: `plaintext` for development (use TLS in production)

## How It Works

### 1. Client Registration

`GrpcClientConfig` uses `@ImportGrpcClients` to register the stub:

```java
@Configuration
@ImportGrpcClients(CustomerRiskServiceGrpc.class)
public class GrpcClientConfig {
}
```

### 2. Stub Injection

The blocking stub is injected into services:

```java
@Service
public class RiskAssessmentService {
    private final CustomerRiskServiceGrpc.CustomerRiskServiceBlockingStub riskStub;

    public RiskAssessmentService(
        CustomerRiskServiceGrpc.CustomerRiskServiceBlockingStub riskStub) {
        this.riskStub = riskStub;
    }
}
```

### 3. Making gRPC Calls

```java
RiskRequest request = RiskRequest.newBuilder()
    .setCustomerId("CUST-001")
    .build();

RiskResponse response = riskStub.getRiskProfile(request);
```

## Testing

### Unit Tests

The project includes unit tests using Mockito to mock the gRPC stub:

```bash
./gradlew test
```

### Integration Testing

To test the full gRPC communication:

1. Start the customer server
2. Run the management client
3. Verify the output shows correct risk profiles

## Key Features

### Virtual Threads

The client uses Virtual Threads for handling concurrent gRPC calls:
- Simplified blocking API
- High throughput with minimal threads
- No need for reactive programming

### Error Handling

The client properly handles gRPC errors:
- `StatusRuntimeException` for gRPC-specific errors
- Proper logging and error propagation
- Graceful degradation

### Observability

Built-in integration with:
- Spring Boot Actuator
- Micrometer metrics
- Logging for all gRPC calls

## Development

### Adding New Client Methods

1. Ensure the `.proto` file is updated
2. Run `./gradlew generateProto`
3. Add new methods to `RiskAssessmentService`
4. Inject and use the generated stub

### Using Async Stubs

For non-blocking calls, inject the async stub:

```java
private final CustomerRiskServiceGrpc.CustomerRiskServiceStub asyncStub;
```

### Configuring Deadlines

Add timeouts to prevent hanging calls:

```java
RiskResponse response = riskStub
    .withDeadlineAfter(5, TimeUnit.SECONDS)
    .getRiskProfile(request);
```

## Troubleshooting

### Connection Refused

**Error**: `UNAVAILABLE: io exception`

**Solution**: Ensure the customer server is running on port 9090:

```bash
cd ../customer
./mvnw spring-boot:run
```

### Wrong Port

If the server is on a different port, update `application.yml`:

```yaml
spring:
  grpc:
    client:
      channels:
        customer-service:
          address: 'static://localhost:9091'  # Updated port
```

### Proto Compilation Issues

Clean and regenerate:

```bash
./gradlew clean generateProto
```

## Production Considerations

### Use TLS

For production, enable TLS encryption:

```yaml
spring:
  grpc:
    client:
      channels:
        customer-service:
          address: 'static://production-server:443'
          negotiation-type: TLS
```

### Service Discovery

For microservices environments, use service discovery instead of static addresses:

```yaml
spring:
  grpc:
    client:
      channels:
        customer-service:
          address: 'dns:///customer-service.default.svc.cluster.local:9090'
```

### Load Balancing

gRPC supports client-side load balancing automatically when using DNS or service discovery.

## Learn More

- [Spring gRPC Documentation](https://docs.spring.io/spring-grpc/reference/)
- [gRPC Client Guide](https://grpc.io/docs/languages/java/basics/#client)
- [Spring Boot 4 Documentation](https://docs.spring.io/spring-boot/index.html)
