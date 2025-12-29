package com.apress.crm.management.ai;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for the AI-powered CRM Assistant.
 *
 * Provides endpoints for:
 * - Basic chat interactions
 * - Product knowledge queries (RAG)
 * - Natural language database queries
 * - Function calling for customer operations
 */
@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final CrmAssistant crmAssistant;

    public AssistantController(CrmAssistant crmAssistant) {
        this.crmAssistant = crmAssistant;
    }

    /**
     * Basic chat endpoint.
     * POST /api/assistant/chat
     * Body: {"message": "Hello, how can you help me?"}
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        if (userMessage == null || userMessage.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Message is required"));
        }

        String response = crmAssistant.chat(userMessage);
        return ResponseEntity.ok(Map.of("response", response));
    }

    /**
     * Product knowledge endpoint using RAG.
     * POST /api/assistant/products
     * Body: {"message": "Tell me about the Enterprise plan features"}
     */
    @PostMapping("/products")
    public ResponseEntity<Map<String, String>> productKnowledge(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        if (userMessage == null || userMessage.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Message is required"));
        }

        String response = crmAssistant.chatWithProductKnowledge(userMessage);
        return ResponseEntity.ok(Map.of("response", response));
    }

    /**
     * Natural language database query endpoint.
     * POST /api/assistant/query
     * Body: {"message": "Show me all customers from California"}
     */
    @PostMapping("/query")
    public ResponseEntity<Map<String, String>> queryDatabase(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        if (userMessage == null || userMessage.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Message is required"));
        }

        String response = crmAssistant.queryDatabase(userMessage);
        return ResponseEntity.ok(Map.of("response", response));
    }

    /**
     * Function calling endpoint for customer operations.
     * POST /api/assistant/functions
     * Body: {"message": "Find customer with email john@example.com"}
     */
    @PostMapping("/functions")
    public ResponseEntity<Map<String, String>> chatWithFunctions(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        if (userMessage == null || userMessage.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Message is required"));
        }

        String response = crmAssistant.chatWithFunctions(userMessage);
        return ResponseEntity.ok(Map.of("response", response));
    }

    /**
     * Health check endpoint for the assistant.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "CRM AI Assistant"
        ));
    }
}
