package com.apress.crm.customer.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuration for client-side load balancing.
 *
 * This configuration provides a @LoadBalanced RestClient.Builder bean
 * that can be used to create RestClient instances that automatically
 * resolve service names via Consul and perform client-side load balancing.
 */
@Configuration
public class LoadBalancerConfig {

    /**
     * Provides a load-balanced RestClient.Builder.
     *
     * When injected, this builder will create RestClient instances that:
     * 1. Resolve service names (like "management-service") via Consul
     * 2. Automatically choose healthy instances using round-robin or other strategies
     * 3. Integrate with circuit breakers if configured
     */
    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }
}
