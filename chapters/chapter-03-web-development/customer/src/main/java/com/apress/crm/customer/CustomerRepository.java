package com.apress.crm.customer;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CustomerRepository implements Repository<Customer, UUID>{
    Map<UUID,Customer> customers = new ConcurrentHashMap<>();

    @Override
    public Customer save(Customer entity) {
        Customer c = entity;
        if (c.id() == null) {
            c = new Customer(UUID.randomUUID(), entity.name(), entity.email(), entity.phone());
        }
        customers.put(c.id(), c);
        return c;
    }

    @Override
    public Customer findById(UUID id) {
        return customers.get(id);
    }

    @Override
    public Iterable<Customer> findAll() {
        return customers.values();
    }

    @Override
    public void deleteById(UUID id) {
        customers.remove(id);
    }
}