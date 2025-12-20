package com.apress.crm.customer;

import com.apress.crm.customer.client.CurrencyClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
class CurrencyClientTests {

    @Autowired
    private CurrencyClient currencyClient;

    @Test
    void shouldRetrieveExchangeRates() {
        Map<String, Object> response = currencyClient.getRates("USD");

        assertThat(response).containsKey("rates");
        assertThat(response.get("base_code")).isEqualTo("USD");

        Map<String, Double> rates = (Map<String, Double>) response.get("rates");
        assertThat(rates).containsKey("EUR");
    }

}