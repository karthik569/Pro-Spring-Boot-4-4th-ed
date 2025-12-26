package com.apress.crm.management.repository;

import com.apress.crm.management.model.Address;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface AddressRepository extends R2dbcRepository<Address, UUID> {
    Flux<Address> findAllByCustomerId(UUID customerId);
}