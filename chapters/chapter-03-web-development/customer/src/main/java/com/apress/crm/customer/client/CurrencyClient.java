package com.apress.crm.customer.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import java.util.Map;

@HttpExchange("https://open.er-api.com/v6/latest")
public interface CurrencyClient {

    @GetExchange("/{base}")
    Map<String, Object> getRates(@PathVariable String base);

}