package com.apress.crm.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = "spring.datasource.url=jdbc:h2:mem:api-test-db") // (1)
@AutoConfigureRestTestClient
class CustomerApplicationTests {

	@Autowired
	private RestTestClient client;

	@Test
	void shouldRetrieveAndCreateCustomers() {
		// 1. Verify initial data (loaded by CustomerConfiguration)
		client.get().uri("/api/v1/customers")
				.exchange()
				.expectStatus().isOk()
				.expectBody(Customer[].class)
				.value(customers -> assertThat(customers).hasSize(3));

		// 2. Create a new customer via API
		Customer newCustomer = new Customer("API User", "api@test.com", "555-9999");

		client.post().uri("/api/v1/customers")
				.contentType(MediaType.APPLICATION_JSON)
				.body(newCustomer)
				.exchange()
				.expectStatus().isCreated()
				.expectBody(Customer.class)
				.value(saved -> {
					assertThat(saved.id()).isNotNull();
					assertThat(saved.name()).isEqualTo("API User");
				});

		// 3. Verify total count increased
		client.get().uri("/api/v1/customers")
				.exchange()
				.expectStatus().isOk()
				.expectBody(Customer[].class)
				.value(customers -> assertThat(customers).hasSize(4));
	}

}