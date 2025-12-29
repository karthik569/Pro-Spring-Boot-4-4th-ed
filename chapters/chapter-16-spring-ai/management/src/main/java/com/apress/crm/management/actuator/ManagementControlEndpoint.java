package com.apress.crm.management.actuator;

import com.apress.crm.management.service.ManagementService;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Endpoint(id = "managementControl")
public class ManagementControlEndpoint {

    private final ManagementService managementService;

    public ManagementControlEndpoint(ManagementService managementService) {
        this.managementService = managementService;
    }

    @ReadOperation
    public Map<String, Object> getCacheInfo() {
        return Map.of(
                "cacheSize", managementService.getCacheSize(),
                "status", "operational"
        );
    }

    @DeleteOperation
    public Map<String, String> clearCache() {
        managementService.clearCache();
        return Map.of("result", "Cache cleared successfully");
    }
}
