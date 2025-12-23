package com.apress.crm.management.service;

import com.apress.crm.management.model.*;
import com.apress.crm.management.repository.jpa.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Service
public class ManagementService {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final CommunicationRepository communicationRepository;
    private final CompanyRepository companyRepository;

    public ManagementService(CustomerRepository customerRepository, 
                           AddressRepository addressRepository,
                           CommunicationRepository communicationRepository, 
                           CompanyRepository companyRepository) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.communicationRepository = communicationRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional
    @Retryable(retryFor = SQLException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @CacheEvict(value = "customers", allEntries = true)
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
    @Cacheable(value = "customers", key = "#customerId")
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

    public Page<Customer> getCustomersByLastName(String lastName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastName").ascending());
        return customerRepository.findByLastName(lastName, pageable);
    }
}
