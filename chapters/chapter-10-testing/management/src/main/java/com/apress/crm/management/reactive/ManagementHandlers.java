package com.apress.crm.management.reactive;

import com.apress.crm.management.model.Address;
import com.apress.crm.management.model.Communication;
import com.apress.crm.management.model.Company;
import com.apress.crm.management.model.Customer;
import com.apress.crm.management.service.ManagementService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

@Component
public class ManagementHandlers {

    private final ManagementService managementService;

    public ManagementHandlers(ManagementService managementService) {
        this.managementService = managementService;
    }

    public Mono<ServerResponse> getCustomerDetails(ServerRequest request) {
        String id = request.pathVariable("id");
        return managementService.getCustomerDetails(UUID.fromString(id))
                .flatMap(details -> ServerResponse.ok().bodyValue(details))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> createCustomer(ServerRequest request) {
        return request.bodyToMono(Customer.class)
                .flatMap(customer -> {
                    // Create default associated entities for the simplified create endpoint
                    Company company = new Company(null, "Default Company", "Technology", "example.com");
                    Address address = new Address(null, null, "123 Default St", "City", "ST", "00000");
                    Communication comm = new Communication(null, null, "email", customer.email());
                    
                    return managementService.createCustomerWithDetails(customer, company, address, comm);
                })
                .flatMap(details -> ServerResponse.created(URI.create("/api/v1/management/customers/" + details.customer().customerId()))
                        .bodyValue(details.customer()));
    }
}