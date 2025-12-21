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
        Customer customerToSave = new Customer(null, customer.firstName(), customer.lastName(), customer.jobTitle(), customer.email(), customer.phone(), savedCompany.companyId());
        Customer savedCustomer = this.customerRepository.save(customerToSave);
        Address addressToSave = new Address(null, savedCustomer.customerId(), address.street(), address.city(), address.state(), address.zip());
        Address savedAddress = this.addressRepository.save(addressToSave);
        Communication communicationToSave = new Communication(null, savedCustomer.customerId(), communication.communicationType(), communication.communicationValue());
        Communication savedCommunication = this.communicationRepository.save(communicationToSave);
        return new CustomerDetailsDTO(savedCustomer, savedCompany, List.of(savedAddress), List.of(savedCommunication));
    }

    @Transactional(readOnly = true)
    public CustomerDetailsDTO getCustomerDetails(UUID customerId) {
        Customer customer = this.customerRepository.findById(customerId);
        if (customer == null) {
            return null;
        }

        Company company = this.companyRepository.findById(customer.companyId());
        
        List<Address> addresses = StreamSupport.stream(this.addressRepository.findAllByCustomerId(customerId).spliterator(), false)
                .collect(Collectors.toList());
        
        List<Communication> communications = StreamSupport.stream(this.communicationRepository.findAllByCustomerId(customerId).spliterator(), false)
                .collect(Collectors.toList());

        return new CustomerDetailsDTO(customer, company, addresses, communications);
    }
}