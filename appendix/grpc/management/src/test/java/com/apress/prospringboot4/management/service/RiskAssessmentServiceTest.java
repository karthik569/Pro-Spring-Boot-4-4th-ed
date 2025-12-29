package com.apress.prospringboot4.management.service;

import com.apress.prospringboot4.customer.grpc.CustomerRiskServiceGrpc;
import com.apress.prospringboot4.customer.grpc.RiskRequest;
import com.apress.prospringboot4.customer.grpc.RiskResponse;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RiskAssessmentService.
 *
 * This test demonstrates:
 * - Mocking gRPC stubs
 * - Testing service methods
 * - Verifying gRPC interactions
 */
@ExtendWith(MockitoExtension.class)
class RiskAssessmentServiceTest {

    @Mock
    private CustomerRiskServiceGrpc.CustomerRiskServiceBlockingStub riskStub;

    private RiskAssessmentService riskAssessmentService;

    @BeforeEach
    void setUp() {
        riskAssessmentService = new RiskAssessmentService(riskStub);
    }

    @Test
    void testAssessCustomer_Success() {
        // Given
        String customerId = "CUST-001";
        RiskResponse mockResponse = RiskResponse.newBuilder()
                .setCustomerId(customerId)
                .setRiskLevel("LOW")
                .setScore(0.95)
                .build();

        when(riskStub.getRiskProfile(any(RiskRequest.class)))
                .thenReturn(mockResponse);

        // When
        assertDoesNotThrow(() -> riskAssessmentService.assessCustomer(customerId));

        // Then
        verify(riskStub, times(1)).getRiskProfile(any(RiskRequest.class));
    }

    @Test
    void testGetRiskLevel_Success() {
        // Given
        String customerId = "CUST-002";
        RiskResponse mockResponse = RiskResponse.newBuilder()
                .setCustomerId(customerId)
                .setRiskLevel("MEDIUM")
                .setScore(0.65)
                .build();

        when(riskStub.getRiskProfile(any(RiskRequest.class)))
                .thenReturn(mockResponse);

        // When
        String riskLevel = riskAssessmentService.getRiskLevel(customerId);

        // Then
        assertEquals("MEDIUM", riskLevel);
        verify(riskStub, times(1)).getRiskProfile(any(RiskRequest.class));
    }

    @Test
    void testAssessCustomer_GrpcError() {
        // Given
        String customerId = "CUST-ERROR";
        StatusRuntimeException exception = io.grpc.Status.UNAVAILABLE
                .withDescription("Service unavailable")
                .asRuntimeException();
        when(riskStub.getRiskProfile(any(RiskRequest.class)))
                .thenThrow(exception);

        // When & Then
        assertThrows(StatusRuntimeException.class,
                () -> riskAssessmentService.assessCustomer(customerId));
    }

    @Test
    void testGetRiskLevel_HighRisk() {
        // Given
        String customerId = "CUST-003";
        RiskResponse mockResponse = RiskResponse.newBuilder()
                .setCustomerId(customerId)
                .setRiskLevel("HIGH")
                .setScore(0.25)
                .build();

        when(riskStub.getRiskProfile(any(RiskRequest.class)))
                .thenReturn(mockResponse);

        // When
        String riskLevel = riskAssessmentService.getRiskLevel(customerId);

        // Then
        assertEquals("HIGH", riskLevel);
    }
}
