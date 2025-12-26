package com.apress.crm.customer.messaging.jms;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class CustomerJmsTest {

    @Container
    static GenericContainer<?> artemis = new GenericContainer<>("apache/activemq-artemis:latest-alpine")
            .withExposedPorts(61616);

    @DynamicPropertySource
    static void artemisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.artemis.broker-url",
            () -> "tcp://" + artemis.getHost() + ":" + artemis.getMappedPort(61616));
        registry.add("spring.artemis.user", () -> "artemis");
        registry.add("spring.artemis.password", () -> "artemis");
    }

    @Autowired
    private CustomerValidationProducer producer;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Test
    void testValidationWorkflow() {
        producer.requestValidation("CUST-123");

        // Verify message landed on the queue using JmsTemplate to read it back
        Object payload = jmsTemplate.receiveAndConvert("customer.validation");
        assertThat(payload).asString().contains("CUST-123");
    }
}
