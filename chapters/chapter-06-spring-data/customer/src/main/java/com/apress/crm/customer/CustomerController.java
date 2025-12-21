package com.apress.crm.customer;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerRepository repository;

    public CustomerController(CustomerRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    Iterable<Customer> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    Customer findById(@PathVariable UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Customer save(@RequestBody @jakarta.validation.Valid Customer customer) {
        return repository.save(customer);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteById(@PathVariable UUID id) {
        repository.deleteById(id);
    }
}
