package com.apress.crm.management;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CockroachContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.banner-mode=off",
        "spring.sql.init.mode=always",
        "spring.profiles.active=test"
    }
)
class ManagementIntegrationTests extends BaseIntegrationTest {

    @Test
    void contextLoads() {
        assertThat(cockroach.isRunning()).isTrue();
    }
}
