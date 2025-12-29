package com.apress.crm.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    java.util.Optional<Customer> findByEmail(String email);
    List<Customer> findByName(String name);
    List<Customer> findByEmailEndingWith(String domain);

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
    @Query(value = "SELECT * FROM customers AS OF SYSTEM TIME '-5s' WHERE name = :name", nativeQuery = true)
    List<Customer> findByNameAsOfSystemTime(@Param("name") String name);
}
