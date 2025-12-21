package com.apress.crm.management.model;

import jakarta.validation.constraints.NotBlank;
import java.util.Objects;
import java.util.UUID;

public record Communication(
        UUID communicationId,
        UUID customerId,
        @NotBlank(message = "Communication type is required")
        String communicationType,
        @NotBlank(message = "Communication value is required")
        String communicationValue
) {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Communication that = (Communication) o;
        return Objects.equals(communicationId, that.communicationId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(communicationId);
    }
}