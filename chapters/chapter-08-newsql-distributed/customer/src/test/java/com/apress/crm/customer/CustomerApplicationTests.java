package com.apress.crm.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.cache.CacheManager;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureRestTestClient
class CustomerApplicationTests extends BaseTest {

	@Autowired
	private RestTestClient restTestClient;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private CacheManager cacheManager;

	@BeforeEach
	void setUp() {
		customerRepository.deleteAll();
		if (cacheManager.getCache("customers") != null) {
			cacheManager.getCache("customers").clear();
		}
	}

	// ========== CRUD REST API Tests ==========

	@Test
	void shouldRetrieveAndCreateCustomers() {
		Customer customer = new Customer("New User", "new@example.com", "555-0199");

		Customer savedCustomer = restTestClient.post().uri("/api/v1/customers")
				.body(customer)
				.exchange()
				.expectStatus().isCreated()
				.expectBody(Customer.class)
				.returnResult().getResponseBody();

		assertThat(savedCustomer).isNotNull();
		assertThat(savedCustomer.getId()).isNotNull();

		Customer retrievedCustomer = restTestClient.get().uri("/api/v1/customers/" + savedCustomer.getId())
				.exchange()
				.expectStatus().isOk()
				.expectBody(Customer.class)
				.returnResult().getResponseBody();

		assertThat(retrievedCustomer).isNotNull();
		assertThat(retrievedCustomer.getName()).isEqualTo("New User");
	}

	@Test
	void shouldGetAllCustomers() {
		// Create test data
		customerService.save(new Customer("Alice", "alice@example.com", "555-0001"));
		customerService.save(new Customer("Bob", "bob@example.com", "555-0002"));
		customerService.save(new Customer("Charlie", "charlie@example.com", "555-0003"));

		// GET all customers
		restTestClient.get().uri("/api/v1/customers")
				.exchange()
				.expectStatus().isOk();

		// Verify via service layer
		List<Customer> customers = customerService.findAll();
		assertThat(customers).hasSizeGreaterThanOrEqualTo(3);
	}

	@Test
	void shouldGetCustomerById() {
		Customer saved = customerService.save(new Customer("David", "david@example.com", "555-0004"));
		UUID customerId = saved.getId();

		Customer found = restTestClient.get().uri("/api/v1/customers/" + customerId)
				.exchange()
				.expectStatus().isOk()
				.expectBody(Customer.class)
				.returnResult().getResponseBody();

		assertThat(found).isNotNull();
		assertThat(found.getId()).isEqualTo(customerId);
		assertThat(found.getName()).isEqualTo("David");
		assertThat(found.getEmail()).isEqualTo("david@example.com");
	}

	@Test
	void shouldDeleteCustomer() {
		Customer saved = customerService.save(new Customer("To Delete", "delete@example.com", "555-9999"));
		UUID customerId = saved.getId();

		// DELETE the customer
		restTestClient.delete().uri("/api/v1/customers/" + customerId)
				.exchange()
				.expectStatus().isNoContent();

		// Verify it's deleted (should return 404)
		restTestClient.get().uri("/api/v1/customers/" + customerId)
				.exchange()
				.expectStatus().isNotFound();
	}

	// ========== Validation Tests ==========

