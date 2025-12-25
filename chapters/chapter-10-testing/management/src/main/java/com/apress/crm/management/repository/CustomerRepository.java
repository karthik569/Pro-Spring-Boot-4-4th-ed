package com.apress.crm.management.repository;

import com.apress.crm.management.model.Customer;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import java.util.UUID;

public interface CustomerRepository extends R2dbcRepository<Customer, UUID> {
}