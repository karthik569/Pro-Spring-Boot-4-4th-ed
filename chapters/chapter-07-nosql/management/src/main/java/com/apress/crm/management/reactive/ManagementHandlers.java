package com.apress.crm.management.reactive;

import com.apress.crm.management.service.CorporateService;
import com.apress.crm.management.service.ManagementService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Component
public class ManagementHandlers {

    private final ManagementService managementService;
    private final CorporateService corporateService;

    public ManagementHandlers(ManagementService managementService, CorporateService corporateService) {
        this.managementService = managementService;
        this.corporateService = corporateService;
    }

    // Listing 7-8: Reactive handler using session-aware service method
    public Mono<ServerResponse> getCustomerDetails(ServerRequest request) {
        UUID customerId = UUID.fromString(request.pathVariable("id"));
        return managementService.getCustomerWithSession(customerId)
                .flatMap(dto -> ServerResponse.ok().bodyValue(dto))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    // Link companies in Neo4j
    public Mono<ServerResponse> linkCompanies(ServerRequest request) {
        String parent = request.queryParam("parent").orElseThrow();
        String subsidiary = request.queryParam("subsidiary").orElseThrow();
        
        return Mono.fromRunnable(() -> corporateService.linkSubsidiary(parent, subsidiary))
                .subscribeOn(Schedulers.boundedElastic())
                .then(ServerResponse.ok().build());
    }
}
