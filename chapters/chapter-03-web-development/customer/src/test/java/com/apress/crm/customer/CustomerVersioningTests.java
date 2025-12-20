package com.apress.crm.customer;

import com.apress.crm.customer.v1.CustomerV1;
import com.apress.crm.customer.v2.CustomerV2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class CustomerVersioningTests {

    @Autowired
    private RestTestClient client;

    @Test
    void testLegacyVsModernApi() {
        // 1. Legacy API (V1) - Accepts Invalid Email
        CustomerV1 badDataV1 = new CustomerV1(null, "John", "not-an-email");

        client.post().uri("/api/v1_1/customers")
                .body(badDataV1)
                .exchange()
                .expectStatus().isCreated(); // Success (Legacy behavior)

        // 2. Modern API (V2) - Rejects Invalid Email
        CustomerV2 badDataV2 = new CustomerV2(null, "Mr.", "John", "not-an-email");

        client.post().uri("/api/v2_0/customers")
                .body(badDataV2)
                .exchange()
                .expectStatus().isBadRequest() // Fail (Modern behavior)
                .expectBody()
                .jsonPath("$.detail").isEqualTo("Validation failed")
                .jsonPath("$.errors").value(errors ->
                        assertThat(errors.toString()).contains("Invalid email"));

        // 3. Modern API (V2) - Accepts Valid Data with Title
        CustomerV2 validDataV2 = new CustomerV2(null, "Dr.", "Jane Doe", "jane@example.com");

        client.post().uri("/api/v2_0/customers")
                .body(validDataV2)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerV2.class)
                .value(c -> assertThat(c.title()).isEqualTo("Dr."));
    }

}