package com.apress.crm.customer.service;

import com.apress.crm.customer.Customer;
import com.apress.crm.customer.CustomerService;
import com.apress.crm.customer.event.CustomerCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RecordApplicationEvents
class CustomerServiceEventTest {

    @Autowired
    private ApplicationEvents events;

    @Autowired
    private CustomerService customerService;

    @Test
    void testCustomerCreatedEventIsPublished() {
        customerService.save(new Customer(null, "John Doe", "john@example.com"));

        long count = events.stream(CustomerCreatedEvent.class).count();
        assertThat(count).isEqualTo(1);
    }
}
