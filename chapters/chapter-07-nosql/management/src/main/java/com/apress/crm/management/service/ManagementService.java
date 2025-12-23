package com.apress.crm.management.service;

import com.apress.crm.management.model.*;
import com.apress.crm.management.repository.jpa.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class ManagementService {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final CommunicationRepository communicationRepository;
    private final CompanyRepository companyRepository;
    private final ReactiveRedisOperations<String, CustomerSession> sessionOps;

    public ManagementService(CustomerRepository customerRepository, 
                           AddressRepository addressRepository,
                           CommunicationRepository communicationRepository, 
                           CompanyRepository companyRepository,
                           ReactiveRedisOperations<String, CustomerSession> sessionOps) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.communicationRepository = communicationRepository;
        this.companyRepository = companyRepository;
        this.sessionOps = sessionOps;
    }

    @Transactional
    public CustomerDetailsDTO createCustomerWithDetails(Customer customer, Company company, Address address, Communication communication) {
        Company savedCompany = this.companyRepository.save(company);
        
        customer.setCompany(savedCompany);
        Customer savedCustomer = this.customerRepository.save(customer);
        
        address.setCustomerId(savedCustomer.getCustomerId());
        Address savedAddress = this.addressRepository.save(address);
        
        communication.setCustomerId(savedCustomer.getCustomerId());
        Communication savedCommunication = this.communicationRepository.save(communicationToSave(savedCustomer.getCustomerId(), communication));
        
        return new CustomerDetailsDTO(savedCustomer, savedCompany, List.of(savedAddress), List.of(savedCommunication));
    }

    private Communication communicationToSave(UUID customerId, Communication communication) {
        return new Communication(null, customerId, communication.getType(), communication.getCommValue());
    }

    @Transactional(readOnly = true)
    public CustomerDetailsDTO getCustomerDetails(UUID customerId) {
        Customer customer = this.customerRepository.findById(customerId).orElse(null);
        if (customer == null) {
            return null;
        }

        Company company = customer.getCompany();
        List<Address> addresses = this.addressRepository.findByCustomerId(customerId);
        List<Communication> communications = this.communicationRepository.findByCustomerId(customerId);

        return new CustomerDetailsDTO(customer, company, addresses, communications);
    }

    // Listing 7-7: Reactive method from the book
    @Transactional(readOnly = true)
    public Mono<CustomerDetailsDTO> getCustomerWithSession(UUID customerId) {
        return Mono.fromCallable(() -> getCustomerDetails(customerId))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(details -> {
                    if (details == null) return Mono.empty();
                    String key = "session:" + customerId;
                    CustomerSession session = new CustomerSession(key, customerId, "View Details");
                    return sessionOps.opsForValue()
                            .set(key, session, Duration.ofMinutes(30))
                            .thenReturn(details);
                });
    }

    public Page<Customer> getCustomersByLastName(String lastName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastName").ascending());
        return customerRepository.findByLastName(lastName, pageable);
    }

    // Redis: Session management - non-blocking
    public Mono<Boolean> startSession(UUID customerId) {
        return sessionOps.opsForValue().set("session:" + customerId, new CustomerSession("session:" + customerId, customerId, "Login"), Duration.ofMinutes(30));
    }

    public Mono<CustomerSession> getSession(UUID customerId) {
        return sessionOps.opsForValue().get("session:" + customerId);
    }
}
