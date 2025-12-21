package com.apress.crm.management.repository;

import com.apress.crm.management.model.Customer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManagementCustomerRepositoryTest {

    @Mock
    private JdbcClient jdbcClient;

    @Mock
    private JdbcClient.StatementSpec statementSpec;

    @Mock
    private JdbcClient.MappedQuerySpec<Customer> querySpec;

    @InjectMocks
    private CustomerRepository repository;

    @Test
    void findByIdShouldReturnCustomerRecord() {
        // Given
        UUID id = UUID.randomUUID();
        Customer customer = new Customer(id, "Sundar", "Pichai", "CEO",
                "sundar@google.com", "111", null);

        when(jdbcClient.sql(anyString())).thenReturn(statementSpec);
        when(statementSpec.param("id", id)).thenReturn(statementSpec);
        when(statementSpec.query(Customer.class)).thenReturn(querySpec);
        when(querySpec.optional()).thenReturn(Optional.of(customer));

        // When
        Customer result = repository.findById(id);

        // Then
        assertEquals("Sundar", result.firstName());
        assertEquals("CEO", result.jobTitle());
    }
}
