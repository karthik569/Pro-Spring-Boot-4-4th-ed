package com.apress.crm.management.repository;

import com.apress.crm.management.model.Customer;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CustomerRepository implements Repository<Customer, UUID> {

    private final JdbcClient jdbcClient;

    public CustomerRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Customer save(Customer entity) {
        UUID id = entity.customerId() != null ? entity.customerId() : UUID.randomUUID();
        Customer customerToSave = new Customer(id, entity.firstName(), entity.lastName(), entity.jobTitle(), entity.email(), entity.phone(), entity.companyId());

        int updated = jdbcClient.sql("UPDATE customer SET first_name = ?, last_name = ?, job_title = ?, email = ?, phone = ?, company_id = ? WHERE customer_id = ?")
                .params(customerToSave.firstName(), customerToSave.lastName(), customerToSave.jobTitle(), customerToSave.email(), customerToSave.phone(), customerToSave.companyId(), customerToSave.customerId())
                .update();

        if (updated == 0) {
            jdbcClient.sql("INSERT INTO customer (customer_id, first_name, last_name, job_title, email, phone, company_id) VALUES (?, ?, ?, ?, ?, ?, ?)")
                    .params(customerToSave.customerId(), customerToSave.firstName(), customerToSave.lastName(), customerToSave.jobTitle(), customerToSave.email(), customerToSave.phone(), customerToSave.companyId())
                    .update();
        }

        return customerToSave;
    }

    @Override
    public Customer findById(UUID uuid) {
        return jdbcClient.sql("SELECT customer_id, first_name, last_name, job_title, email, phone, company_id FROM customer WHERE customer_id = :id")
                .param("id", uuid)
                .query(Customer.class)
                .optional()
                .orElse(null);
    }

    @Override
    public Iterable<Customer> findAll() {
        return jdbcClient.sql("SELECT customer_id, first_name, last_name, job_title, email, phone, company_id FROM customer")
                .query(Customer.class)
                .list();
    }

    @Override
    public void deleteById(UUID uuid) {
        jdbcClient.sql("DELETE FROM customer WHERE customer_id = :id")
                .param("id", uuid)
                .update();
    }

    public void saveAll(Iterable<Customer> entities) {
        entities.forEach(this::save);
    }
}
