package com.apress.crm.customer.integration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;
import org.springframework.integration.channel.DirectChannel;

@Configuration
public class FeedbackIntegrationConfig {

    @Bean
    public MessageChannel feedbackInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow processFeedbackFlow() {
        return IntegrationFlow.from("feedbackInputChannel")
                .filter(String.class, source -> !source.isEmpty()) // Filter empty feedback
                .transform(String.class, String::trim)             // Normalize: Trim whitespace
                .handle("feedbackProcessor", "process")            // Service Activator
                .get();
    }

    @Bean
    public FeedbackProcessor feedbackProcessor() {
        return new FeedbackProcessor();
    }

    public static class FeedbackProcessor {
        public void process(String feedback) {
            System.out.println("Processing customer feedback: " + feedback);
        }
    }
}
