package com.apress.crm.customer;

import com.apress.crm.customer.aot.CustomerBeanRegistrar;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.security.crypto.password.PasswordEncoder;

import static java.lang.System.out;

@Configuration
@EnableCaching
@EnableRetry
@Import(CustomerBeanRegistrar.class)  // Enable AOT-friendly bean registration
public class CustomerConfiguration {

    @Bean
    ApplicationListener<ApplicationReadyEvent> customerAppReady(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        return event -> {
            customerRepository.save(new Customer("John", "john@example.com", "1-800-PHONE", passwordEncoder.encode("password")));
            customerRepository.save(new Customer("Jane", "jane@example.com", "1-800-PHONE", passwordEncoder.encode("password")));
            customerRepository.save(new Customer("Jack", "jack@example.com", "1-800-PHONE", passwordEncoder.encode("password")));

            customerRepository.findAll().forEach(out::println);
        };
    }
}
