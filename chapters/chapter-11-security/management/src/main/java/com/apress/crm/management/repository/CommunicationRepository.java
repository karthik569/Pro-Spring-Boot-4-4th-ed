package com.apress.crm.management.repository;

import com.apress.crm.management.model.Communication;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface CommunicationRepository extends R2dbcRepository<Communication, UUID> {
    Flux<Communication> findAllByCustomerId(UUID customerId);
}