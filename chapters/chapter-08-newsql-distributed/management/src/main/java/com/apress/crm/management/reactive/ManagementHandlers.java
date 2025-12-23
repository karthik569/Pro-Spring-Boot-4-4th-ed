package com.apress.crm.management.reactive;

import com.apress.crm.management.model.CustomerDetailsDTO;
import com.apress.crm.management.service.ManagementService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class ManagementHandlers {

    private final ManagementService managementService;

    public ManagementHandlers(ManagementService managementService) {
        this.managementService = managementService;
    }

    public Mono<ServerResponse> getCustomerDetails(ServerRequest request) {
        UUID customerId = UUID.fromString(request.pathVariable("id"));
        CustomerDetailsDTO customerDetails = managementService.getCustomerDetails(customerId);
        return Mono.justOrEmpty(customerDetails)
                .flatMap(dto -> ServerResponse.ok().bodyValue(dto))
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}
