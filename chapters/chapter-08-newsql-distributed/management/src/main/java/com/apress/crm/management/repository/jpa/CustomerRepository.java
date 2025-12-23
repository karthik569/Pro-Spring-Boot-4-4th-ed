package com.apress.crm.management.repository.jpa;

import com.apress.crm.management.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Page<Customer> findByLastName(String lastName, Pageable pageable);
    Customer findByEmail(String email);
}