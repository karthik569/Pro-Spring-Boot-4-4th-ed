package com.apress.crm.management.model;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("addresses")
public record Address(
        @Id UUID addressId,
        UUID customerId,
        @NotBlank String street,
        @NotBlank String city,
        @NotBlank String state,
        @NotBlank String zip
) {}
