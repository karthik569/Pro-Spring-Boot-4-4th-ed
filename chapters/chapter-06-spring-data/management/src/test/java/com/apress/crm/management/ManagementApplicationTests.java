package com.apress.crm.management;

import com.apress.crm.management.model.Customer;
import com.apress.crm.management.model.CustomerDetailsDTO;
import com.apress.crm.management.repository.CustomerRepository;
import com.apress.crm.management.service.ManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.data.domain.Page;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ManagementApplicationTests {

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private ManagementService managementService;

	@Test
	void shouldRetrieveCustomerDetails() {
		// Verify we have data populated by ManagementConfiguration
		Iterable<Customer> customers = customerRepository.findAll();
		assertThat(customers).isNotEmpty();

		// Pick one customer to test
		Customer customer = customers.iterator().next();

		webTestClient.get().uri("/api/v1/management/customers/" + customer.getCustomerId())
				.exchange()
				.expectStatus().isOk()
				.expectBody(CustomerDetailsDTO.class)
				.value(dto -> {
					assertThat(dto).isNotNull();
					assertThat(dto.customer()).isNotNull();
					assertThat(dto.customer().getCustomerId()).isEqualTo(customer.getCustomerId());
					assertThat(dto.customer().getFirstName()).isEqualTo(customer.getFirstName());
					assertThat(dto.customer().getLastName()).isEqualTo(customer.getLastName());
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

	@Test
	void shouldRetrieveCustomersByCompanyWithPagination() {
		// Pick one customer to find their company
		Customer customer = customerRepository.findAll().iterator().next();
		assertThat(customer.getCompany()).isNotNull();

		Page<Customer> result = managementService.getCustomersByCompany(customer.getCompany().getCompanyId(), 0, 10);

		assertThat(result).isNotEmpty();
		assertThat(result.getContent()).contains(customer);
		// Verify sort (it was set to lastName ascending in service)
		if (result.getContent().size() > 1) {
			assertThat(result.getContent().get(0).getLastName())
					.isLessThanOrEqualTo(result.getContent().get(1).getLastName());
		}
	}

}
