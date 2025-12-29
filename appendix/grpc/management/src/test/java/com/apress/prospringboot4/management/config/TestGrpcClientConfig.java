package com.apress.prospringboot4.management.config;

import com.apress.prospringboot4.customer.grpc.CustomerRiskServiceGrpc;
import com.apress.prospringboot4.customer.grpc.RiskRequest;
import com.apress.prospringboot4.customer.grpc.RiskResponse;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test configuration for gRPC clients.
 * Provides mock implementations for testing.
 */
@TestConfiguration
public class TestGrpcClientConfig {

    @Bean
    @Primary
    public CustomerRiskServiceGrpc.CustomerRiskServiceBlockingStub testCustomerRiskServiceBlockingStub() {
        // Create a mock stub
        CustomerRiskServiceGrpc.CustomerRiskServiceBlockingStub mockStub =
                mock(CustomerRiskServiceGrpc.CustomerRiskServiceBlockingStub.class);

        // Configure mock behavior
        RiskResponse mockResponse = RiskResponse.newBuilder()
                .setCustomerId("TEST-001")
                .setRiskLevel("LOW")
                .setScore(0.95)
                .build();

        when(mockStub.getRiskProfile(any(RiskRequest.class))).thenReturn(mockResponse);

        return mockStub;
    }
}
