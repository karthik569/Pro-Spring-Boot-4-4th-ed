package com.apress.crm.management;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestCacheConfiguration.class)
public abstract class BaseTest {
    // Base test class for all integration tests
    // - Uses local CockroachDB at localhost:26257/management_db
    // - Uses in-memory cache (ConcurrentMapCacheManager) for simplified test setup
    // - Tests run with 'test' profile using application-test.yml configuration
}