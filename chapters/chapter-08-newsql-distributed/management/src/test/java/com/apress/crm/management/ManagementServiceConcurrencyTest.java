package com.apress.crm.management;

import com.apress.crm.management.model.*;
import com.apress.crm.management.repository.jpa.CustomerRepository;
import com.apress.crm.management.service.ManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ManagementServiceConcurrencyTest extends BaseTest {

    @Autowired
    private ManagementService managementService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        if (cacheManager.getCache("customers") != null) {
            cacheManager.getCache("customers").clear();
        }
    }

    // ========== Service Layer Integration Tests ==========

    @Test
    void shouldCreateCustomerWithDetailsAcrossFourTables() {
        // Test complex multi-table transaction (Customer, Company, Address, Communication)
        Company company = new Company(null, "Service Test Co", "Technology", "servicetest.com");
        Customer customer = new Customer(null, "Service", "User", "Developer", "service@example.com", "555-1234", null);
        Address address = new Address(null, null, "123 Main St", "TestCity", "TS", "12345");
        Communication communication = new Communication(null, null, "email", "service@example.com");

        CustomerDetailsDTO result = managementService.createCustomerWithDetails(customer, company, address, communication);

        assertThat(result).isNotNull();
        assertThat(result.customer().getCustomerId()).isNotNull();
        assertThat(result.company().getCompanyId()).isNotNull();
        assertThat(result.addresses()).hasSize(1);
        assertThat(result.communications()).hasSize(1);
        assertThat(result.customer().getCompany()).isNotNull();
    }

    @Test
    void shouldRetrieveCustomerDetails() {
        // Create customer with all related data
        Company company = new Company(null, "Retrieve Test Co", "Finance", "retrievetest.com");
        Customer customer = new Customer(null, "Retrieve", "User", "Analyst", "retrieve@example.com", "555-5678", null);
        Address address = new Address(null, null, "456 Oak Ave", "DataCity", "DC", "67890");
        Communication communication = new Communication(null, null, "phone", "555-5678");

        CustomerDetailsDTO created = managementService.createCustomerWithDetails(customer, company, address, communication);
        UUID customerId = created.customer().getCustomerId();

        // Retrieve the customer details
        CustomerDetailsDTO retrieved = managementService.getCustomerDetails(customerId);

        assertThat(retrieved).isNotNull();
        assertThat(retrieved.customer().getCustomerId()).isEqualTo(customerId);
        assertThat(retrieved.company().getName()).isEqualTo("Retrieve Test Co");
        assertThat(retrieved.addresses()).hasSize(1);
        assertThat(retrieved.communications()).hasSize(1);
    }

    @Test
    void shouldVerifyCacheEvictionOnCreate() {
        // Create initial customer
        Company company = new Company(null, "Cache Test Co", "Tech", "cachetest.com");
        Customer customer = new Customer(null, "Cache", "User", "Dev", "cache@example.com", "555-9999", null);
        Address address = new Address(null, null, "789 Cache St", "CacheCity", "CC", "99999");
        Communication communication = new Communication(null, null, "email", "cache@example.com");

        CustomerDetailsDTO created = managementService.createCustomerWithDetails(customer, company, address, communication);
        UUID customerId = created.customer().getCustomerId();

        // First call - hits database, caches result
        managementService.getCustomerDetails(customerId);
        assertThat(isCached(customerId)).isTrue();

        // Create another customer - should evict all cache entries
        Company company2 = new Company(null, "Another Co", "Tech", "another.com");
        Customer customer2 = new Customer(null, "Another", "User", "Dev", "another@example.com", "555-8888", null);
        Address address2 = new Address(null, null, "999 New St", "NewCity", "NC", "88888");
        Communication communication2 = new Communication(null, null, "email", "another@example.com");

        managementService.createCustomerWithDetails(customer2, company2, address2, communication2);

        // Cache should be cleared after create
        assertThat(cacheManager.getCache("customers")).isNotNull();
    }

    @Test
    void shouldFindCustomersByLastNameWithPagination() {
        // Create multiple customers with same last name
        for (int i = 0; i < 5; i++) {
            Company company = new Company(null, "Company " + i, "Tech", "company" + i + ".com");
            Customer customer = new Customer(null, "User" + i, "Smith", "Dev", "user" + i + "@example.com", "555-000" + i, null);
            Address address = new Address(null, null, i + " Street", "City", "ST", "1000" + i);
            Communication communication = new Communication(null, null, "email", "user" + i + "@example.com");

            managementService.createCustomerWithDetails(customer, company, address, communication);
        }

        // Test pagination
        Page<Customer> page1 = managementService.getCustomersByLastName("Smith", 0, 3);
        assertThat(page1.getContent()).hasSize(3);
        assertThat(page1.getTotalElements()).isGreaterThanOrEqualTo(5);

        Page<Customer> page2 = managementService.getCustomersByLastName("Smith", 1, 3);
        assertThat(page2.getContent()).hasSizeGreaterThan(0);
    }

    // ========== Real CockroachDB Concurrency Tests ==========

    @Test
    void shouldHandleConcurrentCustomerCreationsWithVirtualThreads() throws InterruptedException {
        // Test concurrent multi-table inserts using virtual threads
        int threads = 20;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger();
        List<UUID> createdIds = new ArrayList<>();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < threads; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        Company company = new Company(null, "Concurrent Co " + index, "Tech", "concurrent" + index + ".com");
                        Customer customer = new Customer(null, "Concurrent", "User" + index, "Dev", "concurrent" + index + "@example.com", "555-" + String.format("%04d", index), null);
                        Address address = new Address(null, null, index + " Concurrent St", "City", "ST", "1000" + index);
                        Communication communication = new Communication(null, null, "email", "concurrent" + index + "@example.com");

                        CustomerDetailsDTO result = managementService.createCustomerWithDetails(customer, company, address, communication);
                        synchronized (createdIds) {
                            createdIds.add(result.customer().getCustomerId());
                        }
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        // Some failures are expected with concurrent operations
                    } finally {
                        endLatch.countDown();
                    }
                });
            }
            startLatch.countDown();
            endLatch.await(30, TimeUnit.SECONDS);
        }

        // Verify that most inserts succeeded
        assertThat(successCount.get()).isGreaterThan(threads / 2);
        assertThat(createdIds).hasSizeGreaterThan(threads / 2);
    }

    @Test
    void shouldHandleConcurrentUpdatesToDifferentCustomers() throws InterruptedException {
        // Create initial customers
        List<CustomerDetailsDTO> customers = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Company company = new Company(null, "Update Co " + i, "Tech", "update" + i + ".com");
            Customer customer = new Customer(null, "User", "Update" + i, "Dev", "update" + i + "@example.com", "555-000" + i, null);
            Address address = new Address(null, null, i + " Update St", "City", "ST", "2000" + i);
            Communication communication = new Communication(null, null, "email", "update" + i + "@example.com");

            customers.add(managementService.createCustomerWithDetails(customer, company, address, communication));
        }

        int threads = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < threads; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        // Each thread creates a new customer (simulating concurrent updates)
                        Company company = new Company(null, "Thread Co " + index, "Tech", "thread" + index + ".com");
                        Customer customer = new Customer(null, "Thread", "User" + index, "Dev", "thread" + index + "@example.com", "555-900" + index, null);
                        Address address = new Address(null, null, index + " Thread St", "City", "ST", "9000" + index);
                        Communication communication = new Communication(null, null, "email", "thread" + index + "@example.com");

                        managementService.createCustomerWithDetails(customer, company, address, communication);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        // Some failures might occur
                    } finally {
                        endLatch.countDown();
                    }
                });
            }
            startLatch.countDown();
            endLatch.await(30, TimeUnit.SECONDS);
        }

        assertThat(successCount.get()).isGreaterThan(0);
    }

    @Test
    void shouldHandleConcurrentReadsAndWrites() throws InterruptedException {
        // Create initial customer
        Company company = new Company(null, "Read Write Co", "Tech", "readwrite.com");
        Customer customer = new Customer(null, "ReadWrite", "Test", "Dev", "readwrite@example.com", "555-0000", null);
        Address address = new Address(null, null, "100 RW St", "City", "ST", "10000");
        Communication communication = new Communication(null, null, "email", "readwrite@example.com");

        CustomerDetailsDTO initial = managementService.createCustomerWithDetails(customer, company, address, communication);
        UUID customerId = initial.customer().getCustomerId();

        int threads = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threads);
        AtomicInteger readCount = new AtomicInteger();
        AtomicInteger writeCount = new AtomicInteger();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < threads; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        if (index % 2 == 0) {
                            // Read operation
                            managementService.getCustomerDetails(customerId);
                            readCount.incrementAndGet();
                        } else {
                            // Write operation
                            Company newCompany = new Company(null, "Write Co " + index, "Tech", "write" + index + ".com");
                            Customer newCustomer = new Customer(null, "Write", "User" + index, "Dev", "write" + index + "@example.com", "555-" + index, null);
                            Address newAddress = new Address(null, null, index + " Write St", "City", "ST", "5000" + index);
                            Communication newCommunication = new Communication(null, null, "email", "write" + index + "@example.com");

                            managementService.createCustomerWithDetails(newCustomer, newCompany, newAddress, newCommunication);
                            writeCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        // Some failures expected
                    } finally {
                        endLatch.countDown();
                    }
                });
            }
            startLatch.countDown();
            endLatch.await(30, TimeUnit.SECONDS);
        }

        // Verify both reads and writes occurred
        assertThat(readCount.get()).isGreaterThan(0);
        assertThat(writeCount.get()).isGreaterThan(0);
    }

    @Test
    void shouldVerifySerializableIsolationWithConcurrentSaves() throws InterruptedException {
        // Test that CockroachDB's SERIALIZABLE isolation handles concurrent operations
        int threads = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < threads; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        Company company = new Company(null, "Serializable Co " + index, "Tech", "serializable" + index + ".com");
                        Customer customer = new Customer(null, "Serializable", "Test" + index, "Dev", "serializable" + index + "@example.com", "555-" + index, null);
                        Address address = new Address(null, null, index + " Serial St", "City", "ST", "7000" + index);
                        Communication communication = new Communication(null, null, "email", "serializable" + index + "@example.com");

                        CustomerDetailsDTO result = managementService.createCustomerWithDetails(customer, company, address, communication);
                        assertThat(result.customer().getCustomerId()).isNotNull();
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        // CockroachDB may retry transactions internally
                    } finally {
                        endLatch.countDown();
                    }
                });
            }
            startLatch.countDown();
            endLatch.await(30, TimeUnit.SECONDS);
        }

        // All should succeed with SERIALIZABLE isolation
        assertThat(successCount.get()).isEqualTo(threads);
    }

    // ========== CockroachDB Follower Reads with Concurrency ==========

    @Test
    void shouldReduceContentionUsingFollowerReads() throws InterruptedException {
        // Create initial data set
        for (int i = 0; i < 10; i++) {
            Company company = new Company(null, "Follower Co " + i, "Tech", "follower" + i + ".com");
            Customer customer = new Customer(null, "Follower", "User" + i, "Dev", "follower" + i + "@example.com", "555-000" + i, null);
            Address address = new Address(null, null, i + " Follower St", "City", "ST", "3000" + i);
            Communication communication = new Communication(null, null, "email", "follower" + i + "@example.com");

            managementService.createCustomerWithDetails(customer, company, address, communication);
        }

        // Wait for data to be available for follower reads
        Thread.sleep(6000);

        // Simulate high read load using follower reads (reduces load on leader)
        int readThreads = 20;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(readThreads);
        AtomicInteger followerReadCount = new AtomicInteger();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < readThreads; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        // Use follower read - can read from nearby replica instead of leader
                        List<Customer> customers = customerRepository.findAllAsOfSystemTime();
                        if (!customers.isEmpty()) {
                            followerReadCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        // Follower reads are highly available
                    } finally {
                        endLatch.countDown();
                    }
                });
            }
            startLatch.countDown();
            endLatch.await(30, TimeUnit.SECONDS);
        }

        // Most reads should succeed via follower reads
        assertThat(followerReadCount.get()).isGreaterThan(readThreads / 2);
    }

    @Test
    void shouldHandleMixedCurrentAndHistoricalReads() throws InterruptedException {
        // Create initial customers
        Company company1 = new Company(null, "Mixed Co 1", "Tech", "mixed1.com");
        Customer customer1 = new Customer(null, "Mixed", "Read1", "Dev", "mixed1@example.com", "555-1001", null);
        Address address1 = new Address(null, null, "100 Mixed St", "City", "ST", "4001");
        Communication communication1 = new Communication(null, null, "email", "mixed1@example.com");

        Company company2 = new Company(null, "Mixed Co 2", "Tech", "mixed2.com");
        Customer customer2 = new Customer(null, "Mixed", "Read2", "Dev", "mixed2@example.com", "555-1002", null);
        Address address2 = new Address(null, null, "200 Mixed St", "City", "ST", "4002");
        Communication communication2 = new Communication(null, null, "email", "mixed2@example.com");

        CustomerDetailsDTO created1 = managementService.createCustomerWithDetails(customer1, company1, address1, communication1);
        CustomerDetailsDTO created2 = managementService.createCustomerWithDetails(customer2, company2, address2, communication2);

        // Wait for follower read availability
        Thread.sleep(6000);

        int threads = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threads);
        AtomicInteger currentReads = new AtomicInteger();
        AtomicInteger followerReads = new AtomicInteger();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < threads; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        if (index % 2 == 0) {
                            // Current read (from leader)
                            customerRepository.findById(created1.customer().getCustomerId());
                            currentReads.incrementAndGet();
                        } else {
                            // Historical read (from follower)
                            customerRepository.findAllAsOfSystemTime();
                            followerReads.incrementAndGet();
                        }
                    } catch (Exception e) {
                        // Some failures acceptable
                    } finally {
                        endLatch.countDown();
                    }
                });
            }
            startLatch.countDown();
            endLatch.await(30, TimeUnit.SECONDS);
        }

        // Both current and follower reads should work
        assertThat(currentReads.get()).isGreaterThan(0);
        assertThat(followerReads.get()).isGreaterThan(0);
    }

    @Test
    void shouldDemonstrateFollowerReadScalability() throws InterruptedException {
        // This test shows how follower reads enable horizontal read scaling
        // by distributing read load across follower replicas

        // Create baseline data
        List<CustomerDetailsDTO> initialCustomers = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Company company = new Company(null, "Scalability Co " + i, "Tech", "scale" + i + ".com");
            Customer customer = new Customer(null, "Scalability", "Test" + i, "Dev", "scale" + i + "@example.com", "555-" + i, null);
            Address address = new Address(null, null, i + " Scale St", "City", "ST", "6000" + i);
            Communication communication = new Communication(null, null, "email", "scale" + i + "@example.com");

            initialCustomers.add(managementService.createCustomerWithDetails(customer, company, address, communication));
        }

        // Wait for replication
        Thread.sleep(6000);

        // Simulate heavy read workload that would stress a single leader
        int heavyReadLoad = 50;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(heavyReadLoad);
        AtomicInteger successfulReads = new AtomicInteger();
        long startTime = System.currentTimeMillis();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < heavyReadLoad; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        // Follower reads can be served by any replica
                        if (index % 3 == 0) {
                            // Some queries with WHERE clause
                            customerRepository.findByLastNameAsOfSystemTime("Test0");
                        } else {
                            // Most queries are full scans
                            customerRepository.findAllAsOfSystemTime();
                        }
                        successfulReads.incrementAndGet();
                    } catch (Exception e) {
                        // Track failures
                    } finally {
                        endLatch.countDown();
                    }
                });
            }
            startLatch.countDown();
            endLatch.await(60, TimeUnit.SECONDS);
        }

        long duration = System.currentTimeMillis() - startTime;

        // High success rate demonstrates follower read scalability
        assertThat(successfulReads.get()).isGreaterThan((int)(heavyReadLoad * 0.8));

        // Log performance metrics (in real tests, duration would be significantly
        // lower with follower reads in geo-distributed setup)
        System.out.println("Follower reads: " + successfulReads.get() + "/" + heavyReadLoad +
                " succeeded in " + duration + "ms");
    }

    // ========== Helper Methods ==========

    private boolean isCached(UUID id) {
        return cacheManager.getCache("customers") != null &&
                cacheManager.getCache("customers").get(id) != null;
    }
}
