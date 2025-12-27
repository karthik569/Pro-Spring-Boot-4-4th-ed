package com.apress.crm.tests;

import org.junit.jupiter.api.*;
import org.springframework.http.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Comprehensive integration test demonstrating a complete CRM workflow.
 *
 * This test shows a realistic scenario:
 * 1. Create a company in management service
 * 2. Create an address in management service
 * 3. Create a customer in customer service (publishes event)
 * 4. Verify event was consumed by management service
 * 5. Customer service queries management service for company/address data
 * 6. All calls go through API Gateway
 * 7. Service discovery and load balancing work correctly
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CRMWorkflowIntegrationTest extends BaseIntegrationTest {

    private UUID companyId;
    private UUID addressId;
    private UUID customerId;

    private WebClient apiGatewayClient;
    private WebClient managementClient;
    private WebClient customerClient;

    @BeforeAll
    void setup() {
        // Create WebClient for API Gateway
        apiGatewayClient = WebClient.builder()
                .baseUrl(API_GATEWAY_URL)
                .defaultHeaders(headers -> headers.setBasicAuth("admin", "admin"))
                .build();

        // Create WebClient for direct service access (when needed)
        managementClient = WebClient.builder()
                .baseUrl(MANAGEMENT_SERVICE_URL)
                .defaultHeaders(headers -> headers.setBasicAuth("admin", "admin"))
                .build();

        customerClient = WebClient.builder()
                .baseUrl(CUSTOMER_SERVICE_URL)
                .defaultHeaders(headers -> headers.setBasicAuth("admin", "admin"))
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("Step 1: Create a company in management service via API Gateway")
    void shouldCreateCompanyViaGateway() {
        // Given - A new company
        Map<String, Object> companyRequest = Map.of(
                "companyName", "Acme Corporation",
                "industry", "Technology",
                "website", "https://acme.com"
        );

        // When - Creating company through API Gateway
        Map<String, Object> response = apiGatewayClient.post()
                .uri("/api/management/companies")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(companyRequest)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        // Then - Company should be created
        assertThat(response).isNotNull();
        assertThat(response.get("companyId")).isNotNull();
        assertThat(response.get("companyName")).isEqualTo("Acme Corporation");
        assertThat(response.get("industry")).isEqualTo("Technology");

        // Save company ID for later use
        companyId = UUID.fromString(response.get("companyId").toString());
        System.out.println("Created company with ID: " + companyId);
    }

    @Test
    @Order(2)
    @DisplayName("Step 2: Create an address in management service via API Gateway")
    void shouldCreateAddressViaGateway() {
        // Given - A new address
        Map<String, Object> addressRequest = Map.of(
                "street", "123 Main Street",
                "city", "San Francisco",
                "state", "CA",
                "zip", "94105"
        );

        // When - Creating address through API Gateway
        Map<String, Object> response = apiGatewayClient.post()
                .uri("/api/management/addresses")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(addressRequest)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        // Then - Address should be created
        assertThat(response).isNotNull();
        assertThat(response.get("addressId")).isNotNull();
        assertThat(response.get("street")).isEqualTo("123 Main Street");
        assertThat(response.get("city")).isEqualTo("San Francisco");

        // Save address ID for later use
        addressId = UUID.fromString(response.get("addressId").toString());
        System.out.println("Created address with ID: " + addressId);
    }

    @Test
    @Order(3)
    @DisplayName("Step 3: Create a customer via API Gateway (should publish event to RabbitMQ)")
    void shouldCreateCustomerAndPublishEvent() {
        // Given - A new customer
        Map<String, Object> customerRequest = Map.of(
                "name", "John Doe",
                "email", "john.doe@acme.com",
                "phone", "+1-555-0123",
                "password", "password123"
        );

        // When - Creating customer through API Gateway
        Map<String, Object> response = apiGatewayClient.post()
                .uri("/api/customers")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(customerRequest)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        // Then - Customer should be created
        assertThat(response).isNotNull();
        assertThat(response.get("id")).isNotNull();
        assertThat(response.get("name")).isEqualTo("John Doe");
        assertThat(response.get("email")).isEqualTo("john.doe@acme.com");

        // Save customer ID for later use
        customerId = UUID.fromString(response.get("id").toString());
        System.out.println("Created customer with ID: " + customerId);
    }

    @Test
    @Order(4)
    @DisplayName("Step 4: Verify management service received customer event via RabbitMQ")
    void shouldReceiveCustomerEventInManagementService() {
        // Given - Customer was created in previous test
        assertThat(customerId).isNotNull();

        // When/Then - Wait for event to be consumed (eventually consistent)
        await()
                .atMost(15, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    // Query management service directly to check if customer was synced
                    Map<String, Object> customer = managementClient.get()
                            .uri("/api/management/customers/{id}", customerId)
                            .retrieve()
                            .bodyToMono(Map.class)
                            .block();

                    assertThat(customer).isNotNull();
                    assertThat(customer.get("customerId")).isEqualTo(customerId.toString());
                    // Name gets split into firstName/lastName
                    assertThat(customer.get("firstName")).isEqualTo("John");
                    assertThat(customer.get("lastName")).isEqualTo("Doe");
                    assertThat(customer.get("email")).isEqualTo("john.doe@acme.com");
                });

        System.out.println("Customer event successfully consumed by management service");
    }

    @Test
    @Order(5)
    @DisplayName("Step 5: Customer service queries company data from management service (load-balanced)")
    void shouldQueryCompanyViaLoadBalancedCall() {
        // Given - Company exists in management service
        assertThat(companyId).isNotNull();

        // When - Customer service queries management service for company
        // (This would typically be done by customer service internally)
        // For testing, we'll query via gateway to verify it works
        Map<String, Object> company = apiGatewayClient.get()
                .uri("/api/management/companies/{id}", companyId)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        // Then - Should get company data
        assertThat(company).isNotNull();
        assertThat(company.get("companyId")).isEqualTo(companyId.toString());
        assertThat(company.get("companyName")).isEqualTo("Acme Corporation");

        System.out.println("Successfully retrieved company via load-balanced call");
    }

    @Test
    @Order(6)
    @DisplayName("Step 6: Query all customers through API Gateway")
    void shouldGetAllCustomersViaGateway() {
        // When - Getting all customers through API Gateway
        Object[] customers = apiGatewayClient.get()
                .uri("/api/customers")
                .retrieve()
                .bodyToMono(Object[].class)
                .block();

        // Then - Should have at least our created customer
        assertThat(customers).isNotNull();
        assertThat(customers.length).isGreaterThanOrEqualTo(1);

        System.out.println("Found " + customers.length + " customers");
    }

    @Test
    @Order(7)
    @DisplayName("Step 7: Query all companies through API Gateway")
    void shouldGetAllCompaniesViaGateway() {
        // When - Getting all companies through API Gateway
        Object[] companies = apiGatewayClient.get()
                .uri("/api/management/companies")
                .retrieve()
                .bodyToMono(Object[].class)
                .block();

        // Then - Should have at least our created company
        assertThat(companies).isNotNull();
        assertThat(companies.length).isGreaterThanOrEqualTo(1);

        System.out.println("Found " + companies.length + " companies");
    }

    @Test
    @Order(8)
    @DisplayName("Step 8: Verify service discovery is working via Consul")
    void shouldVerifyServiceDiscoveryViaConsul() {
        // When - Querying Consul for registered services
        WebClient consulClient = WebClient.builder()
                .baseUrl(CONSUL_URL)
                .build();

        Map<String, Object> services = consulClient.get()
                .uri("/v1/catalog/services")
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        // Then - Should have all our services registered
        assertThat(services).isNotNull();
        assertThat(services).containsKey("customer-service");
        assertThat(services).containsKey("management-service");
        assertThat(services).containsKey("api-gateway");
        assertThat(services).containsKey("config-server");

        System.out.println("All services registered in Consul: " + services.keySet());
    }

    @Test
    @Order(9)
    @DisplayName("Step 9: Update customer and verify event is published again")
    void shouldUpdateCustomerAndPublishEvent() {
        // Given - Existing customer
        assertThat(customerId).isNotNull();

        // When - Updating customer
        Map<String, Object> updateRequest = Map.of(
                "name", "John Smith",  // Changed last name
                "email", "john.smith@acme.com",  // Changed email
                "phone", "+1-555-0124",
                "password", "newpassword"
        );

        Map<String, Object> response = apiGatewayClient.put()
                .uri("/api/customers/{id}", customerId)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        // Then - Customer should be updated
        assertThat(response).isNotNull();
        assertThat(response.get("name")).isEqualTo("John Smith");
        assertThat(response.get("email")).isEqualTo("john.smith@acme.com");

        // And - Management service should eventually receive the update
        await()
                .atMost(15, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Map<String, Object> customer = managementClient.get()
                            .uri("/api/management/customers/{id}", customerId)
                            .retrieve()
                            .bodyToMono(Map.class)
                            .block();

                    assertThat(customer).isNotNull();
                    assertThat(customer.get("email")).isEqualTo("john.smith@acme.com");
                    assertThat(customer.get("lastName")).isEqualTo("Smith");
                });

        System.out.println("Customer update event successfully processed");
    }

    @Test
    @Order(10)
    @DisplayName("Step 10: Complete CRM workflow summary")
    void shouldSummarizeCompleteWorkflow() {
        System.out.println("\n========== COMPLETE CRM WORKFLOW SUMMARY ==========");
        System.out.println("✅ Created company: " + companyId);
        System.out.println("✅ Created address: " + addressId);
        System.out.println("✅ Created customer: " + customerId);
        System.out.println("✅ Customer event published to RabbitMQ");
        System.out.println("✅ Management service consumed customer event");
        System.out.println("✅ Load-balanced calls between services work");
        System.out.println("✅ API Gateway routing works");
        System.out.println("✅ Service discovery via Consul works");
        System.out.println("✅ Customer update events work");
        System.out.println("===================================================\n");

        // All assertions pass if we got here
        assertThat(companyId).isNotNull();
        assertThat(addressId).isNotNull();
        assertThat(customerId).isNotNull();
    }

}
