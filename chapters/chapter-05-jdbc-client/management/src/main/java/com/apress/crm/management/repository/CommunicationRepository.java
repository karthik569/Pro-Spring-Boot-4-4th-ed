package com.apress.crm.management.repository;

import com.apress.crm.management.model.Communication;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CommunicationRepository implements Repository<Communication, UUID> {

    private final JdbcClient jdbcClient;

    public CommunicationRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Communication save(Communication entity) {
        UUID id = entity.communicationId() != null ? entity.communicationId() : UUID.randomUUID();
        Communication communicationToSave = new Communication(id, entity.customerId(), entity.communicationType(), entity.communicationValue());

        int updated = jdbcClient.sql("UPDATE communication SET customer_id = ?, communication_type = ?, communication_value = ? WHERE communication_id = ?")
                .params(communicationToSave.customerId(), communicationToSave.communicationType(), communicationToSave.communicationValue(), communicationToSave.communicationId())
                .update();

        if (updated == 0) {
            jdbcClient.sql("INSERT INTO communication (communication_id, customer_id, communication_type, communication_value) VALUES (?, ?, ?, ?)")
                    .params(communicationToSave.communicationId(), communicationToSave.customerId(), communicationToSave.communicationType(), communicationToSave.communicationValue())
                    .update();
        }

        return communicationToSave;
    }

    @Override
    public Communication findById(UUID uuid) {
        return jdbcClient.sql("SELECT communication_id, customer_id, communication_type, communication_value FROM communication WHERE communication_id = :id")
                .param("id", uuid)
                .query(Communication.class)
                .optional()
                .orElse(null);
    }

    @Override
    public Iterable<Communication> findAll() {
        return jdbcClient.sql("SELECT communication_id, customer_id, communication_type, communication_value FROM communication")
                .query(Communication.class)
                .list();
    }

    @Override
    public void deleteById(UUID uuid) {
        jdbcClient.sql("DELETE FROM communication WHERE communication_id = :id")
                .param("id", uuid)
                .update();
    }

    public Iterable<Communication> findAllByCustomerId(UUID customerId) {
        return jdbcClient.sql("SELECT communication_id, customer_id, communication_type, communication_value FROM communication WHERE customer_id = :customerId")
                .param("customerId", customerId)
                .query(Communication.class)
                .list();
    }
}