package com.apress.crm.customer;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final Repository<Customer, UUID> repository;
    private final CrmProperties crmProperties;
    private final MessageSource messageSource;


    public CustomerController(Repository<Customer, UUID> repository, CrmProperties crmProperties, MessageSource messageSource) {
        this.repository = repository;
        this.crmProperties = crmProperties;
        this.messageSource = messageSource;
    }

    @GetMapping("/welcome")
    public String welcome() {
        return crmProperties.welcomeMessage();
    }

    @GetMapping("/greet/{name}")
    public String greet(@PathVariable String name) {
        Locale locale = LocaleContextHolder.getLocale(); // (1)
        return messageSource.getMessage("welcome.message", new Object[]{name}, locale); // (2)
    }

    @GetMapping
    Iterable<Customer> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    Customer findById(@PathVariable UUID id) {
        return repository.findById(id);
    }

    @PostMapping
    Customer save(@RequestBody Customer customer) {
        return repository.save(customer);
    }

    @DeleteMapping("/{id}")
    void deleteById(@PathVariable UUID id) {
        repository.deleteById(id);
    }
}