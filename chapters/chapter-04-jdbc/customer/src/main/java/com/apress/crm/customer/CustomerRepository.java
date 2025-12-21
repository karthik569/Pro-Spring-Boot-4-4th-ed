package com.apress.crm.customer;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

public class CustomerRepository implements Repository<Customer, UUID> {

    private final JdbcTemplate jdbcTemplate;
    private final CustomerRowMapper rowMapper = new CustomerRowMapper();

    public CustomerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Iterable<Customer> findAll() {
        return jdbcTemplate.query("SELECT * FROM customer", rowMapper);
    }

    @Override
    public Customer findById(UUID id) {
        String sql = "SELECT * FROM customer WHERE id = ?";
        List<Customer> result = jdbcTemplate.query(sql, rowMapper, id);
        return result.stream().findFirst().orElse(null);
    }

    @Override
    public Customer save(Customer customer) {
        // If ID is null, generate one. Records are immutable, so we create a new instance.
        if (customer.id() == null) {
            Customer newCustomer = new Customer(UUID.randomUUID(), customer.name(), customer.email(), customer.phone());
            create(newCustomer);
            return newCustomer;
        }

        // If ID exists, check if it's an update
        if (findById(customer.id()) != null) {
            update(customer);
            return customer;
        }

        // ID exists but record doesn't, so create it (e.g. predefined UUID)
        create(customer);
        return customer;
    }

    private void create(Customer customer) {
        String sql = "INSERT INTO customer (id, name, email, phone) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, customer.id(), customer.name(), customer.email(), customer.phone());
    }

    private void update(Customer customer) {
        String sql = "UPDATE customer SET name = ?, email = ?, phone = ? WHERE id = ?";
        jdbcTemplate.update(sql, customer.name(), customer.email(), customer.phone(), customer.id());
    }

    @Override
    public void deleteById(UUID id) {
        jdbcTemplate.update("DELETE FROM customer WHERE id = ?", id);
    }

}