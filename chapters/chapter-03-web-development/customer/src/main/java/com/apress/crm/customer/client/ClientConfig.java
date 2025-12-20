package com.apress.crm.customer.client;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(CurrencyClient.class) // (1)
public class ClientConfig { }