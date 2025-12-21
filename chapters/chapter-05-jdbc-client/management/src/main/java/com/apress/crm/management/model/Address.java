package com.apress.crm.management.model;

import jakarta.validation.constraints.NotBlank;
import java.util.Objects;
import java.util.UUID;

public record Address(
        UUID addressId,
        UUID customerId,
        @NotBlank(message = "Street is required")
        String street,
        @NotBlank(message = "City is required")
        String city,
        @NotBlank(message = "State is required")
        String state,
        @NotBlank(message = "Zip is required")
        String zip
) {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(addressId, address.addressId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(addressId);
    }
}
