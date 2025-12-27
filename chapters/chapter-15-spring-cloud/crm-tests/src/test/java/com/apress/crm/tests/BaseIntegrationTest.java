package com.apress.crm.tests;

/**
 * Base class for integration tests of Spring Cloud CRM microservices.
 *
 * IMPORTANT: Before running these tests, ensure all services are running:
 *   1. docker-compose up -d
 *   2. cd config-server && ./mvnw spring-boot:run
 *   3. cd customer && ./mvnw spring-boot:run
 *   4. cd management && ./gradlew bootRun
 *   5. cd api-gateway && ./mvnw spring-boot:run
 *
 * These tests assume the services are already running and available at:
 * - Config Server: http://localhost:8888
 * - Customer Service: http://localhost:8081
 * - Management Service: http://localhost:8082
 * - API Gateway: http://localhost:8080
 * - Consul: http://localhost:8500
 * - RabbitMQ: localhost:5672 (Management UI: http://localhost:15672)
 */
public abstract class BaseIntegrationTest {

    // Service URLs - services must be running before tests
    protected static final String CONFIG_SERVER_URL = "http://localhost:8888";
    protected static final String CUSTOMER_SERVICE_URL = "http://localhost:8081";
    protected static final String MANAGEMENT_SERVICE_URL = "http://localhost:8082";
    protected static final String API_GATEWAY_URL = "http://localhost:8080";
    protected static final String CONSUL_URL = "http://localhost:8500";
}
