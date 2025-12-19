package com.apress.crm.management.model;

import java.util.Objects;
import java.util.UUID;

public record Adress(
        UUID addressId,
        UUID customerId,
        String street,
        String city,
        String state,
        String zip
) {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Adress adress = (Adress) o;
        return Objects.equals(addressId, adress.addressId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(addressId);
    }
}
