package com.apress.crm.customer.actuator;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMetrics {

    private final Counter createdCounter;
    private final Timer onboardingTimer;

    public CustomerMetrics(MeterRegistry registry) {
        this.createdCounter = Counter.builder("crm.customer.created")
                .description("Total number of customers created")
                .tag("type", "standard")
                .register(registry);

        this.onboardingTimer = Timer.builder("crm.customer.onboarding")
                .description("Time taken to onboard a customer")
                .publishPercentiles(0.95, 0.99)
                .register(registry);
    }

    public void incrementCreated() {
        createdCounter.increment();
    }

    public Timer.Sample startTimer(MeterRegistry registry) {
        return Timer.start(registry);
    }

    public void stopTimer(Timer.Sample sample) {
        sample.stop(onboardingTimer);
    }
}
