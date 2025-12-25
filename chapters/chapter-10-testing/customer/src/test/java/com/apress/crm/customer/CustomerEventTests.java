package com.apress.crm.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RecordApplicationEvents
class CustomerEventTests {

    @Autowired
    private ApplicationEvents events;

    @Autowired
    private EventService eventService;

    @Test
    void shouldPublishEventWhenCustomerCreated() {
        eventService.publishCustomerCreated(UUID.randomUUID());

        assertThat(events.stream(CustomerCreatedEvent.class)).hasSize(1);
    }

    @TestConfiguration
    static class Config {
        @Bean
        EventService eventService(ApplicationEventPublisher publisher) {
            return new EventService(publisher);
        }
    }

    static class EventService {
        private final ApplicationEventPublisher publisher;

        EventService(ApplicationEventPublisher publisher) {
            this.publisher = publisher;
        }

        void publishCustomerCreated(UUID id) {
            publisher.publishEvent(new CustomerCreatedEvent(id));
        }
    }

    record CustomerCreatedEvent(UUID id) {}
}
