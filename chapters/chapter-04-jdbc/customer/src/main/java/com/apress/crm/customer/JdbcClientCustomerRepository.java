package com.apress.crm.customer;

import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.UUID;

public class JdbcClientCustomerRepository implements Repository<Customer, UUID> {

    private final JdbcClient jdbcClient;

    public JdbcClientCustomerRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Iterable<Customer> findAll() {
        return jdbcClient.sql("SELECT * FROM customer")
                .query(Customer.class) // (1)
                .list();
    }

    @Override
    public Customer findById(UUID id) {
        return jdbcClient.sql("SELECT * FROM customer WHERE id = :id")
                .param("id", id) // (2)
                .query(Customer.class)
                .optional()
                .orElse(null);
    }

    @Override
    public Customer save(Customer customer) {
        // If ID is null, generate one
        if (customer.id() == null) {
            Customer newCustomer = new Customer(UUID.randomUUID(), customer.name(), customer.email(), customer.phone());
            create(newCustomer);
            return newCustomer;
        }

        if (findById(customer.id()) != null) {
            update(customer);
            return customer;
        }

        create(customer);
        return customer;
    }

    private void create(Customer customer) {
        jdbcClient.sql("INSERT INTO customer (id, name, email, phone) VALUES (:id, :name, :email, :phone)")
                .param("id", customer.id())
                .param("name", customer.name())
                .param("email", customer.email())
                .param("phone", customer.phone())
                .update();
    }

    private void update(Customer customer) {
        jdbcClient.sql("UPDATE customer SET name = :name, email = :email, phone = :phone WHERE id = :id")
                .param("name", customer.name())
                .param("email", customer.email())
                .param("phone", customer.phone())
                .param("id", customer.id())
                .update();
    }

    @Override
    public void deleteById(UUID id) {
        jdbcClient.sql("DELETE FROM customer WHERE id = :id")
                .param("id", id)
                .update();
    }

}