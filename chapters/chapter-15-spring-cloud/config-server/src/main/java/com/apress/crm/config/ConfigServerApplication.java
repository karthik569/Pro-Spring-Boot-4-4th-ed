package com.apress.crm.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Spring Cloud Config Server - Centralized Configuration Management
 *
 * This server provides externalized configuration for all microservices in the ecosystem.
 * Configuration is stored in a Git repository and served over HTTP to client applications.
 *
 * Features:
 * - Centralized configuration management
 * - Environment-specific properties
 * - Dynamic refresh of configuration
 * - Integration with service discovery (Consul)
 */
@SpringBootApplication
@EnableConfigServer // <1>
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
