package com.apress.prospringboot4.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Customer gRPC Server Application.
 *
 * This application demonstrates Spring gRPC 1.0.1 server implementation
 * with Spring Boot 4, showcasing:
 * - Protocol Buffers for service contracts
 * - gRPC service implementation with @GrpcService
 * - Virtual Threads support (Java 21)
 * - Native observability with Actuator
 */
@SpringBootApplication
public class CustomerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerApplication.class, args);
    }
}
