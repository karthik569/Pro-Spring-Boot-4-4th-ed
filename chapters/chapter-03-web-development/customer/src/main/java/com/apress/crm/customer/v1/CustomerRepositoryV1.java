package com.apress.crm.customer.v1;

import org.springframework.stereotype.Repository;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CustomerRepositoryV1 {

    private final Map<UUID, CustomerV1> customers = new ConcurrentHashMap<>();

    public CustomerV1 save(CustomerV1 customer) {
        UUID id = customer.id() != null ? customer.id() : UUID.randomUUID();
        CustomerV1 toSave = new CustomerV1(id, customer.name(), customer.email());
        customers.put(id, toSave);
        return toSave;
    }

}