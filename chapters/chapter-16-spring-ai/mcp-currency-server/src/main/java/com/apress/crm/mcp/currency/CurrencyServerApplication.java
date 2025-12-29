package com.apress.crm.mcp.currency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Currency Conversion Server Application.
 *
 * This is a simple MCP (Model Context Protocol) compatible server
 * that provides currency conversion tools for AI assistants.
 *
 * In a production system, this would connect to real currency exchange APIs.
 * For demonstration, it uses static exchange rates.
 */
@SpringBootApplication
public class CurrencyServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CurrencyServerApplication.class, args);
    }
}
