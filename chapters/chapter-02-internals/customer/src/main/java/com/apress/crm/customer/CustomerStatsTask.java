package com.apress.crm.customer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.stream.StreamSupport;

@Component
public class CustomerStatsTask {

    private static final Logger log = LoggerFactory.getLogger(CustomerStatsTask.class);
    private final Repository<Customer, ?> repository;

    public CustomerStatsTask(Repository<Customer, ?> repository) {
        this.repository = repository;
    }

    @Scheduled(fixedRate = 60000) // (1)
    public void reportCustomerCount() {
        long count = StreamSupport.stream(repository.findAll().spliterator(), false).count();
        log.info("CRM Stats: Total customers currently in system: {}", count);
    }
}