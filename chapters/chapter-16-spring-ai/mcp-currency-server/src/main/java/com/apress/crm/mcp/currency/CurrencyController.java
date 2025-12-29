package com.apress.crm.mcp.currency;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * REST controller for currency conversion operations.
 *
 * Provides endpoints that can be called by AI assistants to perform
 * currency conversions.
 */
@RestController
@RequestMapping("/api/currency")
public class CurrencyController {

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    /**
     * Convert an amount from one currency to another.
     *
     * POST /api/currency/convert
     * Body: {"amount": 100.00, "from": "USD", "to": "EUR"}
     */
    @PostMapping("/convert")
    public ConversionResponse convert(@RequestBody ConversionRequest request) {
        BigDecimal convertedAmount = currencyService.convert(
                request.amount(),
                request.from(),
                request.to()
        );

        return new ConversionResponse(
                request.amount(),
                request.from(),
                convertedAmount,
                request.to()
        );
    }

    /**
     * Get current exchange rate between two currencies.
     *
     * GET /api/currency/rate?from=USD&to=EUR
     */
    @GetMapping("/rate")
    public ExchangeRateResponse getExchangeRate(
            @RequestParam String from,
            @RequestParam String to
    ) {
        BigDecimal rate = currencyService.getExchangeRate(from, to);
        return new ExchangeRateResponse(from, to, rate);
    }

    /**
     * Get all supported currencies.
     */
    @GetMapping("/supported")
    public Map<String, String> getSupportedCurrencies() {
        return currencyService.getSupportedCurrencies();
    }

    public record ConversionRequest(
            BigDecimal amount,
            String from,
            String to
    ) {}

    public record ConversionResponse(
            BigDecimal originalAmount,
            String originalCurrency,
            BigDecimal convertedAmount,
            String targetCurrency
    ) {}

    public record ExchangeRateResponse(
            String from,
            String to,
            BigDecimal rate
    ) {}
}
