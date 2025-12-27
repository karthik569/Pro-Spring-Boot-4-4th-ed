package com.apress.crm.customer;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
@EnableCaching
public class TestCacheConfiguration {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        // Use a simple in-memory cache for tests instead of Redis
        // This simplifies test setup and makes tests faster
        return new ConcurrentMapCacheManager("customers");
    }
}
