package com.apress.crm.management.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("customers")
public record Customer(
        @Id UUID customerId,
        @NotBlank String firstName,
        @NotBlank String lastName,
        String jobTitle,
        @Email @NotBlank String email,
        String phone,
        UUID companyId) {
}
