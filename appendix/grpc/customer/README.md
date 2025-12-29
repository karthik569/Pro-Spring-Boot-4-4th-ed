# Customer gRPC Server

This project demonstrates a **gRPC server** implementation using **Spring gRPC 1.0.1** with **Spring Boot 4** and **Java 21**.

## Overview

The Customer gRPC Server exposes a **CustomerRiskService** that provides risk assessment profiles for customers. It showcases:

- Protocol Buffers (`.proto`) for service contracts
- `@GrpcService` annotation for service implementation
- Virtual Threads support for high concurrency
- Native observability with Spring Boot Actuator
- gRPC server configuration

## Technology Stack

- **Spring Boot**: 4.0.1
- **Spring gRPC**: 1.0.1
- **Java**: 21 (with Virtual Threads)
- **Protocol Buffers**: 3.25.5
- **gRPC**: 1.69.0
- **Build Tool**: Maven

## Project Structure

```
customer/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/apress/prospringboot4/customer/
│   │   │       ├── CustomerApplication.java          # Main application
│   │   │       └── grpc/
│   │   │           └── CustomerRiskGrpcService.java  # gRPC service implementation
│   │   ├── proto/
│   │   │   └── customer-service.proto                # Protocol Buffer definition
│   │   └── resources/
│   │       └── application.yml                       # Configuration
│   └── test/
│       └── java/
│           └── com/apress/prospringboot4/customer/
│               ├── CustomerApplicationTests.java
│               └── grpc/
│                   └── CustomerRiskGrpcServiceTest.java
└── pom.xml                                           # Maven build file
```

## Service Contract

The service is defined in `customer-service.proto`:

```protobuf
service CustomerRiskService {
  rpc GetRiskProfile (RiskRequest) returns (RiskResponse);
}

message RiskRequest {
  string customer_id = 1;
}

message RiskResponse {
  string customer_id = 1;
  string risk_level = 2;  // LOW, MEDIUM, HIGH
  double score = 3;
}
```

## Building the Project

### Generate Protocol Buffer Code

```bash
./mvnw protobuf:compile protobuf:compile-custom
```

Generated files will be in `target/generated-sources/protobuf/`.

### Compile the Project

```bash
./mvnw compile
```

### Run Tests

```bash
./mvnw test
```

### Package the Application

```bash
./mvnw clean package
```

## Running the Application

### Start the Server

```bash
./mvnw spring-boot:run
```

The gRPC server will start on port **9090** (configured in `application.yml`).

### Expected Output

```
Customer gRPC Server Application started
gRPC Server started on port 9090
```

## Configuration

The server is configured in `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: customer-grpc-server
  threads:
    virtual:
      enabled: true        # Enable Virtual Threads (Java 21)
  grpc:
    server:
      port: 9090          # gRPC server port
```

## Testing the Service

### Using grpcurl

If you have [grpcurl](https://github.com/fullstorydev/grpcurl) installed:

```bash
# List services
grpcurl -plaintext localhost:9090 list

# Call the GetRiskProfile method
grpcurl -plaintext -d '{"customer_id":"CUST-001"}' \
  localhost:9090 CustomerRiskService/GetRiskProfile
```

### Using the Management Client

Start the **management** gRPC client project (in the `../management` directory) to consume this service.

## Sample Data

The service includes demo risk profiles:

| Customer ID | Risk Level | Score |
|-------------|-----------|-------|
| CUST-001 | LOW | 0.95 |
| CUST-002 | MEDIUM | 0.65 |
| CUST-003 | HIGH | 0.25 |
| CUST-004 | LOW | 0.88 |
| Unknown | MEDIUM | 0.50 |

## Key Features

### Virtual Threads (Java 21)

The server uses Virtual Threads for handling gRPC requests, providing:
- High concurrency with minimal memory overhead
- Simplified blocking programming model
- Excellent scalability

### Native Observability

Built-in integration with:
- **Micrometer** for metrics
- **Spring Boot Actuator** for health checks
- Metrics endpoint: `http://localhost:8080/actuator/metrics`

### Error Handling

The service properly handles errors and returns appropriate gRPC status codes.

## Development

### Adding New RPC Methods

1. Update `customer-service.proto` with new RPC definitions
2. Run `./mvnw protobuf:compile protobuf:compile-custom`
3. Implement the new methods in `CustomerRiskGrpcService`

### Modifying Risk Profiles

Edit the `CustomerRiskGrpcService` constructor to add/modify customer risk data.

## Troubleshooting

### Port Already in Use

If port 9090 is already in use, change it in `application.yml`:

```yaml
spring:
  grpc:
    server:
      port: 9091  # Use a different port
```

### Proto Compilation Issues

Ensure you have the correct protoc version:

```bash
./mvnw protobuf:help
```

## Learn More

- [Spring gRPC Documentation](https://docs.spring.io/spring-grpc/reference/)
- [gRPC Official Documentation](https://grpc.io/docs/)
- [Protocol Buffers Guide](https://protobuf.dev/)
- [Spring Boot 4 Documentation](https://docs.spring.io/spring-boot/index.html)
