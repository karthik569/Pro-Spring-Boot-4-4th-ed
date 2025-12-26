package com.apress.crm.customer.messaging.pulsar;

import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuditProducer {

    private final PulsarTemplate<String> pulsarTemplate;

    public AuditProducer(PulsarTemplate<String> pulsarTemplate) {
        this.pulsarTemplate = pulsarTemplate;
    }

    public void logAction(String action, String user) {
        try {
            pulsarTemplate.send("crm-audit-topic", "User " + user + " performed " + action);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send audit log", e);
        }
    }
}
