package com.apress.crm.shell;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Spring Shell commands for interacting with the CRM AI Assistant.
 *
 * These commands provide a conversational interface to the AI assistant
 * through the command line.
 */
@ShellComponent
public class AssistantCommands {

    private final RestClient restClient;
    private static final String BASE_URL = "http://localhost:8082/api/assistant";

    public AssistantCommands(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl(BASE_URL)
                .defaultHeader("Authorization", "Basic YWRtaW46YWRtaW4=")  // admin:admin
                .build();
    }

    /**
     * Basic chat with the AI assistant.
     *
     * Usage: chat "What is CRM?"
     */
    @ShellMethod(value = "Chat with the AI assistant", key = "chat")
    public String chat(@ShellOption(value = {"-m", "--message"}) String message) {
        try {
            Map<String, String> response = restClient.post()
                    .uri("/chat")
                    .body(Map.of("message", message))
                    .retrieve()
                    .body(Map.class);

            return formatResponse(response);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Ask about product features and pricing using RAG.
     *
     * Usage: products "What features are in the Professional plan?"
     */
    @ShellMethod(value = "Ask about product features and pricing", key = "products")
    public String products(@ShellOption(value = {"-m", "--message"}) String message) {
        try {
            Map<String, String> response = restClient.post()
                    .uri("/products")
                    .body(Map.of("message", message))
                    .retrieve()
                    .body(Map.class);

            return formatResponse(response);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Query the database using natural language.
     *
     * Usage: query "Show me all customers from California"
     */
    @ShellMethod(value = "Query the database using natural language", key = "query")
    public String query(@ShellOption(value = {"-m", "--message"}) String message) {
        try {
            Map<String, String> response = restClient.post()
                    .uri("/query")
                    .body(Map.of("message", message))
                    .retrieve()
                    .body(Map.class);

            return formatResponse(response);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Use function calling for customer operations.
     *
     * Usage: functions "Find customer with email john@example.com"
     */
    @ShellMethod(value = "Use AI functions for customer operations", key = "functions")
    public String functions(@ShellOption(value = {"-m", "--message"}) String message) {
        try {
            Map<String, String> response = restClient.post()
                    .uri("/functions")
                    .body(Map.of("message", message))
                    .retrieve()
                    .body(Map.class);

            return formatResponse(response);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Check if the assistant service is available.
     *
     * Usage: health
     */
    @ShellMethod(value = "Check assistant service health", key = "health")
    public String health() {
        try {
            Map<String, String> response = restClient.get()
                    .uri("/health")
                    .retrieve()
                    .body(Map.class);

            return "Assistant Service Status: " + response.get("status");
        } catch (Exception e) {
            return "Error: Assistant service is not available - " + e.getMessage();
        }
    }

    private String formatResponse(Map<String, String> response) {
        if (response == null) {
            return "No response received";
        }

        if (response.containsKey("error")) {
            return "Error: " + response.get("error");
        }

        return response.getOrDefault("response", "No response content");
    }
}
