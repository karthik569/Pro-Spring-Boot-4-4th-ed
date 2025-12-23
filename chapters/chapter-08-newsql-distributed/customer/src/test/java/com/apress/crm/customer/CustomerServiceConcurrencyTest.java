package com.apress.crm.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.dao.PessimisticLockingFailureException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doCallRealMethod;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CustomerServiceConcurrencyTest {

    @Autowired
    private CustomerService customerService;

    @MockitoSpyBean
    private CustomerRepository customerRepository;

    @Test
    void shouldRetryOnSqlException_HappyPath() {
        Customer customer = new Customer("Retry User", "retry@example.com", "123");
        
        // Mock the repository to throw a SQLException (simulating a transient DB error) on the first call, then succeed
        doThrow(new PessimisticLockingFailureException("Simulated lock failure"))
                .doCallRealMethod()
                .when(customerRepository).save(any(Customer.class));

        // This call should succeed after one retry
        Customer saved = customerService.save(customer);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        
        // Verify save was called twice (1 failure + 1 success)
        verify(customerRepository, times(2)).save(any(Customer.class));
    }

    @Test
    void shouldFailAfterMaxRetries() {
        Customer customer = new Customer("Fail User", "fail@example.com", "123");

        // Mock the repository to always throw exception
        doThrow(new PessimisticLockingFailureException("Simulated persistent lock failure"))
                .when(customerRepository).save(any(Customer.class));

        // Expect the service to eventually give up and throw the exception
        assertThatThrownBy(() -> customerService.save(customer))
                .isInstanceOf(PessimisticLockingFailureException.class);

        // Verify save was called maxAttempts times (3 by default)
        verify(customerRepository, times(3)).save(any(Customer.class));
    }
}