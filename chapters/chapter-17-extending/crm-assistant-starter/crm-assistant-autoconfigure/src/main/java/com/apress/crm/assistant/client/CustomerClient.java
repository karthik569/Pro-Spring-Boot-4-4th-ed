package com.apress.crm.assistant.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * HTTP Exchange client for the Customer Service.
 *
 * This interface uses Spring's declarative HTTP client feature to communicate
 * with the customer service. The implementation is auto-generated at runtime.
 *
 * Used by the AI assistant's function calling feature to perform customer operations.
 */
@HttpExchange(url = "/api/customers")
public interface CustomerClient {

    /**
     * Get all customers.
     *
     * @return list of all customers
     */
    @GetExchange
    List<Map<String, Object>> getAllCustomers();

    /**
     * Get a customer by ID.
     *
     * @param id the customer ID
     * @return the customer details
     */
    @GetExchange("/{id}")
    Map<String, Object> getCustomerById(@PathVariable UUID id);

    /**
     * Search customers by email.
     *
     * @param email the email to search for
     * @return list of matching customers
     */
    @GetExchange("/search")
    List<Map<String, Object>> searchByEmail(@RequestParam String email);

    /**
     * Update a customer.
     *
     * @param id the customer ID
     * @param customer the updated customer data
     * @return the updated customer
     */
    @PutExchange("/{id}")
    Map<String, Object> updateCustomer(@PathVariable UUID id, @RequestBody Map<String, Object> customer);
}
