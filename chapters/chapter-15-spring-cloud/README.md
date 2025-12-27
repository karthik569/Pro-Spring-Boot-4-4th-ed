# Chapter 15: Building Cloud-Native Microservices with Spring Cloud

This chapter demonstrates a complete microservices architecture using Spring Cloud with Spring Boot 4.0.1.

## Architecture Overview

The ecosystem consists of the following components:

```
┌─────────────────┐
│  External       │
│  Clients        │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  API Gateway    │  (Port 8080)
│  - Routing      │
│  - Circuit      │
│    Breakers     │
└────────┬────────┘
         │
    ┌────┴─────┐
    │          │
    ▼          ▼
┌─────────┐ ┌──────────────┐
│Customer │ │ Management   │
│Service  │ │ Service      │
│(8081)   │ │ (8082)       │
└────┬────┘ └──────┬───────┘
     │             │
     │  ┌──────────┴────────┐
     │  │                   │
     ▼  ▼                   ▼
┌─────────────┐      ┌──────────────┐
│  RabbitMQ   │      │  PostgreSQL  │
│  (Events)   │      │  (Databases) │
└─────────────┘      └──────────────┘
         │
         ▼
   ┌─────────────┐
   │   Consul    │  (Service Discovery)
   │   (8500)    │
   └─────────────┘
         │
         ▼
   ┌─────────────┐
   │Config Server│  (Centralized Config)
   │   (8888)    │
   └─────────────┘
```

## Components

### Infrastructure Services (via Docker Compose)

- **Consul** (Port 8500) - Service discovery and health checking
- **RabbitMQ** (Ports 5672, 15672) - Message broker for event-driven communication
- **PostgreSQL Customer DB** (Port 5432) - Database for customer-service
- **PostgreSQL Management DB** (Port 5433) - Database for management-service

### Spring Boot Applications

1. **config-server** (Port 8888)
   - Centralized configuration management
   - Serves configuration from local file system
   - Registers with Consul

2. **api-gateway** (Port 8080)
   - Spring Cloud Gateway (reactive)
   - Routes to customer-service and management-service
   - Circuit breakers with Resilience4J
   - Service discovery integration

3. **customer-service** (Port 8081)
   - Customer management (JPA, Spring MVC)
   - Publishes events when customers are created/updated
   - Calls management-service for companies/addresses
   - Uses @LoadBalanced RestClient

4. **management-service** (Port 8082)
   - Manages companies, addresses, communications (R2DBC, WebFlux)
   - Consumes customer events from RabbitMQ
   - Provides REST APIs for customer-service
   - OpenTelemetry tracing enabled

## Spring Cloud Features Demonstrated

### 1. Service Discovery (Consul)
- Automatic service registration
- Health check integration with Spring Boot Actuator
- Client-side load balancing

### 2. Centralized Configuration (Spring Cloud Config)
- Git-backed configuration (using local filesystem for demo)
- Environment-specific properties
- Dynamic configuration refresh capability

### 3. API Gateway (Spring Cloud Gateway)
- Unified entry point for all services
- Dynamic routing based on service discovery
- Circuit breaker integration
- Request/response filtering

### 4. Circuit Breakers (Resilience4J)
- Fault tolerance and resilience
- Graceful degradation with fallbacks
- Automatic circuit opening/closing based on failure rates

### 5. Event-Driven Communication (Spring Cloud Stream)
- RabbitMQ integration
- Function-based programming model
- Producer: customer-service publishes events via StreamBridge
- Consumer: management-service consumes events via @Bean Consumer

### 6. Load-Balanced REST Communication
- @LoadBalanced RestClient
- Service-to-service calls using service names
- Automatic failover to healthy instances

## Prerequisites

- Java 21
- Maven 3.9+ (for customer, config-server, api-gateway)
- Gradle 8.x (for management)
- Docker and Docker Compose

## Quick Start

### 1. Start Infrastructure Services

```bash
# Start Consul, RabbitMQ, and PostgreSQL databases
docker-compose up -d

# Verify services are running
docker-compose ps

# Access Consul UI: http://localhost:8500
# Access RabbitMQ UI: http://localhost:15672 (guest/guest)
```

### 2. Start Config Server

```bash
cd config-server
./mvnw spring-boot:run

# Verify it's registered in Consul:
# http://localhost:8500/ui/dc1/services/config-server

# Test configuration endpoint:
curl http://localhost:8888/customer-service/default
```

