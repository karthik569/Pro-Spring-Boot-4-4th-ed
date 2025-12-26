package com.apress.crm.management.messaging.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"spring.rsocket.server.port=0", "spring.kafka.enabled=true"})
@Testcontainers
class KafkaConsumerTest {

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private LegacyEventConsumer consumer;

    @Test
    void testKafkaConsumption() throws InterruptedException {
        // Send message to Kafka
        kafkaTemplate.send("legacy-system-events", "key-1", "Legacy Data Payload").join();

        // Wait for message to be consumed
        boolean messageReceived = consumer.getLatch().await(15, TimeUnit.SECONDS);
        assertThat(messageReceived).as("Kafka message should be received within 15 seconds").isTrue();
    }
}
