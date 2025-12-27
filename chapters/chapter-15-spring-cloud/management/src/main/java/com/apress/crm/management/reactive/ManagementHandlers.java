package com.apress.crm.management.reactive;

import com.apress.crm.management.model.Customer;
import com.apress.crm.management.service.ManagementService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ManagementHandlers {

    private final ManagementService managementService;
    private final Validator validator;

    public ManagementHandlers(ManagementService managementService, Validator validator) {
        this.managementService = managementService;
        this.validator = validator;
    }

    public Mono<ServerResponse> getCustomerDetails(ServerRequest request) {
        String id = request.pathVariable("id");
        return managementService.getCustomerDetails(UUID.fromString(id))
                .flatMap(details -> ServerResponse.ok().bodyValue(details))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> createCustomer(ServerRequest request) {
        return request.bodyToMono(Customer.class)
                .flatMap(customer -> validate(customer, request))
                .flatMap(managementService::saveCustomer)
                .flatMap(saved -> ServerResponse.created(URI.create("/api/v1/customers/" + saved.customerId()))
                        .bodyValue(saved));
    }

    private <T> Mono<T> validate(T body, ServerRequest request) {
        Set<ConstraintViolation<T>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Validation failed: " + errorMessage));
        }
        return Mono.just(body);
    }
}