### 3. Start Customer Service

```bash
cd customer
./mvnw spring-boot:run

# Verify registration in Consul:
# http://localhost:8500/ui/dc1/services/customer-service

# Test health endpoint:
curl http://localhost:8081/actuator/health
```

### 4. Start Management Service

```bash
cd management
./gradlew bootRun

# Verify registration in Consul:
# http://localhost:8500/ui/dc1/services/management-service

# Test health endpoint:
curl http://localhost:8082/actuator/health
```

### 5. Start API Gateway

```bash
cd api-gateway
./mvnw spring-boot:run

# Verify registration in Consul:
# http://localhost:8500/ui/dc1/services/api-gateway

# Test gateway routes:
curl http://localhost:8080/actuator/health
```

## Testing the Ecosystem

### 1. Test Service Discovery via Gateway

```bash
# Access customer service through gateway
curl http://localhost:8080/api/customers

# Access management service through gateway
curl http://localhost:8080/api/management/companies
curl http://localhost:8080/api/management/addresses
```

### 2. Test Event-Driven Communication

```bash
# Create a customer (will publish event to RabbitMQ)
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -u admin:admin \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com",
    "phone": "+1-555-0123"
  }'

# Check management service logs - should see event consumption
# Check RabbitMQ UI to see message flow: http://localhost:15672
```

### 3. Test Circuit Breaker

```bash
# Stop management service
cd management
# Press Ctrl+C

# Try accessing through gateway - should get fallback response
curl http://localhost:8080/api/management/companies

# Expected response:
# {
#   "message": "Management service is temporarily unavailable...",
#   "status": "circuit-open"
# }

# Check circuit breaker state:
curl http://localhost:8080/actuator/circuitbreakers
```

## Configuration Details

### Customer Service Configuration
- Database: PostgreSQL on port 5432 (customerdb)
- Spring Cloud Stream output: `customer-events-topic`
- Consul health check: `/actuator/health`

### Management Service Configuration
- Database: PostgreSQL on port 5433 (managementdb)
- Spring Cloud Stream input: `customer-events-topic` (group: management-group)
- OpenTelemetry tracing enabled
- R2DBC connection pooling enabled

### API Gateway Routes
- `/api/customers/**` → customer-service (with circuit breaker)
- `/api/management/**` → management-service (with circuit breaker)

## Monitoring and Observability

### Consul UI
- URL: http://localhost:8500
- View all registered services
- Monitor health checks
- See service instances

### RabbitMQ Management UI
- URL: http://localhost:15672
- Credentials: guest/guest
- Monitor queues and exchanges
- View message rates

### Actuator Endpoints
All services expose:
- `/actuator/health` - Health status
- `/actuator/info` - Application info
- `/actuator/prometheus` - Prometheus metrics
- `/actuator/metrics` - Micrometer metrics

API Gateway also exposes:
- `/actuator/gateway/routes` - Configured routes
- `/actuator/circuitbreakers` - Circuit breaker status

## Troubleshooting

### Services not registering with Consul
```bash
# Check Consul is running
docker-compose ps consul

# Check service logs for Consul connection errors
# Verify spring.cloud.consul.host and port in application.yml
```

### RabbitMQ connection issues
```bash
# Check RabbitMQ is running
docker-compose ps rabbitmq

# Verify connection in RabbitMQ UI: http://localhost:15672
# Check spring.rabbitmq.host and port in Config Server configuration
```

### Database connection issues
```bash
# Check PostgreSQL containers
docker-compose ps postgres-customer postgres-management

# Test database connectivity
docker exec -it postgres-customer psql -U customer -d customerdb
docker exec -it postgres-management psql -U management -d managementdb
```

## Development Tips

### Hot Reload
Use Spring Boot DevTools for automatic restart

### Accessing Config Server Configuration
```bash
# View customer-service config
curl http://localhost:8888/customer-service/default

# View management-service config
curl http://localhost:8888/management-service/default

# View api-gateway config
curl http://localhost:8888/api-gateway/default
```

## Stopping Services

```bash
# Stop all applications (Ctrl+C in each terminal)

# Stop infrastructure
docker-compose down

# Stop infrastructure and remove volumes
docker-compose down -v
```

## Additional Resources

- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [Spring Cloud Consul](https://spring.io/projects/spring-cloud-consul)
- [Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream)
- [Resilience4J](https://resilience4j.readme.io/)

---

**Pro Spring Boot 4** - Chapter 15 Example Code
