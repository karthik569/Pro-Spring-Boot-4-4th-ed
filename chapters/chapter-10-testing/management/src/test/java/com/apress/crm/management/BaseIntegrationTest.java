package com.apress.crm.management;

import org.testcontainers.containers.CockroachContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@Testcontainers
public abstract class BaseIntegrationTest {

    @Container
    static CockroachContainer cockroach = new CockroachContainer("cockroachdb/cockroach:v25.4.0");

    @DynamicPropertySource
    static void r2dbcProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> String.format("r2dbc:postgresql://%s:%d/defaultdb",
                cockroach.getHost(), cockroach.getMappedPort(26257)));
        registry.add("spring.r2dbc.username", cockroach::getUsername);
        registry.add("spring.r2dbc.password", cockroach::getPassword);
    }
}
