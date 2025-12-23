package com.apress.crm.management;

import com.apress.crm.management.model.*;
import com.apress.crm.management.repository.jpa.CustomerRepository;
import com.apress.crm.management.service.ManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureWebTestClient
class ManagementApplicationTests extends BaseTest {

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private ManagementService managementService;

	@Autowired
	private ReactiveRedisOperations<String, CustomerSession> sessionOps;

	private Customer testCustomer;

	@BeforeEach
	void setUp() {
		customerRepository.deleteAll();
		
		Company company = new Company(null, "Test Company", "Test Industry", "test.com");
		Customer customer = new Customer(null, "Test", "User", "Tester", "test@test.com", "123", null);
		Address address = new Address(null, null, "123 Street", "City", "ST", "12345");
		Communication communication = new Communication(null, null, "email", "test@test.com");
		
		CustomerDetailsDTO details = managementService.createCustomerWithDetails(customer, company, address, communication);
		testCustomer = details.customer();
	}

	@Test
	void shouldRetrieveCustomerDetails() {
		webTestClient.get().uri("/api/v1/management/customers/" + testCustomer.getCustomerId())
				.exchange()
				.expectStatus().isOk()
				.expectBody(CustomerDetailsDTO.class)
				.value(dto -> {
					assertThat(dto).isNotNull();
					assertThat(dto.customer().getCustomerId()).isEqualTo(testCustomer.getCustomerId());
					assertThat(dto.company()).isNotNull();
				});
	}

	@Test
	void shouldReturnNotFoundForInvalidId() {
		webTestClient.get().uri("/api/v1/management/customers/" + UUID.randomUUID())
				.exchange()
				.expectStatus().isNotFound();
	}

	@Test
	void shouldRetrieveCustomersByLastNameWithPagination() {
		Page<Customer> result = managementService.getCustomersByLastName("User", 0, 10);
		assertThat(result).isNotEmpty();
		assertThat(result.getContent()).extracting(Customer::getCustomerId).contains(testCustomer.getCustomerId());
	}

	@Test
	void shouldVerifyRedisSession() {
		UUID customerId = testCustomer.getCustomerId();
		var verifySession = managementService.startSession(customerId)
				.then(managementService.getSession(customerId));
		
		StepVerifier.create(verifySession)
				.expectNextMatches(session -> session.lastAction().equals("Login"))
				.verifyComplete();
	}
}