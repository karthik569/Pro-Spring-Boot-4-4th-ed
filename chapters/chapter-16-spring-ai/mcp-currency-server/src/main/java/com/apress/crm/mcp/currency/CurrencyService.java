package com.apress.crm.mcp.currency;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Service for currency conversion operations.
 *
 * In a production system, this would integrate with real-time currency exchange APIs
 * like exchangerate-api.com, openexchangerates.org, or similar services.
 *
 * For demonstration purposes, this uses static exchange rates.
 */
@Service
public class CurrencyService {

    // Static exchange rates to USD (base currency)
    // In production, these would be fetched from a real API
    private static final Map<String, BigDecimal> EXCHANGE_RATES_TO_USD = Map.of(
            "USD", new BigDecimal("1.00"),
            "EUR", new BigDecimal("0.92"),
            "GBP", new BigDecimal("0.79"),
            "JPY", new BigDecimal("149.50"),
            "CAD", new BigDecimal("1.36"),
            "AUD", new BigDecimal("1.52"),
            "CHF", new BigDecimal("0.88"),
            "CNY", new BigDecimal("7.24"),
            "INR", new BigDecimal("83.12"),
            "MXN", new BigDecimal("17.05")
    );

    private static final Map<String, String> CURRENCY_NAMES = Map.of(
            "USD", "US Dollar",
            "EUR", "Euro",
            "GBP", "British Pound",
            "JPY", "Japanese Yen",
            "CAD", "Canadian Dollar",
            "AUD", "Australian Dollar",
            "CHF", "Swiss Franc",
            "CNY", "Chinese Yuan",
            "INR", "Indian Rupee",
            "MXN", "Mexican Peso"
    );

    /**
     * Convert an amount from one currency to another.
     *
     * @param amount the amount to convert
     * @param fromCurrency the source currency code (e.g., "USD")
     * @param toCurrency the target currency code (e.g., "EUR")
     * @return the converted amount
     */
    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        validateCurrency(fromCurrency);
        validateCurrency(toCurrency);

        // Convert from source currency to USD
        BigDecimal amountInUSD = amount.divide(
                EXCHANGE_RATES_TO_USD.get(fromCurrency),
                6,
                RoundingMode.HALF_UP
        );

        // Convert from USD to target currency
        BigDecimal convertedAmount = amountInUSD.multiply(EXCHANGE_RATES_TO_USD.get(toCurrency));

        // Round to 2 decimal places
        return convertedAmount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Get the exchange rate from one currency to another.
     *
     * @param fromCurrency the source currency code
     * @param toCurrency the target currency code
     * @return the exchange rate
     */
    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        validateCurrency(fromCurrency);
        validateCurrency(toCurrency);

        BigDecimal fromRate = EXCHANGE_RATES_TO_USD.get(fromCurrency);
        BigDecimal toRate = EXCHANGE_RATES_TO_USD.get(toCurrency);

        return toRate.divide(fromRate, 6, RoundingMode.HALF_UP);
    }

    /**
     * Get all supported currencies.
     *
     * @return map of currency codes to currency names
     */
    public Map<String, String> getSupportedCurrencies() {
        return CURRENCY_NAMES;
    }

    private void validateCurrency(String currencyCode) {
        if (!EXCHANGE_RATES_TO_USD.containsKey(currencyCode)) {
            throw new IllegalArgumentException(
                    "Unsupported currency: " + currencyCode +
                    ". Supported currencies: " + EXCHANGE_RATES_TO_USD.keySet()
            );
        }
    }
}
