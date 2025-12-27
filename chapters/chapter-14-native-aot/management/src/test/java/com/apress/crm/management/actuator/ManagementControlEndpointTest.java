package com.apress.crm.management.actuator;

import com.apress.crm.management.service.ManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ManagementControlEndpointTest {

    @Autowired
    private ManagementControlEndpoint endpoint;

    @Autowired
    private ManagementService managementService;

    @Test
    void shouldGetCacheInfoFromCustomEndpoint() {
        // Given - add something to cache
        managementService.runComplexReport();

        // When - call the custom actuator endpoint
        var result = endpoint.getCacheInfo();

        // Then
        assertThat(result).containsEntry("status", "operational");
        assertThat(result).containsKey("cacheSize");
    }

    @Test
    void shouldClearCacheViaCustomEndpoint() {
        // Given - ensure cache has some data
        managementService.runComplexReport();

        // When - call DELETE on custom endpoint
        var result = endpoint.clearCache();

        // Then
        assertThat(result).containsEntry("result", "Cache cleared successfully");
        assertThat(managementService.getCacheSize()).isEqualTo(0);
    }
}
