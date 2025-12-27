package com.apress.crm.management.controller;

import com.apress.crm.management.model.Address;
import com.apress.crm.management.repository.AddressRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST Controller for Address management.
 *
 * This controller provides endpoints for retrieving address information
 * that can be called by other microservices (e.g., customer-service).
 */
@RestController
@RequestMapping("/api/management/addresses")
public class AddressController {

    private final AddressRepository addressRepository;

    public AddressController(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @GetMapping
    public Flux<Address> getAllAddresses() {
        return addressRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Address>> getAddressById(@PathVariable UUID id) {
        return addressRepository.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Address>> createAddress(@RequestBody Address address) {
        return addressRepository.save(address)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Address>> updateAddress(@PathVariable UUID id,
                                                       @RequestBody Address address) {
        return addressRepository.findById(id)
                .flatMap(existing -> {
                    // Records are immutable, create a new instance with updated values
                    Address updated = new Address(
                            existing.addressId(),
                            existing.customerId(),
                            address.street(),
                            address.city(),
                            address.state(),
                            address.zip()
                    );
                    return addressRepository.save(updated);
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteAddress(@PathVariable UUID id) {
        return addressRepository.deleteById(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
