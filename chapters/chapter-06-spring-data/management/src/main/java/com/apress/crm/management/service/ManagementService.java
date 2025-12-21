package com.apress.crm.management.service;

import com.apress.crm.management.model.Address;
import com.apress.crm.management.model.Communication;
import com.apress.crm.management.model.Company;
import com.apress.crm.management.model.Customer;
import com.apress.crm.management.model.CustomerDetailsDTO;
import com.apress.crm.management.repository.AddressRepository;
import com.apress.crm.management.repository.CommunicationRepository;
import com.apress.crm.management.repository.CompanyRepository;
import com.apress.crm.management.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ManagementService {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final CommunicationRepository communicationRepository;
    private final CompanyRepository companyRepository;

    public ManagementService(CustomerRepository customerRepository, AddressRepository addressRepository,
                           CommunicationRepository communicationRepository, CompanyRepository companyRepository) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.communicationRepository = communicationRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public CustomerDetailsDTO createCustomerWithDetails(Customer customer, Company company, Address address, Communication communication) {
        Company savedCompany = this.companyRepository.save(company);
        
        // Update customer with saved company
        customer.setCompany(savedCompany);
        // We assume ID is generated if null, but if passed, we use it.
        // JPA usually ignores set ID for generated strategy unless we merge.
        // For UUID strategy, typically we let JPA handle it or set it if we want.
        // The entities have @GeneratedValue so we should pass null or let it generate.
        // The incoming 'customer' might have null ID.
        
        Customer customerToSave = new Customer(null, customer.getFirstName(), customer.getLastName(), customer.getJobTitle(), customer.getEmail(), customer.getPhone(), savedCompany);
        Customer savedCustomer = this.customerRepository.save(customerToSave);
        
        Address addressToSave = new Address(null, savedCustomer, address.getStreet(), address.getCity(), address.getState(), address.getZip());
        Address savedAddress = this.addressRepository.save(addressToSave);
        
        Communication communicationToSave = new Communication(null, savedCustomer, communication.getCommunicationType(), communication.getCommunicationValue());
        Communication savedCommunication = this.communicationRepository.save(communicationToSave);
        
        return new CustomerDetailsDTO(savedCustomer, savedCompany, List.of(savedAddress), List.of(savedCommunication));
    }

    @Transactional(readOnly = true)
    public CustomerDetailsDTO getCustomerDetails(UUID customerId) {
        Customer customer = this.customerRepository.findById(customerId).orElse(null);
        if (customer == null) {
            return null;
        }

        // Company is eager loaded or lazy loaded, but accessible via getCompany()
        Company company = customer.getCompany();
        
        List<Address> addresses = StreamSupport.stream(this.addressRepository.findByCustomerCustomerId(customerId).spliterator(), false)
                .collect(Collectors.toList());
        
        List<Communication> communications = StreamSupport.stream(this.communicationRepository.findByCustomerCustomerId(customerId).spliterator(), false)
                .collect(Collectors.toList());

        return new CustomerDetailsDTO(customer, company, addresses, communications);
    }

    public Page<Customer> getCustomersByCompany(UUID companyId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastName").ascending());
        return customerRepository.findByCompanyCompanyId(companyId, pageable);
    }
}
