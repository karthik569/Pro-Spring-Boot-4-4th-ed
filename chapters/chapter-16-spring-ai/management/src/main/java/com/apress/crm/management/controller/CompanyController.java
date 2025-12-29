package com.apress.crm.management.controller;

import com.apress.crm.management.model.Company;
import com.apress.crm.management.repository.CompanyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST Controller for Company management.
 *
 * This controller provides endpoints for retrieving company information
 * that can be called by other microservices (e.g., customer-service).
 */
@RestController
@RequestMapping("/api/management/companies")
public class CompanyController {

    private final CompanyRepository companyRepository;

    public CompanyController(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @GetMapping
    public Flux<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Company>> getCompanyById(@PathVariable UUID id) {
        return companyRepository.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Company>> createCompany(@RequestBody Company company) {
        return companyRepository.save(company)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Company>> updateCompany(@PathVariable UUID id,
                                                       @RequestBody Company company) {
        return companyRepository.findById(id)
                .flatMap(existing -> {
                    // Records are immutable, create a new instance with updated values
                    Company updated = new Company(
                            existing.companyId(),
                            company.companyName(),
                            company.industry(),
                            company.website()
                    );
                    return companyRepository.save(updated);
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteCompany(@PathVariable UUID id) {
        return companyRepository.deleteById(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
