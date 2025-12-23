package com.apress.crm.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.dao.PessimisticLockingFailureException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CustomerConcurrencyTest extends BaseTest {

    @Autowired
    private CustomerService customerService;

    @MockitoSpyBean
    private CustomerRepository customerRepository;

    @Test
    void shouldRetryOnSqlException_HappyPath() {
        Customer customer = new Customer("Retry User", "retry@example.com", "123");
        
        doThrow(new PessimisticLockingFailureException("Simulated lock failure"))
                .doCallRealMethod()
                .when(customerRepository).save(any(Customer.class));

        Customer saved = customerService.save(customer);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        
        verify(customerRepository, times(2)).save(any(Customer.class));
    }

    @Test
    void shouldEmulateConcurrencyWithVirtualThreads() throws InterruptedException {
        Customer customer = customerService.save(new Customer("Concurrent User", "concurrent@example.com", "123"));
        
        int threads = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < threads; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        customerService.save(customer);
                        successCount.incrementAndGet();
                    } catch (Exception ignored) {
                    } finally {
                        endLatch.countDown();
                    }
                });
            }
            startLatch.countDown();
            endLatch.await(10, TimeUnit.SECONDS);
        }

        assertThat(successCount.get()).isGreaterThan(0);
    }

    @Test
    void shouldFailAfterMaxRetries() {
        Customer customer = new Customer("Fail User", "fail@example.com", "123");

        doThrow(new PessimisticLockingFailureException("Simulated persistent lock failure"))
                .when(customerRepository).save(any(Customer.class));

        assertThatThrownBy(() -> customerService.save(customer))
                .isInstanceOf(PessimisticLockingFailureException.class);

        verify(customerRepository, times(3)).save(any(Customer.class));
    }
}