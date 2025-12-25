package com.apress.crm.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CustomerRepository customerRepository;

    @org.springframework.test.context.bean.override.mockito.MockitoSpyBean
    private CustomerController customerControllerSpy;

    @Test
    void shouldReturnCustomerWithHamcrest() {
        UUID id = UUID.randomUUID();
        Customer mockCustomer = new Customer(id, "Felipe", "Gutierrez", "felipe@test.com", "555-1234", true);

        when(customerRepository.findById(any(UUID.class)))
                .thenReturn(Mono.just(mockCustomer));

        webTestClient.get().uri("/api/v1/customers/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.firstName").isEqualTo("Felipe")
                .jsonPath("$.email").value(containsString("@test.com"))
                .jsonPath("$.lastName").value(notNullValue());
    }
}
