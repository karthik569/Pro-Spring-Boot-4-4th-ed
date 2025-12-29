package com.apress.crm.assistant.config;

import com.apress.crm.assistant.CrmAssistant;
import com.apress.crm.assistant.client.CustomerClient;
import com.apress.crm.assistant.impl.DefaultCrmAssistant;
import com.apress.crm.assistant.service.DatabaseAgentService;
import com.apress.crm.assistant.service.RAGService;
import com.apress.crm.assistant.tool.CustomerTools;
import com.apress.crm.assistant.tool.R2dbcQueryTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Auto-configuration for the CRM AI Assistant.
 *
 * This configuration class sets up all the necessary beans for the AI assistant:
 * - HTTP client for customer service
 * - Function callbacks for AI function calling
 * - Tools for database queries and customer operations
 * - The main CrmAssistant service
 *
 * Conditional Behaviors:
 * - Only activates if Spring AI ChatClient is on the classpath
 * - Can be disabled via crm.assistant.enabled=false
 * - Allows user overrides via @ConditionalOnMissingBean
 */
@AutoConfiguration
@ConditionalOnClass(ChatClient.class)
@EnableConfigurationProperties(CrmAssistantProperties.class)
@ConditionalOnProperty(prefix = "crm.assistant", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CrmAssistantAutoConfiguration {

    /**
     * Configure the HTTP client for calling the customer service.
     * Uses service discovery to locate the customer service.
     */
    @Bean
    @ConditionalOnMissingBean
    public CustomerClient customerClient(WebClient.Builder webClientBuilder) {
        // Create WebClient for customer service
        WebClient webClient = webClientBuilder
                .baseUrl("http://customer-service")  // Uses service discovery
                .defaultHeader("Authorization", "Basic YWRtaW46YWRtaW4=")  // admin:admin
                .build();

        // Create HTTP Service Proxy
        WebClientAdapter adapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(CustomerClient.class);
    }

    /**
     * R2DBC Query Tool bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public R2dbcQueryTool r2dbcQueryTool(DatabaseClient databaseClient, ObjectMapper objectMapper) {
        return new R2dbcQueryTool(databaseClient, objectMapper);
    }

    /**
     * Customer Tools bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public CustomerTools customerTools(CustomerClient customerClient, ObjectMapper objectMapper) {
        return new CustomerTools(customerClient, objectMapper);
    }

    /**
     * RAG Service bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public RAGService ragService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        return new RAGService(chatClientBuilder, vectorStore);
    }

    /**
     * Database Agent Service bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public DatabaseAgentService databaseAgentService(ChatClient.Builder chatClientBuilder, R2dbcQueryTool r2dbcQueryTool) {
        return new DatabaseAgentService(chatClientBuilder, r2dbcQueryTool);
    }

    /**
     * Main CRM Assistant bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public CrmAssistant crmAssistant(
            ChatClient.Builder chatClientBuilder,
            RAGService ragService,
            DatabaseAgentService databaseAgentService,
            CustomerTools customerTools
    ) {
        return new DefaultCrmAssistant(chatClientBuilder, ragService, databaseAgentService, customerTools);
    }

    /**
     * Function callback for searching customers by email.
     */
    @Bean
    @Description("Search for customers by email address. Returns customer information including name, phone, and ID.")
    public FunctionCallback searchCustomerByEmailFunction(CustomerTools customerTools) {
        return FunctionCallback.builder()
                .function("searchCustomerByEmail", customerTools.searchCustomerByEmail())
                .description("Search for customers by email address. Returns customer information including name, phone, and ID.")
                .inputType(CustomerTools.SearchByEmailRequest.class)
                .build();
    }

    /**
     * Function callback for getting a customer by ID.
     */
    @Bean
    @Description("Get detailed information about a specific customer by their ID. Returns all customer fields.")
    public FunctionCallback getCustomerByIdFunction(CustomerTools customerTools) {
        return FunctionCallback.builder()
                .function("getCustomerById", customerTools.getCustomerById())
                .description("Get detailed information about a specific customer by their ID. Returns all customer fields.")
                .inputType(CustomerTools.GetCustomerRequest.class)
                .build();
    }

    /**
     * Function callback for getting all customers.
     */
    @Bean
    @Description("Get a list of all customers in the system. Use this when the user wants to see all customers.")
    public FunctionCallback getAllCustomersFunction(CustomerTools customerTools) {
        return FunctionCallback.builder()
                .function("getAllCustomers", customerTools.getAllCustomers())
                .description("Get a list of all customers in the system. Use this when the user wants to see all customers.")
                .build();
    }

    /**
     * Function callback for executing database queries.
     */
    @Bean
    @Description("Execute a SQL SELECT query against the database. Only SELECT queries are allowed. Returns query results as JSON.")
    public FunctionCallback executeQueryFunction(R2dbcQueryTool r2dbcQueryTool) {
        return FunctionCallback.builder()
                .function("executeQuery", r2dbcQueryTool.executeQuery())
                .description("Execute a SQL SELECT query against the database. Only SELECT queries are allowed. Returns query results as JSON.")
                .inputType(R2dbcQueryTool.QueryRequest.class)
                .build();
    }

    /**
     * Function callback for getting database schema.
     */
    @Bean
    @Description("Get the database schema information including table names, column names, and data types. Use this to understand what data is available.")
    public FunctionCallback getSchemaFunction(R2dbcQueryTool r2dbcQueryTool) {
        return FunctionCallback.builder()
                .function("getSchema", r2dbcQueryTool.getSchema())
                .description("Get the database schema information including table names, column names, and data types. Use this to understand what data is available.")
                .build();
    }
}
