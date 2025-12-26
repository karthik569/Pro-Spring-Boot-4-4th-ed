package com.apress.crm.customer.messaging.jms;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CustomerValidationProducer {

    private final JmsTemplate jmsTemplate;

    public CustomerValidationProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void requestValidation(String customerId) {
        jmsTemplate.convertAndSend("customer.validation",
            Map.of("customerId", customerId, "checkType", "FRAUD_DETECTION"));
    }
}
