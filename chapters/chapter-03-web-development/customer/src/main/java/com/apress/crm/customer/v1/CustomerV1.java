package com.apress.crm.customer.v1;

import java.util.UUID;

public record CustomerV1(UUID id, String name, String email) {
}