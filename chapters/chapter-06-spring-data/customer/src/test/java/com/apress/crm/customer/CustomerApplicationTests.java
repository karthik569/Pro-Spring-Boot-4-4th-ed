package com.apress.crm.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class CustomerApplicationTests {

	@Autowired
	private RestTestClient restTestClient;

	@Test
	void shouldRetrieveAndCreateCustomers() {
		// Verify initial data (3 customers from CustomerConfiguration)
		restTestClient.get().uri("/api/v1/customers")
				.exchange()
				.expectStatus().isOk()
				.expectBody(Customer[].class)
				.value(customers -> assertThat(customers).hasSize(3));

		// Create a new customer
		Customer customer = new Customer("New User", "new@example.com", "555-0199");

		restTestClient.post().uri("/api/v1/customers")
				.contentType(MediaType.APPLICATION_JSON)
				.body(customer)
				.exchange()
				.expectStatus().isCreated()
				.expectBody(Customer.class)
				.value(savedCustomer -> {
					assertThat(savedCustomer.id()).isNotNull();
					assertThat(savedCustomer.name()).isEqualTo(customer.name());
				});

		// Verify list now has 4 customers
		restTestClient.get().uri("/api/v1/customers")
				.exchange()
				.expectStatus().isOk()
				.expectBody(Customer[].class)
				.value(customers -> assertThat(customers).hasSize(4));
	}
}