package com.apress.prospringboot4.management;

import com.apress.prospringboot4.management.config.GrpcClientConfig;
import com.apress.prospringboot4.management.service.RiskAssessmentService;
import org.junit.jupiter.api.Test;

/**
 * Basic application test.
 *
 * This is a simple smoke test to verify the project compiles correctly.
 */
class ManagementApplicationTests {

    @Test
    void smokeTest() {
        // Verify that key classes exist and can be instantiated
        ManagementApplication app = new ManagementApplication();
        GrpcClientConfig config = new GrpcClientConfig();

        // If we get here without exceptions, the test passes
        assert app != null;
        assert config != null;
    }
}
