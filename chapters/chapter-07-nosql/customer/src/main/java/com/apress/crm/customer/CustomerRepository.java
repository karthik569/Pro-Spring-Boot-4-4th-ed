package com.apress.crm.customer;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.VectorSearch;
import org.springframework.data.domain.Vector;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {
    
    List<Customer> findByName(String name);
    List<Customer> findByEmailEndingWith(String domain);

    // New Vector Search support in Spring Data 2025.1
    @VectorSearch(indexName = "vector_index")
    List<Customer> findByVectorNear(Vector vector, Limit limit);
}
