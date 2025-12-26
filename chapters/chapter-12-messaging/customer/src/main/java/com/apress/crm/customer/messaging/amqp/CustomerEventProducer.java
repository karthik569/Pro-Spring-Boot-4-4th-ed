package com.apress.crm.customer.messaging.amqp;

import com.apress.crm.customer.event.CustomerCreatedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.apress.crm.customer.config.AmqpConfig.EXCHANGE_NAME;

@Component
public class CustomerEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public CustomerEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendCustomerCreated(CustomerCreatedEvent event) {
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, "customer.created", event);
    }
}
