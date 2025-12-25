package com.apress.crm.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CockroachContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.util.UUID;

@DataR2dbcTest(properties = {
        "spring.sql.init.mode=always"
})
@Testcontainers
class CustomerRepositoryTest {

    @Container
    static CockroachContainer cockroach = new CockroachContainer("cockroachdb/cockroach:v25.4.0");

    @DynamicPropertySource
    static void r2dbcProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> String.format("r2dbc:postgresql://%s:%d/defaultdb",
                cockroach.getHost(), cockroach.getMappedPort(26257)));
        registry.add("spring.r2dbc.username", cockroach::getUsername);
        registry.add("spring.r2dbc.password", cockroach::getPassword);
    }

    @Autowired
    private CustomerRepository repository;

    @Test
    void shouldSaveAndFindCustomer() {
        Customer customer = new Customer("Test", "User", "test@test.com", "555-0000");

        repository.save(customer)
                .as(StepVerifier::create)
                .expectNextMatches(saved -> saved.id() != null)
                .verifyComplete();

        repository.findAll()
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }
}
