package com.apress.crm.customer;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface CustomerRepository extends R2dbcRepository<Customer, UUID> {
    Flux<Customer> findByLastName(String lastName);
}


