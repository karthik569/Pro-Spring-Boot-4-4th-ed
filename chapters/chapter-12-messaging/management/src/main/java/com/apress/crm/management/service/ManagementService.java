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
import io.r2dbc.spi.R2dbcException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ManagementService {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final CommunicationRepository communicationRepository;
    private final CompanyRepository companyRepository;
    
    private final Map<UUID, CustomerDetailsDTO> cache = new ConcurrentHashMap<>();

    public ManagementService(CustomerRepository customerRepository, AddressRepository addressRepository,
                           CommunicationRepository communicationRepository, CompanyRepository companyRepository) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.communicationRepository = communicationRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public Mono<CustomerDetailsDTO> createCustomerWithDetails(Customer customer, Company company, Address address, Communication communication) {
        return companyRepository.save(company)
                .flatMap(savedCompany -> {
                    Customer c = new Customer(null, customer.firstName(), customer.lastName(), customer.jobTitle(), customer.email(), customer.phone(), savedCompany.companyId());
                    return customerRepository.save(c)
                            .flatMap(savedCustomer -> {
                                Address a = new Address(null, savedCustomer.customerId(), address.street(), address.city(), address.state(), address.zip());
                                Communication comm = new Communication(null, savedCustomer.customerId(), communication.communicationType(), communication.communicationValue());
                                
                                return Mono.zip(
                                        addressRepository.save(a),
                                        communicationRepository.save(comm)
                                ).map(tuple -> new CustomerDetailsDTO(savedCustomer, savedCompany, List.of(tuple.getT1()), List.of(tuple.getT2())));
                            });
                })
                .doOnNext(dto -> cache.put(dto.customer().customerId(), dto))
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                        .filter(throwable -> throwable instanceof R2dbcException && 
                                "40001".equals(((R2dbcException) throwable).getSqlState())));
    }

    public Mono<Customer> saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public Mono<Void> triggerBackgroundSync(UUID customerId) {
        // Simulates a background operation
        return Mono.empty();
    }

    public Mono<CustomerDetailsDTO> getCustomerDetails(UUID customerId) {
        return Mono.justOrEmpty(cache.get(customerId))
                .switchIfEmpty(
                        customerRepository.findById(customerId)
                                .flatMap(customer -> companyRepository.findById(customer.companyId())
                                        .zipWith(Mono.just(customer)))
                                .flatMap(tuple -> {
                                    Company company = tuple.getT1();
                                    Customer customer = tuple.getT2();
                                    return Mono.zip(
                                            addressRepository.findAllByCustomerId(customerId).collectList(),
                                            communicationRepository.findAllByCustomerId(customerId).collectList()
                                    ).map(details -> new CustomerDetailsDTO(customer, company, details.getT1(), details.getT2()));
                                })
                                .doOnNext(dto -> cache.put(customerId, dto))
                );
    }
}