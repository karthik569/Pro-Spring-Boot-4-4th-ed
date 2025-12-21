package com.apress.crm.customer;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerRepository extends ListCrudRepository<Customer, UUID> {

    // Derived query
    List<Customer> findByName(String name);

    // Native SQL Query (JDBC requires native SQL)
    @Query("SELECT * FROM CUSTOMER WHERE EMAIL LIKE :domain")
    List<Customer> findByEmailEndingWith(String domain);
}