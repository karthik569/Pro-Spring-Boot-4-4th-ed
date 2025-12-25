package com.apress.crm.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.CockroachContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.sql.init.mode=always"
})
@Testcontainers
class CustomerApplicationTests {

    @Container
    static CockroachContainer cockroach = new CockroachContainer("cockroachdb/cockroach:v25.4.0");

    @DynamicPropertySource
    static void r2dbcProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> String.format("r2dbc:postgresql://%s:%d/defaultdb",
                cockroach.getHost(), cockroach.getMappedPort(26257)));
        registry.add("spring.r2dbc.username", cockroach::getUsername);
        registry.add("spring.r2dbc.password", cockroach::getPassword);
    }

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp(ApplicationContext context) {
        this.webTestClient = WebTestClient.bindToApplicationContext(context).build();
    }

    @Test
    void shouldHandleConcurrentWritesWithVirtualThreads() throws InterruptedException {
        int concurrentUsers = 10;
        CountDownLatch latch = new CountDownLatch(concurrentUsers);
        AtomicInteger successCount = new AtomicInteger();

        // Use Virtual Threads to simulate high concurrency
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < concurrentUsers; i++) {
                int index = i;
                executor.submit(() -> {
                    try {
                        Customer newCustomer = new Customer("User", "" + index, "user" + index + "@test.com", "1-800-PHONE");

                        webTestClient.post()
                            .uri("/api/v1/customers")
                            .bodyValue(newCustomer)
                            .exchange()
                            .expectStatus().isCreated();

                        successCount.incrementAndGet();
                    } catch (Throwable e) {
                        System.err.println("Error for user " + index + ": " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }

        latch.await(15, TimeUnit.SECONDS);
        assertThat(successCount.get()).isEqualTo(concurrentUsers);
    }
}
