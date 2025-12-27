package com.apress.crm.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Cloud Gateway - API Gateway for CRM Microservices
 *
 * This gateway provides a unified entry point for all client applications.
 * It handles routing, load balancing, circuit breaking, and other cross-cutting concerns.
 *
 * Features:
 * - Dynamic routing to customer-service and management-service
 * - Client-side load balancing via Consul service discovery
 * - Circuit breakers with Resilience4J for fault tolerance
 * - Centralized configuration via Config Server
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
