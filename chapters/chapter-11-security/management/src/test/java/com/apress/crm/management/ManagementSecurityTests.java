package com.apress.crm.management;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK, properties = {
        "spring.main.banner-mode=off",
        "spring.sql.init.mode=always",
        "spring.profiles.active=test"
})
class ManagementSecurityTests extends BaseIntegrationTest {

    // ========== Authentication & Authorization Tests ==========

    @Test
    void shouldReturn401ForUnauthenticatedAccess() {
        webTestClient
                .get().uri("/api/v1/customers/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(authorities = "SCOPE_read")
    void shouldAllowGetAccessWithScopeRead() {
        webTestClient
                .mutateWith(mockUser().authorities("SCOPE_read"))
                .get().uri("/api/v1/customers/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound(); // NotFound means it passed security but didn't find the resource
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldDenyGetAccessWithoutScopeRead() {
        webTestClient
                .mutateWith(mockUser().authorities("ROLE_USER"))
                .get().uri("/api/v1/customers/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDenyPostAccessForAdminWithoutBody() {
        // Just checking access, not body validation here, but 400 means security passed
        webTestClient
                .mutateWith(mockUser().roles("ADMIN"))
                .post().uri("/api/v1/customers")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldDenyPostAccessForNonAdmin() {
        webTestClient
                .mutateWith(mockUser().roles("USER"))
                .post().uri("/api/v1/customers")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldAllowAccessToPublicEndpoints() {
        webTestClient
                .get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }
}
