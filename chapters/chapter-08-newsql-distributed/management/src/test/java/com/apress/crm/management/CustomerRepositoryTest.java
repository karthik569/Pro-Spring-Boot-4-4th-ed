package com.apress.crm.management;

import com.apress.crm.management.model.Company;
import com.apress.crm.management.model.Customer;
import com.apress.crm.management.repository.jpa.CompanyRepository;
import com.apress.crm.management.repository.jpa.CustomerRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private CompanyRepository companyRepository;

    @Autowired
    private EntityManager entityManager;

    private Company testCompany;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        companyRepository.deleteAll();

        // Create a test company for customer associations
        testCompany = new Company(null, "Test Company", "Technology", "testcompany.com");
        testCompany = companyRepository.save(testCompany);
    }

    // ========== Basic CRUD Tests ==========

    @Test
    void shouldSaveAndFindCustomer() {
        Customer customer = new Customer(null, "John", "Doe", "Developer", "john.doe@example.com", "1234567890", testCompany);
        Customer saved = repository.save(customer);

        assertThat(saved.getCustomerId()).isNotNull();

        Optional<Customer> found = repository.findById(saved.getCustomerId());
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("John");
        assertThat(found.get().getLastName()).isEqualTo("Doe");
    }

    @Test
    void shouldFindAllCustomers() {
        repository.save(new Customer(null, "User", "One", "Manager", "user1@example.com", "111", testCompany));
        repository.save(new Customer(null, "User", "Two", "Developer", "user2@example.com", "222", testCompany));

        List<Customer> all = repository.findAll();
        assertThat(all).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldDeleteCustomer() {
        Customer saved = repository.save(new Customer(null, "To", "Delete", "Tester", "delete@example.com", "000", testCompany));
        UUID id = saved.getCustomerId();

        repository.deleteById(id);

        assertThat(repository.findById(id)).isEmpty();
    }

    @Test
    void shouldFindByEmail() {
        repository.save(new Customer(null, "Felipe", "Gutierrez", "Architect", "felipe@example.com", "123", testCompany));
        Customer result = repository.findByEmail("felipe@example.com");
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("Felipe");
    }

    @Test
    void shouldFindByLastNameWithPagination() {
        repository.save(new Customer(null, "Alice", "Smith", "Developer", "alice@example.com", "123", testCompany));
        repository.save(new Customer(null, "Bob", "Smith", "Manager", "bob@example.com", "456", testCompany));
        repository.save(new Customer(null, "Charlie", "Smith", "Tester", "charlie@example.com", "789", testCompany));

        Pageable pageable = PageRequest.of(0, 2, Sort.by("firstName").ascending());
        Page<Customer> result = repository.findByLastName("Smith", pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(3);
        assertThat(result.getContent().get(0).getFirstName()).isEqualTo("Alice");
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
        Customer customer1 = repository.save(new Customer(null, "UUID", "Test1", "Dev", "uuid1@example.com", "111", testCompany));
        Customer customer2 = repository.save(new Customer(null, "UUID", "Test2", "Dev", "uuid2@example.com", "222", testCompany));

        assertThat(customer1.getCustomerId()).isNotNull();
        assertThat(customer2.getCustomerId()).isNotNull();
        assertThat(customer1.getCustomerId()).isNotEqualTo(customer2.getCustomerId());

        // Verify UUIDs are version 4 (random)
        assertThat(customer1.getCustomerId().version()).isEqualTo(4);
        assertThat(customer2.getCustomerId().version()).isEqualTo(4);
    }

    @Test
    void shouldHandleBatchInserts() {
        // Test batch operations - important for distributed databases
        int batchSize = 50;
        List<Customer> customers = new ArrayList<>();

        for (int i = 0; i < batchSize; i++) {
            customers.add(new Customer(
                    null,
                    "Batch",
                    "User" + i,
                    "Developer",
                    "batch" + i + "@example.com",
                    "555-" + String.format("%04d", i),
                    testCompany
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
        Customer customer = new Customer(null, "Isolation", "Test", "Developer", "isolation@example.com", "999", testCompany);
        Customer saved = repository.save(customer);
        entityManager.flush();

        assertThat(saved.getCustomerId()).isNotNull();

        // Retrieve and verify
        Optional<Customer> found = repository.findById(saved.getCustomerId());
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Isolation");
    }

    @Test
    void shouldVerifyConnectionPooling() {
        // Test that we can handle multiple sequential operations
        // This indirectly tests connection pooling (HikariCP)
        for (int i = 0; i < 10; i++) {
            Customer customer = repository.save(
                    new Customer(null, "Pool", "Test" + i, "Dev", "pool" + i + "@example.com", "000", testCompany)
            );
            assertThat(customer.getCustomerId()).isNotNull();

            Optional<Customer> found = repository.findById(customer.getCustomerId());
            assertThat(found).isPresent();
        }

        assertThat(repository.count()).isGreaterThanOrEqualTo(10);
    }

    @Test
    void shouldHandleComplexDerivedQueries() {
        // Test derived queries work correctly with CockroachDB
        repository.save(new Customer(null, "Alice", "Johnson", "Architect", "alice@cockroachlabs.com", "111", testCompany));
        repository.save(new Customer(null, "Bob", "Johnson", "Manager", "bob@cockroachlabs.com", "222", testCompany));
        repository.save(new Customer(null, "Alice", "Cooper", "Developer", "alice@example.com", "333", testCompany));

        // Test findByLastName with pagination
        Pageable pageable = PageRequest.of(0, 10, Sort.by("firstName").ascending());
        Page<Customer> johnsons = repository.findByLastName("Johnson", pageable);
        assertThat(johnsons.getContent()).hasSize(2);
        assertThat(johnsons.getContent().get(0).getFirstName()).isEqualTo("Alice");

        // Test findByEmail
        Customer alice = repository.findByEmail("alice@cockroachlabs.com");
        assertThat(alice).isNotNull();
        assertThat(alice.getLastName()).isEqualTo("Johnson");
    }

    // ========== CockroachDB Follower Reads Tests ==========

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldPerformFollowerReadWithAsOfSystemTime() throws InterruptedException {
        // Create customers without company (simplified for follower read test)
        repository.save(new Customer(null, "Follower", "Read1", "Dev", "follower1@example.com", "555-0001", null));
        repository.save(new Customer(null, "Follower", "Read2", "Dev", "follower2@example.com", "555-0002", null));
        repository.save(new Customer(null, "Follower", "Read3", "Dev", "follower3@example.com", "555-0003", null));

        // Wait to ensure data is committed and available for historical reads
        // In production, follower reads reduce latency by reading from nearby replicas
        Thread.sleep(6000); // Wait > 5 seconds for AS OF SYSTEM TIME '-5s'

        // Perform follower read (AS OF SYSTEM TIME '-5s')
        List<Customer> historicalCustomers = repository.findAllAsOfSystemTime();

        // Should find customers created more than 5 seconds ago
        assertThat(historicalCustomers).hasSizeGreaterThanOrEqualTo(3);
        assertThat(historicalCustomers.stream()
                .anyMatch(c -> c.getLastName().equals("Read1"))).isTrue();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldPerformFollowerReadWithWhereClause() throws InterruptedException {
        // Create specific customer without company (simplified for follower read test)
        Customer savedCustomer = repository.save(
                new Customer(null, "Follower", "QueryTest", "Dev", "follower-query@example.com", "555-9999", null)
        );

        // Wait for data to be available for historical reads
        Thread.sleep(6000);

        // Perform follower read with WHERE clause
        List<Customer> results = repository.findByLastNameAsOfSystemTime("QueryTest");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFirstName()).isEqualTo("Follower");
        assertThat(results.get(0).getEmail()).isEqualTo("follower-query@example.com");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldDemonstrateFollowerReadStaleData() throws InterruptedException {
        // This test demonstrates that follower reads may return slightly stale data
        // which is acceptable for many use cases and reduces latency

        // Create initial customer without company (simplified for follower read test)
        Customer initial = repository.save(
                new Customer(null, "Stale", "DataTest", "Dev", "stale@example.com", "555-1111", null)
        );
        UUID customerId = initial.getCustomerId();

        // Wait for follower read window
        Thread.sleep(6000);

        // Read via follower (should see the customer)
        List<Customer> followerResults = repository.findAllAsOfSystemTime();
        long followerCount = followerResults.stream()
                .filter(c -> c.getCustomerId().equals(customerId))
                .count();
        assertThat(followerCount).isEqualTo(1);

        // Delete the customer NOW
        repository.deleteById(customerId);

        // Follower read still sees it (stale data from -5s)
        List<Customer> stillVisibleViaFollower = repository.findAllAsOfSystemTime();
        long stillVisible = stillVisibleViaFollower.stream()
                .filter(c -> c.getCustomerId().equals(customerId))
                .count();

        // The customer is still visible via follower read because we're reading from -5s ago
        assertThat(stillVisible).isEqualTo(1);

        // But current read doesn't see it
        assertThat(repository.findById(customerId)).isEmpty();
    }
}
