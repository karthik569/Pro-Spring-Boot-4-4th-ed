package com.apress.crm.customer.event;

import com.apress.crm.customer.Customer;
import java.time.Instant;

public record CustomerCreatedEvent(Customer customer, Instant timestamp) {}
