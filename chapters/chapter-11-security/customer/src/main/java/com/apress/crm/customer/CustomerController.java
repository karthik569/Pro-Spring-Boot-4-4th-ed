package com.apress.crm.customer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    List<Customer> findAll() {
        return customerService.findAll();
    }

    @GetMapping("/{id}")
    Customer findById(@PathVariable UUID id) {
        return customerService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }

    @PostMapping
    @IsAdmin
    ResponseEntity<Customer> save(@RequestBody @jakarta.validation.Valid Customer customer) {
        return ResponseEntity.status(CREATED).body(customerService.save(customer));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteById(@PathVariable UUID id) {
        customerService.deleteById(id);
    }
}