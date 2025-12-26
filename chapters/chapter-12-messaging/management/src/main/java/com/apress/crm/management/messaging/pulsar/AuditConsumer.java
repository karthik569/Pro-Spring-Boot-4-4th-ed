package com.apress.crm.management.messaging.pulsar;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "spring.pulsar.enabled", matchIfMissing = false)
public class AuditConsumer {

    private final List<String> messages = new ArrayList<>();

    @PulsarListener(subscriptionName = "management-audit-sub", topics = "crm-audit-topic")
    public void listen(String message) {
        System.out.println("AUDIT LOG RECEIVED: " + message);
        messages.add(message);
    }

    public List<String> getMessages() {
        return messages;
    }
}
