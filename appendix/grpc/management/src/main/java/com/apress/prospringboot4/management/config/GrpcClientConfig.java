package com.apress.prospringboot4.management.config;

import com.apress.prospringboot4.customer.grpc.CustomerRiskServiceGrpc;
import io.grpc.Channel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * gRPC Client Configuration.
 *
 * This configuration class creates gRPC client stubs as Spring beans.
 * The stubs can be injected into any Spring-managed component.
 *
 * Connection details are configured in application.yml under
 * spring.grpc.client.channels.
 */
@Configuration
public class GrpcClientConfig {

    /**
     * Creates the blocking stub for CustomerRiskService.
     * The Channel is automatically provided by Spring gRPC based on
     * the configuration in application.yml.
     */
    @Bean
    public CustomerRiskServiceGrpc.CustomerRiskServiceBlockingStub customerRiskServiceBlockingStub(
            @Qualifier("customer-service") Channel channel) {
        return CustomerRiskServiceGrpc.newBlockingStub(channel);
    }
}
