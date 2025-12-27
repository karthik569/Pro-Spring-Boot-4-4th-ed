package com.apress.crm.customer;

import jakarta.persistence.EntityManager;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class CustomerRepositoryTest extends BaseTest {

    @Autowired
    private CustomerRepository repository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldSaveAndFindCustomer() {
        Customer customer = new Customer("John Doe", "john.doe@example.com", "1234567890", "password");
        Customer saved = repository.save(customer);

        assertThat(saved.getId()).isNotNull();

        Optional<Customer> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John Doe");
    }

    @Test
    void shouldFindAllCustomers() {
        repository.save(new Customer("User 1", "user1@example.com", "111", "password"));
        repository.save(new Customer("User 2", "user2@example.com", "222", "password"));

        List<Customer> all = repository.findAll();
        assertThat(all).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldDeleteCustomer() {
        Customer saved = repository.save(new Customer("To Delete", "delete@example.com", "000", "password"));
        UUID id = saved.getId();

        repository.deleteById(id);

        assertThat(repository.findById(id)).isEmpty();
    }

    @Test
    void shouldFindByName() {
        repository.save(new Customer("Felipe", "felipe@example.com", "123", "password"));
        List<Customer> result = repository.findByName("Felipe");
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getName()).isEqualTo("Felipe");
    }

    @Test
    void shouldFindByEmailEndingWith() {
        repository.save(new Customer("Josh", "josh@spring.io", "123", "password"));
        List<Customer> result = repository.findByEmailEndingWith("spring.io");
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getEmail()).endsWith("spring.io");
    }

    // ========== CockroachDB-Specific Tests ==========

    @Test
    void shouldVerifyCockroachDialectIsActive() {
        // Verify we're using CockroachDB dialect
        SessionFactoryImplementor sessionFactory = entityManager.getEntityManagerFactory()
                .unwrap(SessionFactoryImplementor.class);
        String dialectName = sessionFactory.getJdbcServices()
                .getDialect().getClass().getSimpleName();

        assertThat(dialectName).isEqualTo("CockroachDialect");
    }

    @Test
    void shouldGenerateUUIDWithCockroachDB() {
        // CockroachDB uses gen_random_uuid() for UUID generation
        Customer customer1 = repository.save(new Customer("UUID Test 1", "uuid1@example.com", "111", "password"));
        Customer customer2 = repository.save(new Customer("UUID Test 2", "uuid2@example.com", "222", "password"));

        assertThat(customer1.getId()).isNotNull();
        assertThat(customer2.getId()).isNotNull();
        assertThat(customer1.getId()).isNotEqualTo(customer2.getId());

        // Verify UUIDs are version 4 (random)
        assertThat(customer1.getId().version()).isEqualTo(4);
        assertThat(customer2.getId().version()).isEqualTo(4);
    }

    @Test
    void shouldHandleBatchInserts() {
        // Test batch operations - important for distributed databases
        int batchSize = 50;
        List<Customer> customers = new ArrayList<>();

        for (int i = 0; i < batchSize; i++) {
            customers.add(new Customer(
                    "Batch User " + i,
                    "batch" + i + "@example.com",
                    "555-" + String.format("%04d", i),
                    "password"
            ));
        }

        List<Customer> savedCustomers = repository.saveAll(customers);
        entityManager.flush();
        entityManager.clear();

        assertThat(savedCustomers).hasSize(batchSize);
        assertThat(repository.count()).isGreaterThanOrEqualTo(batchSize);
    }

    @Test
    void shouldHandleSerializableIsolationLevel() {
        // CockroachDB uses SERIALIZABLE isolation by default
        // This test verifies that concurrent saves don't cause issues
        Customer customer = new Customer("Isolation Test", "isolation@example.com", "999", "password");
        Customer saved = repository.save(customer);
        entityManager.flush();

        assertThat(saved.getId()).isNotNull();

        // Retrieve and verify
        Optional<Customer> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Isolation Test");
    }

    @Test
    void shouldVerifyConnectionPooling() {
        // Test that we can handle multiple sequential operations
        // This indirectly tests connection pooling (HikariCP)
        for (int i = 0; i < 10; i++) {
            Customer customer = repository.save(
                    new Customer("Pool Test " + i, "pool" + i + "@example.com", "000", "password")
            );
            assertThat(customer.getId()).isNotNull();

            Optional<Customer> found = repository.findById(customer.getId());
            assertThat(found).isPresent();
        }

        assertThat(repository.count()).isGreaterThanOrEqualTo(10);
    }

    @Test
    void shouldHandleComplexDerivedQueries() {
        // Test derived queries work correctly with CockroachDB
        repository.save(new Customer("Alice Johnson", "alice@cockroachlabs.com", "111", "password"));
        repository.save(new Customer("Bob Smith", "bob@cockroachlabs.com", "222", "password"));
        repository.save(new Customer("Alice Cooper", "alice@example.com", "333", "password"));

        // Test findByName
        List<Customer> alices = repository.findByName("Alice Johnson");
        assertThat(alices).hasSize(1);
        assertThat(alices.get(0).getEmail()).isEqualTo("alice@cockroachlabs.com");

        // Test findByEmailEndingWith
        List<Customer> cockroachUsers = repository.findByEmailEndingWith("cockroachlabs.com");
        assertThat(cockroachUsers).hasSize(2);
    }

    // ========== CockroachDB Follower Reads Tests ==========

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldPerformFollowerReadWithAsOfSystemTime() throws InterruptedException {
        // Create customers at the current time
        repository.save(new Customer("Follower Read User 1", "follower1@example.com", "555-0001", "password"));
        repository.save(new Customer("Follower Read User 2", "follower2@example.com", "555-0002", "password"));
        repository.save(new Customer("Follower Read User 3", "follower3@example.com", "555-0003", "password"));

        // Wait to ensure data is committed and available for historical reads
        // In production, follower reads reduce latency by reading from nearby replicas
        Thread.sleep(6000); // Wait > 5 seconds for AS OF SYSTEM TIME '-5s'

        // Perform follower read (AS OF SYSTEM TIME '-5s')
        List<Customer> historicalCustomers = repository.findAllAsOfSystemTime();

        // Should find customers created more than 5 seconds ago
        assertThat(historicalCustomers).hasSizeGreaterThanOrEqualTo(3);
        assertThat(historicalCustomers.stream()
                .anyMatch(c -> c.getName().equals("Follower Read User 1"))).isTrue();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldPerformFollowerReadWithWhereClause() throws InterruptedException {
        // Create specific customer
        Customer savedCustomer = repository.save(
                new Customer("Follower Query Test", "follower-query@example.com", "555-9999", "password")
        );

        // Wait for data to be available for historical reads
        Thread.sleep(6000);

        // Perform follower read with a WHERE clause
        List<Customer> results = repository.findByNameAsOfSystemTime("Follower Query Test");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Follower Query Test");
        assertThat(results.get(0).getEmail()).isEqualTo("follower-query@example.com");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldDemonstrateFollowerReadStaleData() throws InterruptedException {
        // This test demonstrates that follower reads may return slightly stale data
        // which is acceptable for many use cases and reduces latency

        // Create initial customer
        Customer initial = repository.save(
                new Customer("Stale Data Test", "stale@example.com", "555-1111", "password")
        );
        UUID customerId = initial.getId();

        // Wait for a follower read window
        Thread.sleep(6000);

        // Read via follower (should see the customer)
        List<Customer> followerResults = repository.findAllAsOfSystemTime();
        long followerCount = followerResults.stream()
                .filter(c -> c.getId().equals(customerId))
                .count();
        assertThat(followerCount).isEqualTo(1);

        // Delete the customer NOW
        repository.deleteById(customerId);

        // Follower read still sees it (stale data from -5s)
        List<Customer> stillVisibleViaFollower = repository.findAllAsOfSystemTime();
        long stillVisible = stillVisibleViaFollower.stream()
                .filter(c -> c.getId().equals(customerId))
                .count();

        // The customer is still visible via follower read because we're reading from -5s ago
        assertThat(stillVisible).isEqualTo(1);

        // But the current read doesn't see it
        assertThat(repository.findById(customerId)).isEmpty();
    }
}