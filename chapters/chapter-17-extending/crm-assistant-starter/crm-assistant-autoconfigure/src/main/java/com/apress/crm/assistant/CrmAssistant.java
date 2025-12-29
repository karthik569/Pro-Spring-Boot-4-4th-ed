package com.apress.crm.assistant;

/**
 * AI-powered CRM Assistant interface.
 *
 * This interface defines the contract for the CRM assistant that provides:
 * - Basic conversational AI capabilities
 * - RAG (Retrieval-Augmented Generation) for product knowledge
 * - Natural language to SQL database queries
 * - Function calling for customer operations
 */
public interface CrmAssistant {

    /**
     * Basic chat interaction with the assistant.
     *
     * @param userMessage the user's question or request
     * @return the assistant's response
     */
    String chat(String userMessage);

    /**
     * Chat with RAG (Retrieval-Augmented Generation) for product knowledge.
     * Retrieves relevant product documentation from the vector store before generating a response.
     *
     * @param userMessage the user's question about products
     * @return the assistant's response augmented with product knowledge
     */
    String chatWithProductKnowledge(String userMessage);

    /**
     * Execute a natural language database query.
     * Converts the user's natural language request into a SQL query and executes it.
     *
     * @param userMessage the user's natural language query (e.g., "show me all customers from California")
     * @return the query results formatted as a string
     */
    String queryDatabase(String userMessage);

    /**
     * Chat with function calling enabled for customer operations.
     * The assistant can call functions to perform operations like:
     * - Searching for customers
     * - Getting customer details
     * - Updating customer information
     *
     * @param userMessage the user's request
     * @return the assistant's response after executing any necessary functions
     */
    String chatWithFunctions(String userMessage);
}
