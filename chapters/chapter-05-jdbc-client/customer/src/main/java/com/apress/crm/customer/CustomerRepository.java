package com.apress.crm.customer;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CustomerRepository implements Repository<Customer, UUID> {

    private final JdbcClient jdbcClient;

    public CustomerRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    // Demonstrating a custom RowMapper, though Record mapping is automatic
    private final RowMapper<Customer> customerRowMapper = (rs, rowNum) -> new Customer(
            UUID.fromString(rs.getString("id")),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("phone")
    );

    @Override
    public Customer save(Customer entity) {
        UUID id = entity.id() != null ? entity.id() : UUID.randomUUID();
        Customer customerToSave = new Customer(id, entity.name(), entity.email(), entity.phone());

        int updated = jdbcClient.sql("UPDATE customer SET name = ?, email = ?, phone = ? WHERE id = ?")
                .params(customerToSave.name(), customerToSave.email(), customerToSave.phone(), customerToSave.id())
                .update();

        if (updated == 0) {
            jdbcClient.sql("INSERT INTO customer (id, name, email, phone) VALUES (?, ?, ?, ?)")
                    .params(customerToSave.id(), customerToSave.name(), customerToSave.email(), customerToSave.phone())
                    .update();
        }

        return customerToSave;
    }

    @Override
    public Customer findById(UUID id) {
        // Using the fluent API with automatic Record mapping
        return jdbcClient.sql("SELECT id, name, email, phone FROM customer WHERE id = :id")
                .param("id", id)
                .query(Customer.class) // Auto-maps to Record
                .optional()
                .orElse(null);
    }

    @Override
    public Iterable<Customer> findAll() {
        // Demonstrating usage of the custom RowMapper
        return jdbcClient.sql("SELECT id, name, email, phone FROM customer")
                .query(customerRowMapper)
                .list();
    }

    @Override
    public void deleteById(UUID id) {
        jdbcClient.sql("DELETE FROM customer WHERE id = :id")
                .param("id", id)
                .update();
    }
}