package com.apress.crm.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("customers")
public record Customer(
        @Id UUID id,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email @NotBlank String email,
        String phone,
        @Transient boolean isNew) implements Persistable<UUID> {

    public Customer(String firstName, String lastName, String email, String phone) {
        this(UUID.randomUUID(), firstName, lastName, email, phone, true);
    }

    @PersistenceCreator
    public Customer(UUID id, String firstName, String lastName, String email, String phone) {
        this(id, firstName, lastName, email, phone, false);
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}