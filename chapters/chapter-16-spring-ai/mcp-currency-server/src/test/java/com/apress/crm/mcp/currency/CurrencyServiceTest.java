package com.apress.crm.mcp.currency;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CurrencyService.
 */
class CurrencyServiceTest {

    private final CurrencyService currencyService = new CurrencyService();

    @Test
    void shouldConvertUSDToEUR() {
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal converted = currencyService.convert(amount, "USD", "EUR");

        assertThat(converted).isNotNull();
        assertThat(converted.compareTo(BigDecimal.ZERO)).isGreaterThan(0);
    }

    @Test
    void shouldConvertSameCurrency() {
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal converted = currencyService.convert(amount, "USD", "USD");

        assertThat(converted).isEqualByComparingTo(amount);
    }

    @Test
    void shouldGetExchangeRate() {
        BigDecimal rate = currencyService.getExchangeRate("USD", "EUR");

        assertThat(rate).isNotNull();
        assertThat(rate.compareTo(BigDecimal.ZERO)).isGreaterThan(0);
    }

    @Test
    void shouldGetSupportedCurrencies() {
        var currencies = currencyService.getSupportedCurrencies();

        assertThat(currencies).isNotEmpty();
        assertThat(currencies).containsKey("USD");
        assertThat(currencies).containsKey("EUR");
    }
}
