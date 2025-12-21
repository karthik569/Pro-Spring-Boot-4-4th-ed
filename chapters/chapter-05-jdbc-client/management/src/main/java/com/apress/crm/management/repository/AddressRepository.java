package com.apress.crm.management.repository;

import com.apress.crm.management.model.Address;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AddressRepository implements Repository<Address, UUID> {

    private final JdbcClient jdbcClient;

    public AddressRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Address save(Address entity) {
        UUID id = entity.addressId() != null ? entity.addressId() : UUID.randomUUID();
        Address addressToSave = new Address(id, entity.customerId(), entity.street(), entity.city(), entity.state(), entity.zip());

        int updated = jdbcClient.sql("UPDATE address SET customer_id = ?, street = ?, city = ?, state = ?, zip = ? WHERE address_id = ?")
                .params(addressToSave.customerId(), addressToSave.street(), addressToSave.city(), addressToSave.state(), addressToSave.zip(), addressToSave.addressId())
                .update();

        if (updated == 0) {
            jdbcClient.sql("INSERT INTO address (address_id, customer_id, street, city, state, zip) VALUES (?, ?, ?, ?, ?, ?)")
                    .params(addressToSave.addressId(), addressToSave.customerId(), addressToSave.street(), addressToSave.city(), addressToSave.state(), addressToSave.zip())
                    .update();
        }

        return addressToSave;
    }

    @Override
    public Address findById(UUID uuid) {
        return jdbcClient.sql("SELECT address_id, customer_id, street, city, state, zip FROM address WHERE address_id = :id")
                .param("id", uuid)
                .query(Address.class)
                .optional()
                .orElse(null);
    }

    @Override
    public Iterable<Address> findAll() {
        return jdbcClient.sql("SELECT address_id, customer_id, street, city, state, zip FROM address")
                .query(Address.class)
                .list();
    }

    @Override
    public void deleteById(UUID uuid) {
        jdbcClient.sql("DELETE FROM address WHERE address_id = :id")
                .param("id", uuid)
                .update();
    }

    public Iterable<Address> findAllByCustomerId(UUID customerId) {
        return jdbcClient.sql("SELECT address_id, customer_id, street, city, state, zip FROM address WHERE customer_id = :customerId")
                .param("customerId", customerId)
                .query(Address.class)
                .list();
    }
}