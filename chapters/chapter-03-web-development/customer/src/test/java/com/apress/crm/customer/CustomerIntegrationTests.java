package com.apress.crm.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class CustomerIntegrationTests {

    @Autowired
    private RestTestClient client; // Auto-configured via @AutoConfigureRestTestClient

    @Test
    void shouldCreateCustomer() {
        Customer customer = new Customer("John Doe", "john@example.com", "123456789");

        client.post().uri("/api/v1/customers")
                .body(customer)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("Location");
    }

}
