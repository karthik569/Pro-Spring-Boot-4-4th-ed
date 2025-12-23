package com.apress.crm.customer;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestCacheConfiguration.class)
public abstract class BaseTest {
    // Using in-memory cache from TestCacheConfiguration
    // Redis Testcontainer removed for simplicity - tests focus on CockroachDB
}