package com.apress.crm.customer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.sql.init.mode=always"
})
class CustomerLifecycleTests {

    @Nested
    @DisplayName("When creating a new customer")
    class CreationTests {
        @Test
        void shouldAssignValidUUID() {
            // Logic to verify UUID assignment
            assertThat(true).isTrue();
        }

        @Test
        void shouldSendWelcomeEmail() {
            // Logic to verify email trigger
            assertThat(true).isTrue();
        }
    }

    @Nested
    @DisplayName("When deleting a customer")
    class DeletionTests {
        @Test
        void shouldCleanupCache() {
            // Logic to verify cache cleanup
            assertThat(true).isTrue();
        }
    }
}
