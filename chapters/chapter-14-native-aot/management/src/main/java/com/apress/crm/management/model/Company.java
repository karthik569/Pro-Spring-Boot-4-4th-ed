package com.apress.crm.management.model;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("companies")
public record Company(
        @Id UUID companyId,
        @NotBlank String companyName,
        @NotBlank String industry,
        String website
) {}
