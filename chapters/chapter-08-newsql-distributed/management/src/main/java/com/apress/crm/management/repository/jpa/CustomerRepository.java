package com.apress.crm.management.repository.jpa;

import com.apress.crm.management.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Page<Customer> findByLastName(String lastName, Pageable pageable);
    Customer findByEmail(String email);

    /**
     * CockroachDB Follower Reads - Read data as of 5 seconds ago.
     * This allows reading from follower replicas, reducing latency in geo-distributed setups.
     * The "-5s" interval ensures we read committed data that has propagated to followers.
     */
    @Query(value = "SELECT * FROM customers AS OF SYSTEM TIME '-5s'", nativeQuery = true)
    List<Customer> findAllAsOfSystemTime();

    /**
     * CockroachDB Follower Reads with a parameterized query.
     * Demonstrates AS OF SYSTEM TIME works with WHERE clauses.
     */
    @Query(value = "SELECT * FROM customers AS OF SYSTEM TIME '-5s' WHERE last_name = :lastName", nativeQuery = true)
    List<Customer> findByLastNameAsOfSystemTime(@Param("lastName") String lastName);
}