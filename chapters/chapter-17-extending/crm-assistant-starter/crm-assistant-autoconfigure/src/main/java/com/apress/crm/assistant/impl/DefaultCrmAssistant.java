package com.apress.crm.assistant.impl;

import com.apress.crm.assistant.CrmAssistant;
import com.apress.crm.assistant.service.DatabaseAgentService;
import com.apress.crm.assistant.service.RAGService;
import com.apress.crm.assistant.tool.CustomerTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * Default implementation of the CRM AI Assistant.
 *
 * This implementation coordinates all the AI capabilities:
 * - Basic chat with conversation memory
 * - RAG for product knowledge
 * - Natural language database queries
 * - Function calling for customer operations
 */
public class DefaultCrmAssistant implements CrmAssistant {

    private static final Logger log = LoggerFactory.getLogger(DefaultCrmAssistant.class);

    private final ChatClient basicChatClient;
    private final ChatClient functionChatClient;
    private final RAGService ragService;
    private final DatabaseAgentService databaseAgentService;

    private static final String CRM_SYSTEM_MESSAGE = """
            You are a helpful AI assistant for a CRM (Customer Relationship Management) system.

            Your role is to help users with:
            - General questions about CRM concepts
            - Information about customers, companies, and addresses
            - Product information and pricing
            - Database queries
            - Customer operations

            Always be professional, helpful, and concise in your responses.
            If you don't know something, say so honestly.
            """;

    private static final String FUNCTION_SYSTEM_MESSAGE = """
            You are a helpful AI assistant for a CRM system with access to customer data.

            You have access to the following functions:
            - searchCustomerByEmail: Search for customers by email address
            - getCustomerById: Get detailed information about a specific customer
            - getAllCustomers: Get a list of all customers

            When a user asks about customers, determine which function to call and use it.
            Format the results in a clear, user-friendly way.

            Important: Only call functions when necessary. Don't call functions for general questions.
            """;

    public DefaultCrmAssistant(
            ChatClient.Builder chatClientBuilder,
            RAGService ragService,
            DatabaseAgentService databaseAgentService,
            CustomerTools customerTools
    ) {
        this.ragService = ragService;
        this.databaseAgentService = databaseAgentService;

        // Basic chat client with conversation memory
        this.basicChatClient = chatClientBuilder
                .defaultSystem(CRM_SYSTEM_MESSAGE)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(new InMemoryChatMemory())
                )
                .build();

        // Function calling client with customer tools
        this.functionChatClient = chatClientBuilder
                .defaultSystem(FUNCTION_SYSTEM_MESSAGE)
                .defaultFunctions(
                        "searchCustomerByEmail",
                        "getCustomerById",
                        "getAllCustomers"
                )
                .build();
    }

    @Override
    public String chat(String userMessage) {
        log.info("Basic chat: {}", userMessage);

        String response = basicChatClient.prompt()
                .user(userMessage)
                .advisors(advisorSpec -> advisorSpec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, "default")
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10)
                )
                .call()
                .content();

        log.info("Chat response generated");
        return response;
    }

    @Override
    public String chatWithProductKnowledge(String userMessage) {
        log.info("RAG chat: {}", userMessage);

        String systemMessage = """
                You are a helpful AI assistant for a CRM system with deep knowledge about our products.

                Use the provided product documentation to answer the user's question accurately.
                If the documentation doesn't contain the answer, say so honestly.
                Always cite specific features, pricing, or details from the documentation when available.
                """;

        String response = ragService.answerQuestion(userMessage, systemMessage);
        log.info("RAG response generated");
        return response;
    }

    @Override
    public String queryDatabase(String userMessage) {
        log.info("Database query: {}", userMessage);

        String response = databaseAgentService.query(userMessage);
        log.info("Database query completed");
        return response;
    }

    @Override
    public String chatWithFunctions(String userMessage) {
        log.info("Function calling chat: {}", userMessage);

        String response = functionChatClient.prompt()
                .user(userMessage)
                .call()
                .content();

        log.info("Function calling response generated");
        return response;
    }
}
