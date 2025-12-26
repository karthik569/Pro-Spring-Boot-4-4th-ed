package com.apress.crm.management.messaging.rsocket;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.rsocket.server.port=0"})
class RSocketTest {

    @Autowired
    private RSocketRequester.Builder builder;

    @Value("${local.rsocket.server.port}")
    private Integer port;

    @Test
    void testStream() {
        RSocketRequester requester = builder.tcp("localhost", port);

        Flux<String> stream = requester
                .route("system.status.stream")
                .retrieveFlux(String.class)
                .take(3);

        StepVerifier.create(stream)
                .expectNext("System OK - Tick 0")
                .expectNext("System OK - Tick 1")
                .expectNext("System OK - Tick 2")
                .verifyComplete();
    }
}
