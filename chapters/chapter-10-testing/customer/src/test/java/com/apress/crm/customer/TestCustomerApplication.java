package com.apress.crm.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.CockroachContainer;

public class TestCustomerApplication {

    public static void main(String[] args) {
        SpringApplication.from(CustomerApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestcontainersConfiguration {

        @Bean
        @ServiceConnection
        CockroachContainer cockroachContainer() {
            return new CockroachContainer("cockroachdb/cockroach:v24.3.1");
        }
    }
}
