package com.apress.crm.customer.messaging.rabbitmq;

public record CustomerActivity(String customerId, String activity) {}
