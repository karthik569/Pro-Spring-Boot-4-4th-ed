package com.apress.crm.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CustomerJsonTest {

    @Autowired
    private JacksonTester<Customer> json;

    @Test
    void shouldSerializeCustomer() throws IOException {
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");
        Customer customer = new Customer(id, "Felipe", "Gutierrez", "felipe@test.com", "555-1234", true);

        assertThat(json.write(customer)).hasJsonPathStringValue("@.firstName", "Felipe");
        assertThat(json.write(customer)).hasJsonPathStringValue("@.lastName", "Gutierrez");
        assertThat(json.write(customer)).hasJsonPathStringValue("@.email", "felipe@test.com");
    }

    @Test
    void shouldDeserializeCustomer() throws IOException {
        String content = "{\"id\":\"00000000-0000-0000-0000-000000000001\",\"firstName\":\"Felipe\",\"lastName\":\"Gutierrez\",\"email\":\"felipe@test.com\",\"phone\":\"555-1234\",\"isNew\":true}";
        
        Customer customer = json.parseObject(content);
        assertThat(customer.firstName()).isEqualTo("Felipe");
        assertThat(customer.lastName()).isEqualTo("Gutierrez");
        assertThat(customer.email()).isEqualTo("felipe@test.com");
    }
}
