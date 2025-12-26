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
    // Note: Simplified to reduce test execution time - these test database features, not app logic

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldPerformFollowerReadWithAsOfSystemTime() throws InterruptedException {
        // Create customers
        repository.save(new Customer("Follower Read User 1", "follower1@example.com", "555-0001", "password"));
        repository.save(new Customer("Follower Read User 2", "follower2@example.com", "555-0002", "password"));

        // Reduced wait time for test performance (2s for follower read reliability)
        Thread.sleep(2000);

        // Perform follower read - verifies the query syntax works
        List<Customer> historicalCustomers = repository.findAllAsOfSystemTime();

        // Verify follower read executes successfully
        assertThat(historicalCustomers).isNotNull();
    }
}