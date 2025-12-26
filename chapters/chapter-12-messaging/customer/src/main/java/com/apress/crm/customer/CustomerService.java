package com.apress.crm.customer;

import com.apress.crm.customer.event.CustomerCreatedEvent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final ApplicationEventPublisher publisher;

    public CustomerService(CustomerRepository customerRepository, ApplicationEventPublisher publisher) {
        this.customerRepository = customerRepository;
        this.publisher = publisher;
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
        Customer saved = customerRepository.save(customer);
        // Publish event for internal listeners
        publisher.publishEvent(new CustomerCreatedEvent(saved, Instant.now()));
        return saved;
    }

    @Transactional
    @Retryable(retryFor = SQLException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @CacheEvict(value = "customers", allEntries = true)
    public void deleteById(UUID id) {
        customerRepository.deleteById(id);
    }
}
