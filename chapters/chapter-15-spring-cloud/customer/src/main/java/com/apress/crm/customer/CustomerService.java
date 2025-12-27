package com.apress.crm.customer;

import com.apress.crm.customer.actuator.CustomerMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for Customer operations.
 *
 * This service publishes events to RabbitMQ when customers are created or updated,
 * enabling event-driven communication with the management service.
 */
@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final CustomerMetrics customerMetrics;
    private final MeterRegistry meterRegistry;
    private final StreamBridge streamBridge;

    public CustomerService(CustomerRepository customerRepository,
                          CustomerMetrics customerMetrics,
                          MeterRegistry meterRegistry,
                          StreamBridge streamBridge) {
        this.customerRepository = customerRepository;
        this.customerMetrics = customerMetrics;
        this.meterRegistry = meterRegistry;
        this.streamBridge = streamBridge;
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

            // Publish customer event to RabbitMQ
            publishCustomerEvent(saved);

            return saved;
        } finally {
            customerMetrics.stopTimer(sample);
        }
    }

    /**
     * Publishes a customer event to the message broker.
     * The management service will consume this event and update its local state.
     */
    private void publishCustomerEvent(Customer customer) {
        try {
            // Send to the 'customer-events-out-0' binding (configured in Config Server)
            boolean sent = streamBridge.send("customer-events-out-0", customer);
            if (sent) {
                log.info("Published customer event for customer: {} (ID: {})",
                        customer.getName(), customer.getId());
            } else {
                log.warn("Failed to publish customer event for customer: {}",
                        customer.getId());
            }
        } catch (Exception e) {
            log.error("Error publishing customer event: {}", e.getMessage(), e);
            // Don't fail the transaction if event publishing fails
        }
    }

    @Transactional
    @Retryable(retryFor = SQLException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @CacheEvict(value = "customers", allEntries = true)
    public void deleteById(UUID id) {
        customerRepository.deleteById(id);
    }
}
