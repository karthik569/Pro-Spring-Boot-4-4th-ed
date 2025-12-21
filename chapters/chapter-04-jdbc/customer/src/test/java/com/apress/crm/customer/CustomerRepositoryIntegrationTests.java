package com.apress.crm.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional // (1)
class CustomerRepositoryIntegrationTests {

    @Autowired
    private CustomerRepository repository;

    @Test
    void shouldPerformCrudOperations() {
        // 1. Create
        Customer customer = new Customer(UUID.randomUUID(), "Integration Test", "it@test.com", "123-456");
        repository.save(customer);

        // 2. Read (Find By ID)
        Customer found = repository.findById(customer.id());
        assertThat(found).isNotNull();
        assertThat(found.name()).isEqualTo("Integration Test");

        // 3. Update
        Customer toUpdate = new Customer(customer.id(), "Updated Name", "it@test.com", "123-456");
        repository.save(toUpdate);

        Customer updated = repository.findById(customer.id());
        assertThat(updated.name()).isEqualTo("Updated Name");

        // 4. Read (Find All)
        // Note: The DB might contain initial data from CustomerConfiguration
        Iterable<Customer> all = repository.findAll();
        assertThat(all).isNotEmpty();

        // 5. Delete
        repository.deleteById(customer.id());
        Customer deleted = repository.findById(customer.id());
        assertThat(deleted).isNull();
    }

}