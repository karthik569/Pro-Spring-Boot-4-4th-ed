package com.apress.prospringboot4.customer.grpc;

import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CustomerRiskGrpcService using in-process gRPC server.
 *
 * This test demonstrates:
 * - Setting up an in-process gRPC server for testing
 * - Testing gRPC service methods
 * - Verifying response data
 */
class CustomerRiskGrpcServiceTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private CustomerRiskServiceGrpc.CustomerRiskServiceBlockingStub blockingStub;

    @BeforeEach
    void setUp() throws Exception {
        // Generate a unique in-process server name
        String serverName = InProcessServerBuilder.generateName();

        // Create and start the in-process server
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName)
                .directExecutor()
                .addService(new CustomerRiskGrpcService())
                .build()
                .start());

        // Create a client channel and stub
        blockingStub = CustomerRiskServiceGrpc.newBlockingStub(
                grpcCleanup.register(InProcessChannelBuilder
                        .forName(serverName)
                        .directExecutor()
                        .build())
        );
    }

    @Test
    void testGetRiskProfile_KnownCustomer() {
        // Given
        RiskRequest request = RiskRequest.newBuilder()
                .setCustomerId("CUST-001")
                .build();

        // When
        RiskResponse response = blockingStub.getRiskProfile(request);

        // Then
        assertNotNull(response);
        assertEquals("CUST-001", response.getCustomerId());
        assertEquals("LOW", response.getRiskLevel());
        assertEquals(0.95, response.getScore(), 0.001);
    }

    @Test
    void testGetRiskProfile_HighRiskCustomer() {
        // Given
        RiskRequest request = RiskRequest.newBuilder()
                .setCustomerId("CUST-003")
                .build();

        // When
        RiskResponse response = blockingStub.getRiskProfile(request);

        // Then
        assertNotNull(response);
        assertEquals("CUST-003", response.getCustomerId());
        assertEquals("HIGH", response.getRiskLevel());
        assertEquals(0.25, response.getScore(), 0.001);
    }

    @Test
    void testGetRiskProfile_UnknownCustomer() {
        // Given
        RiskRequest request = RiskRequest.newBuilder()
                .setCustomerId("CUST-999")
                .build();

        // When
        RiskResponse response = blockingStub.getRiskProfile(request);

        // Then - Should return default MEDIUM risk for unknown customers
        assertNotNull(response);
        assertEquals("CUST-999", response.getCustomerId());
        assertEquals("MEDIUM", response.getRiskLevel());
        assertEquals(0.50, response.getScore(), 0.001);
    }

    @Test
    void testGetRiskProfile_MediumRiskCustomer() {
        // Given
        RiskRequest request = RiskRequest.newBuilder()
                .setCustomerId("CUST-002")
                .build();

        // When
        RiskResponse response = blockingStub.getRiskProfile(request);

        // Then
        assertNotNull(response);
        assertEquals("CUST-002", response.getCustomerId());
        assertEquals("MEDIUM", response.getRiskLevel());
        assertEquals(0.65, response.getScore(), 0.001);
    }
}
