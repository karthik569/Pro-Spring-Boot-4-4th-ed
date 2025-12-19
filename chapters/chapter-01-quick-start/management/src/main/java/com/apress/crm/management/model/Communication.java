package com.apress.crm.management.model;

import java.util.Objects;
import java.util.UUID;

public record Communication(
        UUID communicationId,
        UUID customer,
        String communicationType,
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
