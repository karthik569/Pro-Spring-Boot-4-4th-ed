package com.apress.crm.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.cache.CacheManager;
import org.springframework.test.web.servlet.client.RestTestClient;

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

	@Test
	void shouldRetrieveAndCreateCustomers() {
		Customer customer = new Customer("New User", "new@example.com", "555-0199");
		
		Customer savedCustomer = restTestClient.post().uri("/api/v1/customers")
				.bodyValue(customer)
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

	private boolean isCached(UUID id) {
		return cacheManager.getCache("customers") != null && cacheManager.getCache("customers").get(id) != null;
	}
}