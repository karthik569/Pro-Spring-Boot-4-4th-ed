package com.apress.prospringboot4.management;

import com.apress.prospringboot4.management.service.RiskAssessmentService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Management gRPC Client Application.
 *
 * This application demonstrates Spring gRPC 1.0.1 client implementation
 * with Spring Boot 4, showcasing:
 * - @ImportGrpcClients for automatic stub registration
 * - Blocking stub injection
 * - gRPC client configuration via application.yml
 * - Virtual Threads support (Java 21)
 */
@SpringBootApplication
public class ManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManagementApplication.class, args);
    }

    /**
     * Demo bean to showcase gRPC client usage.
     */
    @Bean
    public CommandLineRunner demoGrpcClient(RiskAssessmentService riskAssessmentService) {
        return args -> {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("Management gRPC Client Demo");
            System.out.println("=".repeat(60) + "\n");

            // Test different customer IDs
            String[] customerIds = {"CUST-001", "CUST-002", "CUST-003", "CUST-999"};

            for (String customerId : customerIds) {
                try {
                    System.out.println("Assessing customer: " + customerId);
                    riskAssessmentService.assessCustomer(customerId);
                    System.out.println();
                } catch (Exception e) {
                    System.err.println("Error assessing customer " + customerId + ": " + e.getMessage());
                }
            }

            System.out.println("=".repeat(60));
            System.out.println("Demo Complete!");
            System.out.println("=".repeat(60) + "\n");
        };
    }
}
