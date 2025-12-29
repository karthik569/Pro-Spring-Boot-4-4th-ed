package com.apress.prospringboot4.management.service;

import com.apress.prospringboot4.customer.grpc.CustomerRiskServiceGrpc;
import com.apress.prospringboot4.customer.grpc.RiskRequest;
import com.apress.prospringboot4.customer.grpc.RiskResponse;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Risk Assessment Service that uses the gRPC client.
 *
 * This service demonstrates how to:
 * 1. Inject the gRPC BlockingStub
 * 2. Make synchronous gRPC calls
 * 3. Handle gRPC errors properly
 * 4. Use gRPC deadlines/timeouts
 */
@Service
public class RiskAssessmentService {

    private static final Logger log = LoggerFactory.getLogger(RiskAssessmentService.class);

    private final CustomerRiskServiceGrpc.CustomerRiskServiceBlockingStub riskStub;

    public RiskAssessmentService(CustomerRiskServiceGrpc.CustomerRiskServiceBlockingStub riskStub) {
        this.riskStub = riskStub;
    }

    /**
     * Assess a customer's risk profile by calling the gRPC service.
     *
     * @param customerId the customer ID to assess
     */
    public void assessCustomer(String customerId) {
        log.info("Requesting risk profile for customer: {}", customerId);

        try {
            // Build the request
            RiskRequest request = RiskRequest.newBuilder()
                    .setCustomerId(customerId)
                    .build();

            // Make the gRPC call (synchronous blocking call)
            RiskResponse response = riskStub.getRiskProfile(request);

            // Process the response
            log.info("Received risk profile for customer {}: Risk Level = {}, Score = {}",
                    response.getCustomerId(),
                    response.getRiskLevel(),
                    response.getScore());

            System.out.printf("  ├─ Risk Level: %s%n", response.getRiskLevel());
            System.out.printf("  └─ Score: %.2f%n", response.getScore());

        } catch (StatusRuntimeException e) {
            log.error("gRPC call failed for customer {}: {} - {}",
                    customerId, e.getStatus().getCode(), e.getStatus().getDescription());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error assessing customer {}", customerId, e);
            throw new RuntimeException("Failed to assess customer risk", e);
        }
    }

    /**
     * Get the risk level as a string for display purposes.
     *
     * @param customerId the customer ID
     * @return the risk level string
     */
    public String getRiskLevel(String customerId) {
        RiskRequest request = RiskRequest.newBuilder()
                .setCustomerId(customerId)
                .build();

        RiskResponse response = riskStub.getRiskProfile(request);
        return response.getRiskLevel();
    }
}
