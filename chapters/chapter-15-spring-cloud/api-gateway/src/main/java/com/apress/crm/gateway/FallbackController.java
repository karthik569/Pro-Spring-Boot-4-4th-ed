package com.apress.crm.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Fallback controller for circuit breaker fallback responses.
 *
 * When a downstream service is unavailable or the circuit is open,
 * the gateway will route requests to these fallback endpoints
 * to provide graceful degradation.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/customers")
    public ResponseEntity<Map<String, Object>> customerFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "message", "Customer service is temporarily unavailable. Please try again later.",
                        "timestamp", LocalDateTime.now(),
                        "service", "customer-service",
                        "status", "circuit-open"
                ));
    }

    @GetMapping("/management")
    public ResponseEntity<Map<String, Object>> managementFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "message", "Management service is temporarily unavailable. Please try again later.",
                        "timestamp", LocalDateTime.now(),
                        "service", "management-service",
                        "status", "circuit-open"
                ));
    }
}
