package com.apress.crm.customer.v2;

import org.springframework.stereotype.Repository;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CustomerRepositoryV2 {

    private final Map<UUID, CustomerV2> customers = new ConcurrentHashMap<>();

    public CustomerV2 save(CustomerV2 customer) {
        UUID id = customer.id() != null ? customer.id() : UUID.randomUUID();
        CustomerV2 toSave = new CustomerV2(id, customer.title(), customer.name(), customer.email());
        customers.put(id, toSave);
        return toSave;
    }

}