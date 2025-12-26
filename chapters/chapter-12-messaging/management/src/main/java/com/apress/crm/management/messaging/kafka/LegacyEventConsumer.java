package com.apress.crm.management.messaging.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
@ConditionalOnProperty(name = "spring.kafka.enabled", matchIfMissing = false)
public class LegacyEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(LegacyEventConsumer.class);
    private CountDownLatch latch = new CountDownLatch(1);

    @KafkaListener(topics = "legacy-system-events", groupId = "management-group")
    public void listen(ConsumerRecord<String, String> record) {
        log.info("Received Kafka Message: Key={}, Value={}", record.key(), record.value());
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public void resetLatch() {
        latch = new CountDownLatch(1);
    }
}
