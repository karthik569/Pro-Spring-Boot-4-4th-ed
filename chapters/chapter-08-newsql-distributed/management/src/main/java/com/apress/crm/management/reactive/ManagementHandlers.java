package com.apress.crm.management.reactive;

import com.apress.crm.management.model.CustomerDetailsDTO;
import com.apress.crm.management.service.ManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Component
public class ManagementHandlers {

    private static final Logger log = LoggerFactory.getLogger(ManagementHandlers.class);

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

    public Mono<ServerResponse> handleCustomerSync(ServerRequest request) {
        return request.bodyToMono(Map.class)
                .doOnNext(payload -> log.info("Received CDC Sync: {}", payload))
                .flatMap(payload -> ServerResponse.ok().build());
    }
}
