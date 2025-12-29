package com.apress.prospringboot4.customer.grpc;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.grpc.server.service.GrpcService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * gRPC Service implementation for Customer Risk Assessment.
 *
 * This service uses the @GrpcService annotation to automatically register
 * with Spring gRPC server. It extends the generated base class from the
 * .proto file and implements the business logic.
 *
 * Key Features:
 * - Auto-registered as a Spring bean
 * - Thread-safe with Virtual Threads support
 * - Integrated with Spring Boot Actuator metrics
 */
@GrpcService
public class CustomerRiskGrpcService extends CustomerRiskServiceGrpc.CustomerRiskServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(CustomerRiskGrpcService.class);

    // In-memory risk profiles for demo purposes
    private final Map<String, RiskProfile> riskProfiles = new ConcurrentHashMap<>();

    public CustomerRiskGrpcService() {
        // Initialize with some demo data
        riskProfiles.put("CUST-001", new RiskProfile("LOW", 0.95));
        riskProfiles.put("CUST-002", new RiskProfile("MEDIUM", 0.65));
        riskProfiles.put("CUST-003", new RiskProfile("HIGH", 0.25));
        riskProfiles.put("CUST-004", new RiskProfile("LOW", 0.88));
    }

    @Override
    public void getRiskProfile(RiskRequest request, StreamObserver<RiskResponse> responseObserver) {
        String customerId = request.getCustomerId();
        log.info("Received risk profile request for customer: {}", customerId);

        try {
            // Simulate business logic to determine risk
            RiskProfile profile = riskProfiles.getOrDefault(
                    customerId,
                    new RiskProfile("MEDIUM", 0.50) // Default for unknown customers
            );

            RiskResponse response = RiskResponse.newBuilder()
                    .setCustomerId(customerId)
                    .setRiskLevel(profile.level())
                    .setScore(profile.score())
                    .build();

            log.info("Returning risk profile for {}: {} (score: {})",
                    customerId, profile.level(), profile.score());

            // Send the response
            responseObserver.onNext(response);
            // Signal completion
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error processing risk profile request for customer: {}", customerId, e);
            responseObserver.onError(e);
        }
    }

    /**
     * Internal record to hold risk profile data.
     */
    private record RiskProfile(String level, double score) {
    }
}
