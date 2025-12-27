package com.apress.crm.management.dto;

import java.util.UUID;

/**
 * DTO for customer events received from customer-service via RabbitMQ.
 * This matches the structure of the Customer entity in customer-service.
 */
public record CustomerEvent(
        UUID id,
        String name,
        String email,
        String phone
) {}
