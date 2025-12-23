package com.apress.crm.management.reactive;

import com.apress.crm.management.BaseTest;
import com.apress.crm.management.model.CustomerSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

class CustomerSessionTest extends BaseTest {

    @Autowired
    private ReactiveRedisOperations<String, CustomerSession> sessionOps;

    @Autowired
    private com.apress.crm.management.repository.redis.CustomerSessionRepository customerSessionRepository;

    @Test
    void shouldSaveAndRetrieveSessionReactively() {
        UUID customerId = UUID.randomUUID();
        String key = "session:" + customerId;
        CustomerSession session = new CustomerSession(key, customerId, "Login");

        var saveAndGet = sessionOps.opsForValue()
                .set(key, session)
                .then(sessionOps.opsForValue().get(key));

        StepVerifier.create(saveAndGet)
                .expectNext(session)
                .verifyComplete();
    }

    @Test
    void shouldSaveAndRetrieveUsingRepository() {
        UUID customerId = UUID.randomUUID();
        String sessionId = "repo-session:" + customerId;
        CustomerSession session = new CustomerSession(sessionId, customerId, "Repository Access");

        // Blocking call
        customerSessionRepository.save(session);
        
        // Verify
        var retrieved = customerSessionRepository.findById(sessionId);
        
        StepVerifier.create(Mono.justOrEmpty(retrieved))
                .expectNextMatches(s -> s.lastAction().equals("Repository Access"))
                .verifyComplete();
    }
}
