package com.apress.crm.management.repository;

import com.apress.crm.management.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    // Derived query
    List<Customer> findByLastName(String lastName);

    // @Query with JPQL
    @Query("SELECT c FROM Customer c WHERE c.email LIKE %:domain")
    List<Customer> findByEmailDomain(String domain);

    // Custom JPQL Query
    @Query("SELECT c FROM Customer c JOIN FETCH c.company WHERE c.email = :email")
    Customer findByEmailWithCompany(String email);

    // Pagination support
    Page<Customer> findByCompanyCompanyId(UUID companyId, Pageable pageable);
}