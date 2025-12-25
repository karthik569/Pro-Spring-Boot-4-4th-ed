package com.apress.crm.customer;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.UUID;

class CustomerSecurityTests extends BaseTest {

    // ========== Authentication & Authorization Tests ==========

    @Test
    void shouldRedirectToLoginForUnauthenticatedAccess() {
        restTestClient.get().uri("/api/v1/customers/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isFound(); // 302 Redirect to /login
    }

    @Test
    @WithMockUser(authorities = "SCOPE_read")
    void shouldAllowGetAccessWithScopeRead() {
        restTestClient.get().uri("/api/v1/customers/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldDenyGetAccessWithoutScopeRead() {
        restTestClient.get().uri("/api/v1/customers/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "SCOPE_read"})
    void shouldAllowPostAccessForAdmin() {
        Customer customer = new Customer("Admin User", "admin@example.com", "555-0000", "password");
        restTestClient.post().uri("/api/v1/customers")
                .body(customer)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldDenyPostAccessForNonAdmin() {
        Customer customer = new Customer("Regular User", "user@example.com", "555-0000", "password");
        restTestClient.post().uri("/api/v1/customers")
                .body(customer)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldDenyDeleteAccessForNonAdminAtServiceLayer() {
        // The controller allows it, but the service layer @IsAdmin should block it
        restTestClient.delete().uri("/api/v1/customers/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isForbidden();
    }
}
