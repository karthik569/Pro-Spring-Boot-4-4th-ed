package com.apress.crm.management.model;

import java.util.Objects;
import java.util.UUID;

public record Customer(
        UUID customerId,
        String firtName,
        String lastName,
        String JobTitle,
        String email,
        String phone,
        UUID Company) {

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(customerId, customer.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(customerId);
    }
}
