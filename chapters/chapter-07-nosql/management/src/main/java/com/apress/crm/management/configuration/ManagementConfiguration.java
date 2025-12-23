package com.apress.crm.management.configuration;

import com.apress.crm.management.model.*;
import com.apress.crm.management.reactive.ManagementHandlers;
import com.apress.crm.management.service.ManagementService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ManagementConfiguration {

    // Listing 7-9: Reactive routes bean
    @Bean
    public RouterFunction<ServerResponse> managementRoutes(ManagementHandlers handlers) {
        return route(GET("/api/v1/management/customers/{id}"), handlers::getCustomerDetails)
                .andRoute(POST("/api/v1/management/corporate/link"), handlers::linkCompanies);
    }

    // Listing 7-9: Data initialization using ApplicationListener
    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "app.init-data", havingValue = "true")
    public ApplicationListener<ApplicationReadyEvent> managementReady(ManagementService managementService) {
        return event -> {
            System.out.println("Initializing data for Chapter 7 Polyglot...");
            try {
                // Customer 1

                Company company1 = new Company(null, "Apress", "Publishing", "apress.com");
                Customer customer1 = new Customer(null, "Felipe", "Gutierrez", "Senior Staff Engineer", "felipe.g@apress.com", "123-456-7890", null);
                Address address1 = new Address(null, null, "123 Main St", "New York", "NY", "10001");
                Communication communication1 = new Communication(null, null, "email", "felipe.g@apress.com");
                
                System.out.println("Creating customer 1...");
                CustomerDetailsDTO details1 = managementService.createCustomerWithDetails(customer1, company1, address1, communication1);
                System.out.println("Starting session for customer 1...");
                managementService.startSession(details1.customer().getCustomerId()).subscribe();

                // Customer 2
                Company company2 = new Company(null, "Spring", "Software", "spring.io");
                Customer customer2 = new Customer(null, "Josh", "Long", "Developer Advocate", "josh.long@spring.io", "098-765-4321", null);
                Address address2 = new Address(null, null, "456 Oak Ave", "San Francisco", "CA", "94102");
                Communication communication2 = new Communication(null, null, "twitter", "@starbuxman");
                
                System.out.println("Creating customer 2...");
                managementService.createCustomerWithDetails(customer2, company2, address2, communication2);
                
                System.out.println("Data initialization complete.");
            } catch (Exception e) {
                System.err.println("Error during data initialization: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}
