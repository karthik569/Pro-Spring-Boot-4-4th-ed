package com.apress.crm.management;

import com.apress.crm.management.model.*;
import com.apress.crm.management.repository.jpa.CustomerRepository;
import com.apress.crm.management.service.ManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
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
	private CacheManager cacheManager;

	private Customer testCustomer;

	@BeforeEach
	void setUp() {
		customerRepository.deleteAll();
		if (cacheManager.getCache("customers") != null) {
			cacheManager.getCache("customers").clear();
		}

		Company company = new Company(null, "Test Company", "Test Industry", "test.com");
		Customer customer = new Customer(null, "Test", "User", "Tester", "test@test.com", "123", null);
		Address address = new Address(null, null, "123 Street", "City", "ST", "12345");
		Communication communication = new Communication(null, null, "email", "test@test.com");

		CustomerDetailsDTO details = managementService.createCustomerWithDetails(customer, company, address, communication);
		testCustomer = details.customer();
	}

	// ========== REST API Tests ==========

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
					assertThat(dto.addresses()).hasSize(1);
					assertThat(dto.communications()).hasSize(1);
				});
	}

	@Test
	void shouldReturnNotFoundForInvalidId() {
		webTestClient.get().uri("/api/v1/management/customers/" + UUID.randomUUID())
				.exchange()
				.expectStatus().isNotFound();
	}

	@Test
	void shouldRetrieveCustomerDetailsWithCompanyAndRelatedData() {
		// Test that customer details include all related entities
		CustomerDetailsDTO details = managementService.getCustomerDetails(testCustomer.getCustomerId());

		assertThat(details).isNotNull();
		assertThat(details.customer()).isNotNull();
		assertThat(details.company()).isNotNull();
		assertThat(details.company().getName()).isEqualTo("Test Company");
		assertThat(details.addresses()).hasSize(1);
		assertThat(details.addresses().get(0).getCity()).isEqualTo("City");
		assertThat(details.communications()).hasSize(1);
		assertThat(details.communications().get(0).getType()).isEqualTo("email");
	}

	// ========== Service Layer Integration Tests ==========

	@Test
	void shouldCreateCustomerWithAllDetails() {
		Company newCompany = new Company(null, "New Company", "Technology", "newcompany.com");
		Customer newCustomer = new Customer(null, "New", "Customer", "Engineer", "new@example.com", "555-1234", null);
		Address newAddress = new Address(null, null, "456 New St", "NewCity", "NC", "67890");
		Communication newCommunication = new Communication(null, null, "phone", "555-1234");

		CustomerDetailsDTO result = managementService.createCustomerWithDetails(newCustomer, newCompany, newAddress, newCommunication);

		assertThat(result).isNotNull();
		assertThat(result.customer().getCustomerId()).isNotNull();
		assertThat(result.company().getCompanyId()).isNotNull();
		assertThat(result.customer().getCompany()).isNotNull();
		assertThat(result.customer().getCompany().getName()).isEqualTo("New Company");
	}

	@Test
	void shouldFindAllCustomers() {
		// Create additional customers
		for (int i = 0; i < 3; i++) {
			Company company = new Company(null, "Company " + i, "Industry", "company" + i + ".com");
			Customer customer = new Customer(null, "User" + i, "Last" + i, "Job", "user" + i + "@example.com", "555-000" + i, null);
			Address address = new Address(null, null, i + " Street", "City", "ST", "1000" + i);
			Communication communication = new Communication(null, null, "email", "user" + i + "@example.com");

			managementService.createCustomerWithDetails(customer, company, address, communication);
		}

		List<Customer> all = customerRepository.findAll();
		assertThat(all).hasSizeGreaterThanOrEqualTo(4); // 1 from setUp + 3 created here
	}

	// ========== Pagination Tests ==========

	@Test
	void shouldRetrieveCustomersByLastNameWithPagination() {
		Page<Customer> result = managementService.getCustomersByLastName("User", 0, 10);
		assertThat(result).isNotEmpty();
		assertThat(result.getContent()).extracting(Customer::getCustomerId).contains(testCustomer.getCustomerId());
	}

	@Test
	void shouldHandlePaginationWithSorting() {
		// Create multiple customers with same last name
		for (int i = 0; i < 5; i++) {
			Company company = new Company(null, "Page Co " + i, "Tech", "page" + i + ".com");
			Customer customer = new Customer(null, "Alpha" + i, "PageTest", "Dev", "page" + i + "@example.com", "555-" + i, null);
			Address address = new Address(null, null, i + " Page St", "City", "ST", "2000" + i);
			Communication communication = new Communication(null, null, "email", "page" + i + "@example.com");

			managementService.createCustomerWithDetails(customer, company, address, communication);
		}

		// Test first page
		Page<Customer> page1 = managementService.getCustomersByLastName("PageTest", 0, 3);
		assertThat(page1.getContent()).hasSize(3);
		assertThat(page1.getTotalElements()).isGreaterThanOrEqualTo(5);
		assertThat(page1.hasNext()).isTrue();

		// Test second page
		Page<Customer> page2 = managementService.getCustomersByLastName("PageTest", 1, 3);
		assertThat(page2.getContent()).hasSizeGreaterThan(0);
	}

	// ========== Caching Integration Tests ==========

	@Test
	void shouldVerifyCaching() {
		UUID customerId = testCustomer.getCustomerId();

		// First call - should hit the database
		managementService.getCustomerDetails(customerId);
		assertThat(isCached(customerId)).isTrue();

		// Second call - should hit the cache
		CustomerDetailsDTO cached = managementService.getCustomerDetails(customerId);
		assertThat(cached).isNotNull();

		// Verify it's still cached
		assertThat(isCached(customerId)).isTrue();
	}

	// ========== Validation Tests ==========

	@Test
	void shouldRejectInvalidCustomer_MissingRequiredFields() {
		// Create customer with missing required fields (empty name)
		Company company = new Company(null, "Valid Company", "Tech", "valid.com");
		Customer invalidCustomer = new Customer(null, "", "NoFirstName", "Job", "invalid@example.com", "123", null);
		Address address = new Address(null, null, "Street", "City", "ST", "12345");
		Communication communication = new Communication(null, null, "email", "invalid@example.com");

		try {
			managementService.createCustomerWithDetails(invalidCustomer, company, address, communication);
			assertThat(false).as("Should have thrown validation exception").isTrue();
		} catch (Exception e) {
			// Expected validation exception
			assertThat(e).isNotNull();
		}
	}

	@Test
	void shouldRejectInvalidCustomer_InvalidEmail() {
		// Create customer with invalid email format
		Company company = new Company(null, "Valid Company", "Tech", "valid.com");
		Customer invalidCustomer = new Customer(null, "Invalid", "Email", "Job", "not-an-email", "123", null);
		Address address = new Address(null, null, "Street", "City", "ST", "12345");
		Communication communication = new Communication(null, null, "email", "not-an-email");

		try {
			managementService.createCustomerWithDetails(invalidCustomer, company, address, communication);
			assertThat(false).as("Should have thrown validation exception").isTrue();
		} catch (Exception e) {
			// Expected validation exception
			assertThat(e).isNotNull();
		}
	}

	// ========== CockroachDB Integration Tests ==========

	@Test
	void shouldPersistDataInCockroachDB() {
		// This test verifies that data persists in CockroachDB across cache and transaction boundaries
		UUID customerId = testCustomer.getCustomerId();

		// Clear cache to force DB read
		cacheManager.getCache("customers").clear();

		// Retrieve from DB (not cache)
		Customer fromDb = customerRepository.findById(customerId).orElse(null);
		assertThat(fromDb).isNotNull();
		assertThat(fromDb.getFirstName()).isEqualTo("Test");
		assertThat(fromDb.getLastName()).isEqualTo("User");

		// Verify via service layer
		CustomerDetailsDTO details = managementService.getCustomerDetails(customerId);
		assertThat(details).isNotNull();
		assertThat(details.customer().getCustomerId()).isEqualTo(customerId);
		assertThat(details.company()).isNotNull();
	}

	// ========== Helper Methods ==========

	private boolean isCached(UUID id) {
		return cacheManager.getCache("customers") != null &&
				cacheManager.getCache("customers").get(id) != null;
	}
}
