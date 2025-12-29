package com.apress.crm.assistant.tool;

import com.apress.crm.assistant.client.CustomerClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * AI Function Tools for customer operations.
 *
 * These functions are exposed to the LLM and can be automatically called
 * when the AI determines they are needed to answer the user's question.
 *
 * Each function is annotated with @Description to help the AI understand
 * when and how to use it.
 */
public class CustomerTools {

    private static final Logger log = LoggerFactory.getLogger(CustomerTools.class);

    private final CustomerClient customerClient;
    private final ObjectMapper objectMapper;

    public CustomerTools(CustomerClient customerClient, ObjectMapper objectMapper) {
        this.customerClient = customerClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Search for customers by email address.
     *
     * This function is called by the AI when the user asks to find a customer by email.
     */
    public Function<SearchByEmailRequest, String> searchCustomerByEmail() {
        return request -> {
            log.info("AI is calling searchCustomerByEmail with email: {}", request.email());
            try {
                List<Map<String, Object>> customers = customerClient.searchByEmail(request.email());
                String result = objectMapper.writeValueAsString(customers);
                log.info("Found {} customers", customers.size());
                return result;
            } catch (JsonProcessingException e) {
                log.error("Error serializing customer data", e);
                return "Error searching for customer: " + e.getMessage();
            } catch (Exception e) {
                log.error("Error calling customer service", e);
                return "Error communicating with customer service: " + e.getMessage();
            }
        };
    }

    /**
     * Get customer details by ID.
     *
     * This function is called by the AI when the user asks for specific customer details.
     */
    public Function<GetCustomerRequest, String> getCustomerById() {
        return request -> {
            log.info("AI is calling getCustomerById with ID: {}", request.customerId());
            try {
                UUID customerId = UUID.fromString(request.customerId());
                Map<String, Object> customer = customerClient.getCustomerById(customerId);
                String result = objectMapper.writeValueAsString(customer);
                log.info("Retrieved customer: {}", customer.get("name"));
                return result;
            } catch (IllegalArgumentException e) {
                log.error("Invalid UUID format: {}", request.customerId());
                return "Invalid customer ID format: " + request.customerId();
            } catch (JsonProcessingException e) {
                log.error("Error serializing customer data", e);
                return "Error retrieving customer: " + e.getMessage();
            } catch (Exception e) {
                log.error("Error calling customer service", e);
                return "Customer not found or service unavailable";
            }
        };
    }

    /**
     * Get all customers.
     *
     * This function is called by the AI when the user asks to see all customers.
     */
    public Function<Void, String> getAllCustomers() {
        return unused -> {
            log.info("AI is calling getAllCustomers");
            try {
                List<Map<String, Object>> customers = customerClient.getAllCustomers();
                String result = objectMapper.writeValueAsString(customers);
                log.info("Retrieved {} customers", customers.size());
                return result;
            } catch (JsonProcessingException e) {
                log.error("Error serializing customers data", e);
                return "Error retrieving customers: " + e.getMessage();
            } catch (Exception e) {
                log.error("Error calling customer service", e);
                return "Error communicating with customer service: " + e.getMessage();
            }
        };
    }

    /**
     * Request object for searching customers by email.
     */
    public record SearchByEmailRequest(String email) {
    }

    /**
     * Request object for getting a customer by ID.
     */
    public record GetCustomerRequest(String customerId) {
    }
}
