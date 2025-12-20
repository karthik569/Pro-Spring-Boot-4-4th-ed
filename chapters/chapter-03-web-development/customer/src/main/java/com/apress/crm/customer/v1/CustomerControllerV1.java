package com.apress.crm.customer.v1;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1_1/customers")
public class CustomerControllerV1 {

    private final CustomerRepositoryV1 repository;

    public CustomerControllerV1(CustomerRepositoryV1 repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<CustomerV1> create(@RequestBody CustomerV1 customer) {
        // Legacy behavior: accepts anything, minimal checks
        CustomerV1 saved = repository.save(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

}