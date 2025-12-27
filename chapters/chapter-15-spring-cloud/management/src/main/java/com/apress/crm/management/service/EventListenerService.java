package com.apress.crm.management.service;

import com.apress.crm.management.dto.CustomerEvent;
import com.apress.crm.management.model.Customer;
import com.apress.crm.management.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

/**
 * Event Listener Service for consuming customer events from RabbitMQ.
 *
 * This service listens for customer events published by the customer-service
 * and synchronizes the customer data in the management service's local database.
 *
 * Uses Spring Cloud Stream's functional programming model where the bean name
 * 'onCustomerChange' automatically maps to the binding 'onCustomerChange-in-0'.
 */
@Service
public class EventListenerService {

    private static final Logger log = LoggerFactory.getLogger(EventListenerService.class);

    private final CustomerRepository customerRepository;

    public EventListenerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Consumer function that processes customer events.
     * Spring Cloud Stream automatically discovers this bean and binds it to the input channel.
     *
     * The binding name will be: onCustomerChange-in-0
     * This is configured in the Config Server to listen to the 'customer-events-topic'.
     */
    @Bean
    public Consumer<CustomerEvent> onCustomerChange() {
        return event -> {
            log.info("Received customer event: {} (ID: {}, Email: {})",
                    event.name(), event.id(), event.email());

            // Process the event asynchronously using reactive programming
            processCustomerEvent(event)
                    .subscribe(
                            savedCustomer -> log.info("Successfully processed customer event for: {} (ID: {})",
                                    savedCustomer.firstName(), savedCustomer.customerId()),
                            error -> log.error("Error processing customer event: {}",
                                    error.getMessage(), error)
                    );
        };
    }

    /**
     * Processes the customer event by saving or updating the customer in the local database.
     */
    private Mono<Customer> processCustomerEvent(CustomerEvent event) {
        return customerRepository.findById(event.id())
                .flatMap(existing -> {
                    // Customer exists - update it
                    log.debug("Updating existing customer: {}", event.id());
                    return updateCustomer(existing, event);
                })
                .switchIfEmpty(
                        // Customer doesn't exist - create it
                        Mono.defer(() -> {
                            log.debug("Creating new customer: {}", event.id());
                            return createCustomer(event);
                        })
                );
    }

    /**
     * Creates a new customer from the event.
     */
    private Mono<Customer> createCustomer(CustomerEvent event) {
        // Split name into firstName and lastName (simple logic)
        String[] nameParts = event.name().split(" ", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        Customer customer = new Customer(
                event.id(),
                firstName,
                lastName,
                null, // jobTitle
                event.email(),
                event.phone(),
                null  // companyId
        );
        return customerRepository.save(customer);
    }

    /**
     * Updates an existing customer with new data from the event.
     */
    private Mono<Customer> updateCustomer(Customer existing, CustomerEvent event) {
        // Split name into firstName and lastName (simple logic)
        String[] nameParts = event.name().split(" ", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        // Records are immutable, create a new instance with updated values
        Customer updated = new Customer(
                existing.customerId(),
                firstName,
                lastName,
                existing.jobTitle(),
                event.email(),
                event.phone() != null ? event.phone() : existing.phone(),
                existing.companyId()
        );

        return customerRepository.save(updated);
    }
}
