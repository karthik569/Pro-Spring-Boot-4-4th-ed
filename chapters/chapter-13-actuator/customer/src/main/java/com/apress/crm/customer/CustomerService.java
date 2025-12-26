package com.apress.crm.customer;

import com.apress.crm.customer.actuator.CustomerMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMetrics customerMetrics;
    private final MeterRegistry meterRegistry;

    public CustomerService(CustomerRepository customerRepository, CustomerMetrics customerMetrics, MeterRegistry meterRegistry) {
        this.customerRepository = customerRepository;
        this.customerMetrics = customerMetrics;
        this.meterRegistry = meterRegistry;
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Cacheable(value = "customers", key = "#id")
    public Optional<Customer> findById(UUID id) {
        return customerRepository.findById(id);
    }

    @Transactional
    @Retryable(retryFor = SQLException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @CacheEvict(value = "customers", allEntries = true)
    public Customer save(Customer customer) {
        Timer.Sample sample = customerMetrics.startTimer(meterRegistry);
        try {
            Customer saved = customerRepository.save(customer);
            customerMetrics.incrementCreated();
            return saved;
        } finally {
            customerMetrics.stopTimer(sample);
        }
    }

    @Transactional
    @Retryable(retryFor = SQLException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @CacheEvict(value = "customers", allEntries = true)
    public void deleteById(UUID id) {
        customerRepository.deleteById(id);
    }
}
