package com.apress.crm.management.messaging.pulsar;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.pulsar.core.PulsarTemplate;
import org.testcontainers.containers.PulsarContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(properties = {"spring.rsocket.server.port=0", "spring.pulsar.enabled=true"})
@Testcontainers
class PulsarTest {

    @Container
    @ServiceConnection
    static PulsarContainer pulsar = new PulsarContainer(DockerImageName.parse("apachepulsar/pulsar:3.0.0"));

    @Autowired
    private PulsarTemplate<String> pulsarTemplate;

    @Autowired
    private AuditConsumer consumer;

    @Test
    void testPulsarFlow() {
        pulsarTemplate.send("crm-audit-topic", "Login Action");

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
            assertThat(consumer.getMessages()).contains("Login Action")
        );
    }
}
