package com.apress.crm.customer;

import org.springframework.context.annotation.Profile;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@Profile( "mtls")
@HttpExchange("/api/v1/management")
public interface ManagementClient {

    @PostExchange("/customers")
    void createCustomer(Customer customer);
}