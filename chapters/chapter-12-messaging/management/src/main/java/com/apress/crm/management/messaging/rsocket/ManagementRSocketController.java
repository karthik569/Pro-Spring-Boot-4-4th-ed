package com.apress.crm.management.messaging.rsocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Controller
public class ManagementRSocketController {

    @MessageMapping("system.status.stream")
    public Flux<String> streamStatus() {
        return Flux.interval(Duration.ofSeconds(1))
                   .map(i -> "System OK - Tick " + i);
    }
}
