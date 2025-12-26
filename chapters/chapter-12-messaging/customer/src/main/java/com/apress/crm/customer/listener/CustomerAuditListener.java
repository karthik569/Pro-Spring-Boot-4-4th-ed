package com.apress.crm.customer.listener;

import com.apress.crm.customer.event.CustomerCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class CustomerAuditListener {

    private static final Logger log = LoggerFactory.getLogger(CustomerAuditListener.class);

    @Async
    @EventListener
    public void onCustomerCreated(CustomerCreatedEvent event) {
        log.info("AUDIT: New customer created with ID: {}", event.customer().getId());
    }
}
