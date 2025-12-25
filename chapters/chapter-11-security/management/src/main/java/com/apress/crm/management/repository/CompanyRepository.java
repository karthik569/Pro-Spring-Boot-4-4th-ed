package com.apress.crm.management.repository;

import com.apress.crm.management.model.Company;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import java.util.UUID;

public interface CompanyRepository extends R2dbcRepository<Company, UUID> {
}