package com.apress.crm.customer.actuator;

import com.apress.crm.customer.CustomerRepository;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CrmInfoContributor implements InfoContributor {

    private final CustomerRepository customerRepository;

    public CrmInfoContributor(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public void contribute(Info.Builder builder) {
        long count = customerRepository.count();
        builder.withDetail("crm", Map.of(
                "customerCount", count,
                "region", "US-East"
        ));
    }
}
