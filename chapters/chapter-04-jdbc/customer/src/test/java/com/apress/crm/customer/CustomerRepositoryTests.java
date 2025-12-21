package com.apress.crm.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest // (1)
@Import(JdbcClientCustomerRepository.class) // (2)
class CustomerRepositoryTests {

    @Autowired
    private JdbcClientCustomerRepository repository;

    @Test
    void shouldPersistAndRetrieveCustomer() {
        Customer customer = new Customer(UUID.randomUUID(), "John Doe", "john@test.com", "123-456");

        repository.save(customer);

        Customer found = repository.findById(customer.id());
        assertThat(found).isNotNull();
        assertThat(found.email()).isEqualTo("john@test.com");
    }

}