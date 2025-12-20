package com.apress.crm.customer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CustomerRepository implements Repository<Customer, UUID>{
    private static final Logger log = LoggerFactory.getLogger(CustomerRepository.class);
    Map<UUID,Customer> customers = new ConcurrentHashMap<>();

    @Override
    public Customer save(Customer entity) {
        log.debug("Saving customer: {}", entity.name());
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