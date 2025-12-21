package com.apress.crm.management.repository;

import com.apress.crm.management.model.Address;
import com.apress.crm.management.model.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ManagementAddressRepositoryTest {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void shouldFindByCityWithCustomer() {
        // Given
        Customer customer = customerRepository.save(new Customer(null, "John", "Doe", "Dev", "john@example.com", "123", null));
        addressRepository.save(new Address(null, customer, "123 Main St", "TestCity", "NY", "10001"));

        // When
        List<Address> result = addressRepository.findByCityWithCustomer("TestCity");

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getCustomer()).isNotNull();
        assertThat(result.get(0).getCustomer().getFirstName()).isEqualTo("John");
    }
}
