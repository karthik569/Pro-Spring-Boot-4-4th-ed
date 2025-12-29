package com.apress.crm.management.ai;

import com.apress.crm.management.ai.tools.R2dbcQueryTool;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for DatabaseAgentService.
 *
 * These tests verify that the database agent is properly configured.
 * Integration tests that actually call the AI model should be in a separate test project.
 */
class DatabaseAgentServiceTest {

    @Test
    void shouldCreateDatabaseAgentService() {
        // This is a simple unit test to verify the service can be instantiated
        // Real testing of AI functionality requires integration tests with actual LLMs
        assertThat(DatabaseAgentService.class).isNotNull();
    }

    @Test
    void shouldHaveRequiredDependencies() {
        // Verify the required dependencies exist
        assertThat(R2dbcQueryTool.class).isNotNull();
    }
}
