package com.apress.crm.customer.messaging.amqp;

import com.apress.crm.customer.config.AmqpConfig;
import com.apress.crm.customer.Customer;
import com.apress.crm.customer.event.CustomerCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class RabbitMqIntegrationTest {

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:management-alpine");

    @Autowired
    private CustomerEventProducer producer;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void testSendAndReceive() {
        CustomerCreatedEvent event = new CustomerCreatedEvent(
            new Customer(null, "Rabbit", "rabbit@test.com"), Instant.now());

        producer.sendCustomerCreated(event);

        // Receive directly from the queue to verify delivery
        CustomerCreatedEvent received = (CustomerCreatedEvent) rabbitTemplate.receiveAndConvert(AmqpConfig.QUEUE_NAME);
        assertThat(received).isNotNull();
        assertThat(received.customer().getName()).isEqualTo("Rabbit");
    }
}
