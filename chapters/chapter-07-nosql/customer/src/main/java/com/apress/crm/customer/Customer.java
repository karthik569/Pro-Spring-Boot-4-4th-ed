package com.apress.crm.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.VectorIndex;
import org.springframework.data.domain.Persistable;
import org.springframework.data.domain.Vector;

@Document("customers")
public record Customer(
        @Id String id,
        @NotBlank(message = "Name is required")
        String name,
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,
        String phone,
        Vector vector // Latest Spring Data 2025.1 Vector support
) implements Persistable<String> {

    public Customer(String name, String email, String phone) {
        this(null, name, email, phone, null);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return id == null;
    }
}