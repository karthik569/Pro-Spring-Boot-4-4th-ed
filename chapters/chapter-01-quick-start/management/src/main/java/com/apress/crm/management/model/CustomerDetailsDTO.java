package com.apress.crm.management.model;

import java.util.List;

public record CustomerDetailsDTO(
        Customer customer,
        Company company,
        List<Adress> addresses,
        List<Communication> communications
) {
}
