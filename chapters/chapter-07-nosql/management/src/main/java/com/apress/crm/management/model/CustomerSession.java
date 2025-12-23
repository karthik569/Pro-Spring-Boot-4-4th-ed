package com.apress.crm.management.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import java.io.Serializable;
import java.util.UUID;

@RedisHash("sessions")
public record CustomerSession(
        @Id String sessionId,
        UUID customerId,
        String lastAction
) implements Serializable {
}