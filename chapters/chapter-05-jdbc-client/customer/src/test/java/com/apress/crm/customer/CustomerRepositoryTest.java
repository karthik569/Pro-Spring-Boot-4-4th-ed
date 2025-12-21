package com.apress.crm.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.JdbcClient.MappedQuerySpec;
import org.springframework.jdbc.core.simple.JdbcClient.StatementSpec;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomerRepositoryTest {

    @Mock
    private JdbcClient jdbcClient;

    @Mock
    private StatementSpec statementSpec;

    @Mock(answer = org.mockito.Answers.RETURNS_SELF)
    private MappedQuerySpec mappedQuerySpec;

    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        customerRepository = new CustomerRepository(jdbcClient);
    }

    @Test
    void findAllShouldReturnListOfCustomers() {
        Customer customer = new Customer("John Doe", "john@example.com", "1234567890");

        given(jdbcClient.sql(anyString())).willReturn(statementSpec);
        given(statementSpec.query(any(RowMapper.class))).willReturn(mappedQuerySpec);
        given(mappedQuerySpec.list()).willReturn(List.of(customer));

        Iterable<Customer> customers = customerRepository.findAll();

        assertThat(customers).hasSize(1);
        verify(jdbcClient).sql("SELECT id, name, email, phone FROM customer");
    }

    @Test
    void findByIdShouldReturnCustomer() {
        UUID id = UUID.randomUUID();
        Customer customer = new Customer(id, "John Doe", "john@example.com", "1234567890");

        given(jdbcClient.sql(anyString())).willReturn(statementSpec);
        given(statementSpec.param("id", id)).willReturn(statementSpec);
        given(statementSpec.query(Customer.class)).willReturn(mappedQuerySpec);
        given(mappedQuerySpec.optional()).willReturn(Optional.of(customer));

        Customer result = customerRepository.findById(id);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(id);
    }
}
