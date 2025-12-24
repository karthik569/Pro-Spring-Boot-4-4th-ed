package com.apress.crm.management;

import com.apress.crm.management.model.Customer;
import com.apress.crm.management.model.CustomerDetailsDTO;
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

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.sql.init.mode=always"
})
@Testcontainers
class ManagementApplicationTests {

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
    void shouldServeConcurrentReadsFromCache() {
        // 1. Setup: Create a customer
        // We use a simplified customer object, the handler will fill the rest
        Customer newCustomer = new Customer(null, "Cache", "Target", "Manager", "cache@test.com", "555-CACHE", null);

        Customer created = webTestClient.post()
            .uri("/api/v1/customers")
            .bodyValue(newCustomer)
            .exchange()
            .expectStatus().isCreated()
            .returnResult(Customer.class)
            .getResponseBody()
            .blockFirst();

        assertThat(created).isNotNull();
        UUID id = created.customerId();

        // 2. Simulate 100 concurrent users reading the same customer
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, 100).forEach(i ->
                executor.submit(() -> {
                    webTestClient.get()
                        .uri("/api/v1/customers/" + id)
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(CustomerDetailsDTO.class) // The GET endpoint returns CustomerDetailsDTO
                        .value(dto -> {
                            assertThat(dto.customer().customerId()).isEqualTo(id);
                            assertThat(dto.customer().firstName()).isEqualTo("Cache");
                        });
                })
            );
        }
        // The test passes if all requests succeed without error
    }
}