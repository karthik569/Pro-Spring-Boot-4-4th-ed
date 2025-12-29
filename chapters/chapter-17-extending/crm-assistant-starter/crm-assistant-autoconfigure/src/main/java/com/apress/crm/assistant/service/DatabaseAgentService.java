package com.apress.crm.assistant.service;

import com.apress.crm.assistant.tool.R2dbcQueryTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;

/**
 * AI Agent service for natural language to SQL queries.
 *
 * This service acts as an intelligent agent that can:
 * 1. Understand natural language questions about the database
 * 2. Retrieve the database schema
 * 3. Generate appropriate SQL queries
 * 4. Execute the queries
 * 5. Format and present the results
 *
 * The agent uses function calling to interact with the database through R2dbcQueryTool.
 */
public class DatabaseAgentService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseAgentService.class);

    private final ChatClient chatClient;

    private static final String SYSTEM_MESSAGE = """
            You are a helpful database assistant for a CRM (Customer Relationship Management) system.

            Your role is to help users query the CRM database using natural language.

            The database contains the following tables:
            - companies: Stores company information (company_id, company_name, industry, website)
            - addresses: Stores address information (address_id, street, city, state, zip)
            - customers: Stores customer information (customer_id, first_name, last_name, email, phone, company_id, address_id)

            When a user asks a question:
            1. First, call getSchema() to understand the current database schema
            2. Analyze the question and determine what SQL query is needed
            3. Generate a valid PostgreSQL SELECT query
            4. Call executeQuery() with the SQL query
            5. Format the results in a user-friendly way

            Important guidelines:
            - Only generate SELECT queries (no INSERT, UPDATE, DELETE for safety)
            - Use proper JOINs when data from multiple tables is needed
            - Handle null values gracefully
            - If the query returns no results, explain that clearly
            - If the question is ambiguous, ask for clarification
            - Format results in a clear, readable format (e.g., tables or bullet points)

            Example queries:
            - "Show me all customers" -> SELECT * FROM customers
            - "Find customers from California" -> SELECT * FROM customers WHERE state = 'CA'
            - "List all companies in the technology industry" -> SELECT * FROM companies WHERE industry = 'Technology'
            """;

    public DatabaseAgentService(ChatClient.Builder chatClientBuilder, R2dbcQueryTool r2dbcQueryTool) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_MESSAGE)
                .defaultFunctions("executeQuery", "getSchema")
                .build();
    }

    /**
     * Process a natural language database query.
     *
     * @param userQuestion the user's question in natural language
     * @return the query results formatted as a user-friendly response
     */
    public String query(String userQuestion) {
        log.info("Processing database query: {}", userQuestion);

        String response = chatClient.prompt()
                .user(userQuestion)
                .call()
                .content();

        log.info("Database query completed successfully");
        return response;
    }
}
