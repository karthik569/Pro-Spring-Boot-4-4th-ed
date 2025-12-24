package com.apress.crm.customer;

import io.r2dbc.spi.R2dbcException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerRepository repository;

    public CustomerController(CustomerRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    Flux<Customer> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    Mono<ResponseEntity<Customer>> findById(@PathVariable UUID id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Mono<Customer> save(@Valid @RequestBody Customer customer) {
        return repository.save(customer)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                        .filter(throwable -> throwable instanceof R2dbcException &&
                                "40001".equals(((R2dbcException) throwable).getSqlState())));
    }

    @DeleteMapping("/{id}")
    Mono<Void> deleteById(@PathVariable UUID id) {
        return repository.deleteById(id);
    }
}