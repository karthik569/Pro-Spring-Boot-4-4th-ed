package com.apress.crm.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerServiceTest extends BaseTest {

    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void shouldSaveAndRetrieveCustomer() {
        Customer customer = new Customer("Test User", "test@example.com", "123-456-7890");
        Customer saved = customerService.save(customer);
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        
        Customer found = customerService.findById(saved.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Test User");
    }
}