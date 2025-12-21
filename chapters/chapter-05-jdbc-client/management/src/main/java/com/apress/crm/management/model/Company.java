package com.apress.crm.management.model;

import jakarta.validation.constraints.NotBlank;
import java.util.Objects;
import java.util.UUID;

public record Company(
        UUID companyId,
        @NotBlank(message = "Company name is required")
        String companyName,
        String industry,
        String website
) {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Company company = (Company) o;
        return Objects.equals(companyId, company.companyId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(companyId);
    }
}