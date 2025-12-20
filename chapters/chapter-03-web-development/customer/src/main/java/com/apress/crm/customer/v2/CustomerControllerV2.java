package com.apress.crm.customer.v2;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2_0/customers")
public class CustomerControllerV2 {

    private final CustomerRepositoryV2 repository;

    public CustomerControllerV2(CustomerRepositoryV2 repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<CustomerV2> create(@Valid @RequestBody CustomerV2 customer) {
        // Modern behavior: Strict validation handled by GlobalExceptionHandler
        CustomerV2 saved = repository.save(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

}