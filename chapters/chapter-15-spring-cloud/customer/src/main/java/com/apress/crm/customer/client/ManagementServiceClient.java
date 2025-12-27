package com.apress.crm.customer.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Client for calling the management-service via service discovery.
 *
 * This client uses @LoadBalanced RestClient to automatically discover
 * and call healthy instances of the management-service registered in Consul.
 *
 * The base URL uses the service ID (management-service) instead of a hostname,
 * which gets resolved by the load balancer to an actual instance.
 */
@Component
public class ManagementServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ManagementServiceClient.class);

    private final RestClient restClient;

    public ManagementServiceClient(@LoadBalanced RestClient.Builder builder) {
        // The base URL uses the service ID from Consul, not a hostname
        this.restClient = builder
                .baseUrl("http://management-service/api/management")
                .build();
    }

    /**
     * Retrieves a company by ID from the management service.
     */
    public Map<String, Object> getCompanyById(UUID companyId) {
        log.info("Calling management-service to get company: {}", companyId);

        try {
            return restClient.get()
                    .uri("/companies/{id}", companyId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.error("Error calling management-service for company {}: {}",
                    companyId, e.getMessage());
            throw new RuntimeException("Failed to retrieve company from management service", e);
        }
    }

    /**
     * Retrieves all companies from the management service.
     */
    public List<Map<String, Object>> getAllCompanies() {
        log.info("Calling management-service to get all companies");

        try {
            return restClient.get()
                    .uri("/companies")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.error("Error calling management-service for all companies: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve companies from management service", e);
        }
    }

    /**
     * Retrieves an address by ID from the management service.
     */
    public Map<String, Object> getAddressById(UUID addressId) {
        log.info("Calling management-service to get address: {}", addressId);

        try {
            return restClient.get()
                    .uri("/addresses/{id}", addressId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.error("Error calling management-service for address {}: {}",
                    addressId, e.getMessage());
            throw new RuntimeException("Failed to retrieve address from management service", e);
        }
    }

    /**
     * Retrieves all addresses from the management service.
     */
    public List<Map<String, Object>> getAllAddresses() {
        log.info("Calling management-service to get all addresses");

        try {
            return restClient.get()
                    .uri("/addresses")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.error("Error calling management-service for all addresses: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve addresses from management service", e);
        }
    }
}
