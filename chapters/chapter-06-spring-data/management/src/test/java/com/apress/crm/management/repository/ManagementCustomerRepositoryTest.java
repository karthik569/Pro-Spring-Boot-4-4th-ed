package com.apress.crm.management.repository;

import com.apress.crm.management.model.Company;
import com.apress.crm.management.model.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ManagementCustomerRepositoryTest {

    @Autowired
    private CustomerRepository repository;

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    void findByIdShouldReturnCustomer() {
        // Given
        Customer customer = new Customer(null, "Sundar", "Pichai", "CEO",
                "sundar@google.com", "111-222-3333", null);
        Customer saved = repository.save(customer);

        // When
        Optional<Customer> result = repository.findById(saved.getCustomerId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("Sundar");
        assertThat(result.get().getJobTitle()).isEqualTo("CEO");
    }

    @Test
    void shouldFindAllCustomers() {
        // Given
        repository.save(new Customer(null, "John", "Doe", "Dev", "john@example.com", "123", null));
        repository.save(new Customer(null, "Jane", "Doe", "Mgr", "jane@example.com", "456", null));

        // When
        Iterable<Customer> customers = repository.findAll();

        // Then
        assertThat(customers).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldFindByLastName() {
        // Given
        repository.save(new Customer(null, "Felipe", "Gutierrez", "Author", "felipe@example.com", "123", null));

        // When
        List<Customer> result = repository.findByLastName("Gutierrez");

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getFirstName()).isEqualTo("Felipe");
    }

    @Test
    void shouldFindByEmailDomain() {
        // Given
        repository.save(new Customer(null, "Josh", "Long", "Advocate", "josh@spring.io", "123", null));

        // When
        List<Customer> result = repository.findByEmailDomain("spring.io");

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getFirstName()).isEqualTo("Josh");
    }

    @Test
    void shouldFindByEmailWithCompany() {
        // Given
        Company company = companyRepository.save(new Company(null, "Google", "Tech", "google.com"));
        repository.save(new Customer(null, "Sundar", "Pichai", "CEO", "sundar@google.com", "111", company));

        // When
        Customer result = repository.findByEmailWithCompany("sundar@google.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCompany()).isNotNull();
        assertThat(result.getCompany().getCompanyName()).isEqualTo("Google");
    }

    @Test
    void shouldFindByCompanyCompanyIdWithPagination() {
        // Given
        Company company = companyRepository.save(new Company(null, "Apress", "Publishing", "apress.com"));
        repository.save(new Customer(null, "User 1", "Test", "Dev", "user1@apress.com", "1", company));
        repository.save(new Customer(null, "User 2", "Test", "Dev", "user2@apress.com", "2", company));
        repository.save(new Customer(null, "User 3", "Test", "Dev", "user3@apress.com", "3", company));

        // When
        Page<Customer> page = repository.findByCompanyCompanyId(company.getCompanyId(), PageRequest.of(0, 2));

        // Then
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
    }
}