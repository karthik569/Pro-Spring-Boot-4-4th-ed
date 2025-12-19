package com.apress.crm.management.repository;

import com.apress.crm.management.model.Adress;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class AddressRepository implements Repository<Adress, UUID> {

    private final Map<UUID, Adress> addresses = new ConcurrentHashMap<>();

    @Override
    public Adress save(Adress entity) {
        if (entity.addressId() == null) {
            UUID uuid = UUID.randomUUID();
            entity = new Adress(uuid, entity.customerId(), entity.street(), entity.city(), entity.state(), entity.zip());
        }
        this.addresses.put(entity.addressId(), entity);
        return entity;
    }

    @Override
    public Adress findById(UUID uuid) {
        return this.addresses.get(uuid);
    }

    @Override
    public Iterable<Adress> findAll() {
        return this.addresses.values();
    }

    @Override
    public void deleteById(UUID uuid) {
        this.addresses.remove(uuid);
    }

    public Iterable<Adress> findAllByCustomerId(UUID customerId) {
        return this.addresses.values().stream()
                .filter(address -> address.customerId().equals(customerId))
                .collect(Collectors.toList());
    }
}
