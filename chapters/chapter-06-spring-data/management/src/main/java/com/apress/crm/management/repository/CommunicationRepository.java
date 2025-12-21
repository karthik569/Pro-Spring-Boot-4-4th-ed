package com.apress.crm.management.repository;

import com.apress.crm.management.model.Communication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommunicationRepository extends JpaRepository<Communication, UUID> {
    Iterable<Communication> findByCustomerCustomerId(UUID customerId);
}