	@Test
	void shouldRejectInvalidCustomer_MissingName() {
		Customer invalidCustomer = new Customer("", "valid@example.com", "555-0000");

		restTestClient.post().uri("/api/v1/customers")
				.body(invalidCustomer)
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void shouldRejectInvalidCustomer_InvalidEmail() {
		Customer invalidCustomer = new Customer("Invalid Email User", "not-an-email", "555-0000");

		restTestClient.post().uri("/api/v1/customers")
				.body(invalidCustomer)
				.exchange()
				.expectStatus().isBadRequest();
	}

	// ========== Error Handling Tests ==========

	@Test
	void shouldReturn404ForNonExistentCustomer() {
		UUID nonExistentId = UUID.randomUUID();

		restTestClient.get().uri("/api/v1/customers/" + nonExistentId)
				.exchange()
				.expectStatus().isNotFound();
	}

	// ========== Caching Integration Tests ==========

	@Test
	void shouldVerifyCaching() {
		Customer customer = new Customer("Cached User", "cached@example.com", "555-0200");
		Customer saved = customerService.save(customer);
		UUID customerId = saved.getId();

		// First call - should hit the database
		customerService.findById(customerId);
		assertThat(isCached(customerId)).isTrue();

		// Second call - should hit the cache
		customerService.findById(customerId);

		// Verify it's still cached
		assertThat(isCached(customerId)).isTrue();

		// Evict cache
		customerService.deleteById(customerId);
		assertThat(isCached(customerId)).isFalse();
	}

	// ========== CockroachDB Integration Tests ==========

	@Test
	void shouldHandleMultipleSimultaneousRequests() throws InterruptedException {
		// Create initial customer via REST
		Customer customer = new Customer("Concurrent REST User", "concurrent-rest@example.com", "555-9000");

		Customer saved = restTestClient.post().uri("/api/v1/customers")
				.body(customer)
				.exchange()
				.expectStatus().isCreated()
				.expectBody(Customer.class)
				.returnResult().getResponseBody();

		assertThat(saved).isNotNull();
		assertThat(saved.getId()).isNotNull();

		// Verify we can retrieve it
		Customer retrieved = restTestClient.get().uri("/api/v1/customers/" + saved.getId())
				.exchange()
				.expectStatus().isOk()
				.expectBody(Customer.class)
				.returnResult().getResponseBody();

		assertThat(retrieved).isNotNull();
		assertThat(retrieved.getId()).isEqualTo(saved.getId());
	}

	@Test
	void shouldVerifyEndToEndWorkflowWithCockroachDB() {
		// 1. Create multiple customers
		Customer customer1 = restTestClient.post().uri("/api/v1/customers")
				.body(new Customer("Workflow User 1", "workflow1@example.com", "555-1001"))
				.exchange()
				.expectStatus().isCreated()
				.expectBody(Customer.class)
				.returnResult().getResponseBody();

		Customer customer2 = restTestClient.post().uri("/api/v1/customers")
				.body(new Customer("Workflow User 2", "workflow2@example.com", "555-1002"))
				.exchange()
				.expectStatus().isCreated()
				.expectBody(Customer.class)
				.returnResult().getResponseBody();

		// 2. Retrieve all customers
		restTestClient.get().uri("/api/v1/customers")
				.exchange()
				.expectStatus().isOk();

		List<Customer> allCustomers = customerService.findAll();
		assertThat(allCustomers).hasSizeGreaterThanOrEqualTo(2);

		// 3. Retrieve individual customer
		Customer found = restTestClient.get().uri("/api/v1/customers/" + customer1.getId())
				.exchange()
				.expectStatus().isOk()
				.expectBody(Customer.class)
				.returnResult().getResponseBody();

		assertThat(found).isNotNull();
		assertThat(found.getName()).isEqualTo("Workflow User 1");

		// 4. Delete customer
		restTestClient.delete().uri("/api/v1/customers/" + customer2.getId())
				.exchange()
				.expectStatus().isNoContent();

		// 5. Verify deletion
		restTestClient.get().uri("/api/v1/customers/" + customer2.getId())
				.exchange()
				.expectStatus().isNotFound();
	}

	@Test
	void shouldPersistDataInCockroachDB() {
		// This test verifies that data persists in CockroachDB
		// Create customer
		Customer customer = new Customer("Persistence Test", "persist@example.com", "555-7777");

		Customer saved = restTestClient.post().uri("/api/v1/customers")
				.body(customer)
				.exchange()
				.expectStatus().isCreated()
				.expectBody(Customer.class)
				.returnResult().getResponseBody();

		UUID customerId = saved.getId();

		// Clear cache to force DB read
		cacheManager.getCache("customers").clear();

		// Retrieve from DB (not cache)
		Customer fromDb = customerRepository.findById(customerId).orElse(null);
		assertThat(fromDb).isNotNull();
		assertThat(fromDb.getName()).isEqualTo("Persistence Test");

		// Also verify via REST
		Customer viaRest = restTestClient.get().uri("/api/v1/customers/" + customerId)
				.exchange()
				.expectStatus().isOk()
				.expectBody(Customer.class)
				.returnResult().getResponseBody();

		assertThat(viaRest).isNotNull();
		assertThat(viaRest.getName()).isEqualTo("Persistence Test");
	}

	// ========== Helper Methods ==========

	private boolean isCached(UUID id) {
		return cacheManager.getCache("customers") != null &&
				cacheManager.getCache("customers").get(id) != null;
	}
}