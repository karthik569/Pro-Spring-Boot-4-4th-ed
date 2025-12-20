package com.apress.crm.customer;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record Customer(UUID id,
                       @JsonProperty("full_name")
                       String name,
                       String email,
                       String phone) {
    public Customer(String name, String email, String phone) {
        this(UUID.randomUUID(), name, email, phone);
    }
}