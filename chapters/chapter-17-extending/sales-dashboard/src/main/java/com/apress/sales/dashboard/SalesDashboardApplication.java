package com.apress.sales.dashboard;

import com.apress.crm.assistant.CrmAssistant;
import com.apress.crm.assistant.config.EnableCrmAssistant;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Sales Dashboard Application demonstrating the CRM Assistant Starter.
 *
 * This application showcases how easy it is to add AI capabilities
 * to a Spring Boot application using our custom starter:
 *
 * 1. Add the starter dependency
 * 2. Add @EnableCrmAssistant annotation
 * 3. Inject and use CrmAssistant
 *
 * No complex configuration needed - everything is auto-configured!
 */
@SpringBootApplication
@EnableCrmAssistant
public class SalesDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalesDashboardApplication.class, args);
    }

    /**
     * Demo bean that shows the CRM Assistant in action.
     *
     * This CommandLineRunner demonstrates all four capabilities:
     * 1. Basic chat
     * 2. Product knowledge (RAG)
     * 3. Database queries
     * 4. Function calling
     */
    @Bean
    public CommandLineRunner demoAssistant(CrmAssistant crmAssistant) {
        return args -> {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("Sales Dashboard AI Assistant Ready!");
            System.out.println("=".repeat(60) + "\n");

            // Demo 1: Basic Chat
            System.out.println("1. Basic Chat Demo:");
            System.out.println("-".repeat(60));
            String chatResponse = crmAssistant.chat("What is a CRM system?");
            System.out.println("Q: What is a CRM system?");
            System.out.println("A: " + chatResponse);
            System.out.println();

            // Demo 2: Product Knowledge (RAG)
            System.out.println("2. Product Knowledge Demo (RAG):");
            System.out.println("-".repeat(60));
            String productResponse = crmAssistant.chatWithProductKnowledge(
                    "What features are included in the Professional plan?"
            );
            System.out.println("Q: What features are included in the Professional plan?");
            System.out.println("A: " + productResponse);
            System.out.println();

            // Demo 3: Database Query
            System.out.println("3. Database Query Demo:");
            System.out.println("-".repeat(60));
            try {
                String dbResponse = crmAssistant.queryDatabase("Show me all companies");
                System.out.println("Q: Show me all companies");
                System.out.println("A: " + dbResponse);
            } catch (Exception e) {
                System.out.println("Note: Database query requires a running database.");
                System.out.println("Error: " + e.getMessage());
            }
            System.out.println();

            // Demo 4: Function Calling
            System.out.println("4. Function Calling Demo:");
            System.out.println("-".repeat(60));
            try {
                String functionResponse = crmAssistant.chatWithFunctions(
                        "Do we have any customers in the system?"
                );
                System.out.println("Q: Do we have any customers in the system?");
                System.out.println("A: " + functionResponse);
            } catch (Exception e) {
                System.out.println("Note: Function calling requires customer service running.");
                System.out.println("Error: " + e.getMessage());
            }
            System.out.println();

            System.out.println("=".repeat(60));
            System.out.println("Demo Complete!");
            System.out.println("=".repeat(60) + "\n");
        };
    }
}
