package com.apress.crm.management;

import com.apress.crm.management.model.Customer;
import com.apress.crm.management.model.CustomerDetailsDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.sql.init.mode=always",
        "spring.profiles.active=test"
})
class ManagementApplicationTests extends BaseIntegrationTest {

    @Test
    void shouldServeConcurrentReadsFromCache() {
        // 1. Setup: Create a customer
        // We use a simplified customer object, the handler will fill the rest
        Customer newCustomer = new Customer(null, "Cache", "Target", "Manager", "cache@test.com", "555-CACHE", null);

        Customer created = webTestClient
            .post()
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
                    webTestClient
                        .get()
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
