package com.apress.crm.customer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import static org.awaitility.Awaitility.await;

@SpringBootTest(properties = {
        "spring.sql.init.mode=always"
})
class CustomerAsyncTests {

    @Autowired
    private DummyAsyncService service;

    @Test
    void shouldWaitForBackgroundCachePopulation() {
        UUID id = UUID.randomUUID();
        service.triggerBackgroundProcessing(id);

        await()
            .atMost(Duration.ofSeconds(5))
            .pollInterval(Duration.ofMillis(500))
            .until(() -> service.isCachePopulated(id));
    }

    @TestConfiguration
    static class Config {
        @Bean
        DummyAsyncService dummyAsyncService() {
            return new DummyAsyncService();
        }
    }

    static class DummyAsyncService {
        private final Map<UUID, Boolean> cache = new ConcurrentHashMap<>();

        void triggerBackgroundProcessing(UUID id) {
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    Thread.sleep(2000);
                    cache.put(id, true);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        boolean isCachePopulated(UUID id) {
            return cache.getOrDefault(id, false);
        }
    }
}
