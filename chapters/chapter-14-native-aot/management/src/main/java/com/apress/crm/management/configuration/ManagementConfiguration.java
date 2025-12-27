package com.apress.crm.management.configuration;

import com.apress.crm.management.aot.ManagementBeanRegistrar;
import com.apress.crm.management.model.Address;
import com.apress.crm.management.model.Communication;
import com.apress.crm.management.model.Company;
import com.apress.crm.management.model.Customer;
import com.apress.crm.management.reactive.ManagementHandlers;
import com.apress.crm.management.service.ManagementService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@Import(ManagementBeanRegistrar.class)  // Enable AOT-friendly programmatic bean registration
public class ManagementConfiguration {

    @Bean
    @org.springframework.context.annotation.Profile("!test")
    ApplicationListener<ApplicationReadyEvent> managementReady(ManagementService managementService) {
        return event -> {
            // Customer 1
            Company company1 = new Company(null, "Apress", "Publishing", "apress.com");
            Customer customer1 = new Customer(null, "Felipe", "Gomez", "Author", "felipe.gomez@apress.com", "123-456-7890", null);
            Address address1 = new Address(null, null, "123 Main St", "New York", "NY", "10001");
            Communication communication1 = new Communication(null, null, "email", "felipe.gomez@apress.com");
            
            managementService.createCustomerWithDetails(customer1, company1, address1, communication1)
                    .subscribe(dto -> System.out.println("Created customer 1: " + dto));

            // Customer 2
            Company company2 = new Company(null, "Spring", "Software", "spring.io");
            Customer customer2 = new Customer(null, "Josh", "Long", "Developer Advocate", "josh.long@spring.io", "098-765-4321", null);
            Address address2 = new Address(null, null, "456 Oak Ave", "San Francisco", "CA", "94102");
            Communication communication2 = new Communication(null, null, "twitter", "@starbuxman");
            
            managementService.createCustomerWithDetails(customer2, company2, address2, communication2)
                    .subscribe(dto -> System.out.println("Created customer 2: " + dto));

            // Customer 3
            Company company3 = new Company(null, "Google", "Technology", "google.com");
            Customer customer3 = new Customer(null, "Sundar", "Pichai", "CEO", "sundar.pichai@google.com", "111-222-3333", null);
            Address address3 = new Address(null, null, "789 Pine Ln", "Mountain View", "CA", "94043");
            Communication communication3 = new Communication(null, null, "email", "sundar.pichai@google.com");
            
            managementService.createCustomerWithDetails(customer3, company3, address3, communication3)
                    .subscribe(dto -> System.out.println("Created customer 3: " + dto));
        };
    }

    @Bean
    RouterFunction<ServerResponse> managementRoutes(ManagementHandlers handlers) {
        return route(GET("/api/v1/customers/{id}"), handlers::getCustomerDetails)
                .andRoute(POST("/api/v1/customers"), handlers::createCustomer);
    }
}