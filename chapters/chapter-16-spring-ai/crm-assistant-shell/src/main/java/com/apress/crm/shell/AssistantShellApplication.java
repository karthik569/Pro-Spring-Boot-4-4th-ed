package com.apress.crm.shell;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CRM AI Assistant Shell Application.
 *
 * This is a Spring Shell-based command-line interface for interacting
 * with the CRM AI Assistant.
 *
 * Users can type natural language commands to:
 * - Chat with the AI assistant
 * - Query product information
 * - Search for customers
 * - Query the database
 *
 * Commands:
 * - chat: Basic conversation with the assistant
 * - products: Ask about product features and pricing
 * - query: Natural language database queries
 * - functions: Use function calling for customer operations
 */
@SpringBootApplication
public class AssistantShellApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssistantShellApplication.class, args);
    }
}
