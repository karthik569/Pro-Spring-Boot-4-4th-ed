package com.apress.crm.management.repository;

import com.apress.crm.management.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.JdbcClient.MappedQuerySpec;
import org.springframework.jdbc.core.simple.JdbcClient.StatementSpec;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CustomerRepositoryTest {

    @Mock
    private JdbcClient jdbcClient;

    @Mock
    private StatementSpec statementSpec;

    @Mock
    private MappedQuerySpec mappedQuerySpec;

    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        customerRepository = new CustomerRepository(jdbcClient);
    }

    @Test
    void findAllShouldReturnListOfCustomers() {
        Customer customer = new Customer(UUID.randomUUID(), "John", "Doe", "Dev", "john@example.com", "123", UUID.randomUUID());

        given(jdbcClient.sql(anyString())).willReturn(statementSpec);
        given(statementSpec.query(Customer.class)).willReturn(mappedQuerySpec);
        given(mappedQuerySpec.list()).willReturn(List.of(customer));

        Iterable<Customer> customers = customerRepository.findAll();

        assertThat(customers).hasSize(1);
    }
}