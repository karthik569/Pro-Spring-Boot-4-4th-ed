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
        String id = request.pathVariable("id");
        CustomerDetailsDTO details = managementService.getCustomerDetails(UUID.fromString(id));
        if (details != null) {
            return ServerResponse.ok().bodyValue(details);
        } else {
            return ServerResponse.notFound().build();
        }
    }
}