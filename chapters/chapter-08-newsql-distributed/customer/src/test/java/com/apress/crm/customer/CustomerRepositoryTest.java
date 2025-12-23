package com.apress.crm.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository repository;

    @Test
    void shouldSaveAndFindCustomer() {
        Customer customer = new Customer("John Doe", "john.doe@example.com", "1234567890");
        Customer saved = repository.save(customer);

        assertThat(saved.getId()).isNotNull();

        Optional<Customer> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John Doe");
    }

    @Test
    void shouldFindAllCustomers() {
        repository.save(new Customer("User 1", "user1@example.com", "111"));
        repository.save(new Customer("User 2", "user2@example.com", "222"));

        List<Customer> all = repository.findAll();
        assertThat(all).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldDeleteCustomer() {
        Customer saved = repository.save(new Customer("To Delete", "delete@example.com", "000"));
        UUID id = saved.getId();

        repository.deleteById(id);

        assertThat(repository.findById(id)).isEmpty();
    }

    @Test
    void shouldFindByName() {
        repository.save(new Customer("Felipe", "felipe@example.com", "123"));
        List<Customer> result = repository.findByName("Felipe");
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getName()).isEqualTo("Felipe");
    }

    @Test
    void shouldFindByEmailEndingWith() {
        repository.save(new Customer("Josh", "josh@spring.io", "123"));
        List<Customer> result = repository.findByEmailEndingWith("spring.io");
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getEmail()).endsWith("spring.io");
    }
}
