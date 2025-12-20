package com.apress.crm.customer.v2;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CustomerV2(
        UUID id,

        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank @Email(message = "Invalid email")
        String email
) {}