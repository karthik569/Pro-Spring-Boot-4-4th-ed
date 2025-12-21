package com.apress.crm.management;

import com.apress.crm.management.model.Customer;
import com.apress.crm.management.model.CustomerDetailsDTO;
import com.apress.crm.management.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ManagementApplicationTests {

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private CustomerRepository customerRepository;

	@Test
	void shouldRetrieveCustomerDetails() {
		// Verify we have data populated by ManagementConfiguration
		Iterable<Customer> customers = customerRepository.findAll();
		assertThat(customers).isNotEmpty();

		// Pick one customer to test
		Customer customer = customers.iterator().next();

		webTestClient.get().uri("/api/v1/management/customers/" + customer.customerId())
				.exchange()
				.expectStatus().isOk()
				.expectBody(CustomerDetailsDTO.class)
				.value(dto -> {
					assertThat(dto).isNotNull();
					assertThat(dto.customer()).isNotNull();
					assertThat(dto.customer().customerId()).isEqualTo(customer.customerId());
					assertThat(dto.customer().firstName()).isEqualTo(customer.firstName());
					assertThat(dto.customer().lastName()).isEqualTo(customer.lastName());
					assertThat(dto.company()).isNotNull();
					assertThat(dto.addresses()).isNotEmpty();
					assertThat(dto.communications()).isNotEmpty();
				});
	}

	@Test
	void shouldReturnNotFoundForInvalidId() {
		webTestClient.get().uri("/api/v1/management/customers/" + java.util.UUID.randomUUID())
				.exchange()
				.expectStatus().isNotFound();
	}

}
