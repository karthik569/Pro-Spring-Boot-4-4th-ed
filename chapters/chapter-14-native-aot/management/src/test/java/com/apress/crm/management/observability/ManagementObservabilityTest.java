package com.apress.crm.management.observability;

import com.apress.crm.management.service.ManagementService;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.tck.TestObservationRegistry;
import io.micrometer.observation.tck.TestObservationRegistryAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootTest
class ManagementObservabilityTest {

    @TestConfiguration
    static class ObservabilityTestConfig {
        @Bean
        @Primary
        public TestObservationRegistry testObservationRegistry() {
            return TestObservationRegistry.create();
        }
    }

    @Autowired
    private ManagementService managementService;

    @Autowired
    private ObservationRegistry observationRegistry;

    private TestObservationRegistry testRegistry;

    @BeforeEach
    void setUp() {
        testRegistry = (TestObservationRegistry) observationRegistry;
        testRegistry.clear();
    }

    @Test
    void shouldCreateObservationWhenRunningComplexReport() {
        // When
        managementService.runComplexReport();

        // Then - verify observation was created
        TestObservationRegistryAssert.assertThat(testRegistry)
                .hasObservationWithNameEqualTo("management.report")
                .that()
                .hasLowCardinalityKeyValue("reportType", "full");
    }

    @Test
    void shouldAddRegionTagToObservations() {
        // When
        managementService.runComplexReport();

        // Then - verify region tag was added by RegionObservationHandler
        TestObservationRegistryAssert.assertThat(testRegistry)
                .hasObservationWithNameEqualTo("management.report")
                .that()
                .hasLowCardinalityKeyValue("region", "US-East");
    }
}
