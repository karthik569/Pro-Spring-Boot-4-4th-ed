package com.apress.crm.customer;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile( "mtls")
@Service
public class ManagementOrchestrator {

    private final ManagementClient managementClient; // mTLS-enabled RestClient

    public ManagementOrchestrator(ManagementClient managementClient) {
        this.managementClient = managementClient;
    }

    public void onboardNewCustomer(Customer customer) {
        // 1. The RestClient automatically performs the mTLS handshake
        // 2. It obtains an OAuth2 token using its certificate
        // 3. It POSTs the new customer to the management system
        managementClient.createCustomer(customer);
    }
}