package com.apress.crm.customer.actuator;

import com.apress.crm.customer.Customer;
import com.apress.crm.customer.CustomerRepository;
import com.apress.crm.customer.CustomerService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.stream.function.StreamBridge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomerMetricsTest {

    @Test
    void shouldIncrementCreatedCounterWhenCustomerSaved() {
        // Arrange
        MeterRegistry registry = new SimpleMeterRegistry();
        CustomerMetrics metrics = new CustomerMetrics(registry);
        CustomerRepository repo = mock(CustomerRepository.class);
        StreamBridge streamBridge = mock(StreamBridge.class);

        Customer customer = new Customer("John Doe", "john.doe@test.com", "123-456-7890", "password");
        when(repo.save(any(Customer.class))).thenReturn(customer);
        when(streamBridge.send(anyString(), any())).thenReturn(true);

        CustomerService service = new CustomerService(repo, metrics, registry, streamBridge);

        // Act
        service.save(customer);

        // Assert
        assertThat(registry.get("crm.customer.created").counter().count()).isEqualTo(1.0);
    }

    @Test
    void shouldTrackOnboardingTimer() {
        // Arrange
        MeterRegistry registry = new SimpleMeterRegistry();
        CustomerMetrics metrics = new CustomerMetrics(registry);
        CustomerRepository repo = mock(CustomerRepository.class);
        StreamBridge streamBridge = mock(StreamBridge.class);

        Customer customer = new Customer("Jane Smith", "jane.smith@test.com", "987-654-3210", "password");
        when(repo.save(any(Customer.class))).thenReturn(customer);
        when(streamBridge.send(anyString(), any())).thenReturn(true);

        CustomerService service = new CustomerService(repo, metrics, registry, streamBridge);

        // Act
        service.save(customer);

        // Assert
        assertThat(registry.get("crm.customer.onboarding").timer().count()).isEqualTo(1);
        assertThat(registry.get("crm.customer.onboarding").timer().totalTime(java.util.concurrent.TimeUnit.NANOSECONDS)).isGreaterThan(0);
    }
}
