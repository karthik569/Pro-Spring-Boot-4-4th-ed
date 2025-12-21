package com.apress.crm.management.repository;

import com.apress.crm.management.model.Company;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CompanyRepository implements Repository<Company, UUID> {

    private final JdbcClient jdbcClient;

    public CompanyRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Company save(Company entity) {
        UUID id = entity.companyId() != null ? entity.companyId() : UUID.randomUUID();
        Company companyToSave = new Company(id, entity.companyName(), entity.industry(), entity.website());

        int updated = jdbcClient.sql("UPDATE company SET company_name = ?, industry = ?, website = ? WHERE company_id = ?")
                .params(companyToSave.companyName(), companyToSave.industry(), companyToSave.website(), companyToSave.companyId())
                .update();

        if (updated == 0) {
            jdbcClient.sql("INSERT INTO company (company_id, company_name, industry, website) VALUES (?, ?, ?, ?)")
                    .params(companyToSave.companyId(), companyToSave.companyName(), companyToSave.industry(), companyToSave.website())
                    .update();
        }

        return companyToSave;
    }

    @Override
    public Company findById(UUID uuid) {
        return jdbcClient.sql("SELECT company_id, company_name, industry, website FROM company WHERE company_id = :id")
                .param("id", uuid)
                .query(Company.class)
                .optional()
                .orElse(null);
    }

    @Override
    public Iterable<Company> findAll() {
        return jdbcClient.sql("SELECT company_id, company_name, industry, website FROM company")
                .query(Company.class)
                .list();
    }

    @Override
    public void deleteById(UUID uuid) {
        jdbcClient.sql("DELETE FROM company WHERE company_id = :id")
                .param("id", uuid)
                .update();
    }
}