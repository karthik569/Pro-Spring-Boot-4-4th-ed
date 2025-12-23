package com.apress.crm.management.repository.jpa;

import com.apress.crm.management.model.Communication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface CommunicationRepository extends JpaRepository<Communication, UUID> {
    List<Communication> findByCustomerId(UUID customerId);
}