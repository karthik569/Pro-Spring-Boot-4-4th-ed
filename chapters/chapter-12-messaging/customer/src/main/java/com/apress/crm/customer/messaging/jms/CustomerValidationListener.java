package com.apress.crm.customer.messaging.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CustomerValidationListener {

    private static final Logger log = LoggerFactory.getLogger(CustomerValidationListener.class);

    @JmsListener(destination = "customer.validation")
    public void validateCustomer(Map<String, String> payload) {
        log.info("Performing fraud check for customer: {}", payload.get("customerId"));
    }
}
