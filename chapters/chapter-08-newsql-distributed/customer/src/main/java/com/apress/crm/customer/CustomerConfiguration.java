package com.apress.crm.customer;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

import static java.lang.System.out;

@Configuration
@EnableCaching
@EnableRetry
public class CustomerConfiguration {

    @Bean
    ApplicationListener<ApplicationReadyEvent> customerAppReady(CustomerRepository customerRepository) {
        return event -> {
            customerRepository.save(new Customer("John", "john@example.com", "1-800-PHONE"));
            customerRepository.save(new Customer("Jane", "jane@example.com", "1-800-PHONE"));
            customerRepository.save(new Customer("Jack", "jack@example.com", "1-800-PHONE"));

            customerRepository.findAll().forEach(out::println);
        };
    }
}
