package com.apress.crm.customer;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

@Configuration
public class CustomerConfiguration {

    @Bean
    ApplicationListener<ApplicationReadyEvent> customerAppReady(CustomerRepository customerRepository) {
        return event -> {
            customerRepository.deleteAll()
                    .thenMany(
                            Flux.just(
                                    new Customer("John", "Doe", "john@example.com", "1-800-PHONE"),
                                    new Customer("Jane", "Doe", "jane@example.com", "1-800-PHONE"),
                                    new Customer("Jack", "Smith", "jack@example.com", "1-800-PHONE")
                            )
                    )
                    .flatMap(customerRepository::save)
                    .subscribe(c -> System.out.println("Saved: " + c));
        };
    }
}