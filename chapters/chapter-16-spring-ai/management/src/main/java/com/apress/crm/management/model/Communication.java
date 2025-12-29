package com.apress.crm.management.model;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("communications")
public record Communication(
        @Id UUID communicationId,
        UUID customerId,
        @NotBlank String communicationType,
        @NotBlank String communicationValue
) {}
