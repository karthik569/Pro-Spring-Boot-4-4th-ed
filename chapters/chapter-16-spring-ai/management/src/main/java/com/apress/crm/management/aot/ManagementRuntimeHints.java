package com.apress.crm.management.aot;

import com.apress.crm.management.model.Customer;
import com.apress.crm.management.model.Address;
import com.apress.crm.management.model.Company;
import com.apress.crm.management.model.Communication;
import com.apress.crm.management.model.CustomerDetailsDTO;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

/**
 * Advanced runtime hints for Management application.
 * Demonstrates comprehensive hints for reactive applications with R2DBC and OpenTelemetry.
 */
public class ManagementRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Register all domain model classes for reflection
        // Required for R2DBC reactive queries and JSON serialization
        registerDomainModel(hints);

        // Register DTO classes for serialization
        registerDtoClasses(hints);

        // Register resource patterns for reactive applications
        registerResourcePatterns(hints);

        // Register serialization hints
        registerSerializationHints(hints);

        // Register proxy hints for reactive repositories
        registerReactiveProxies(hints);
    }

    private void registerDomainModel(RuntimeHints hints) {
        Class<?>[] domainClasses = {
            Customer.class,
            Address.class,
            Company.class,
            Communication.class
        };

        for (Class<?> clazz : domainClasses) {
            hints.reflection()
                .registerType(
                    TypeReference.of(clazz),
                    hint -> hint
                        .withMembers(
                            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                            MemberCategory.INVOKE_DECLARED_METHODS,
                            MemberCategory.INVOKE_PUBLIC_METHODS
                        )
                );
        }
    }

    private void registerDtoClasses(RuntimeHints hints) {
        hints.reflection()
            .registerType(
                TypeReference.of(CustomerDetailsDTO.class),
                hint -> hint
                    .withMembers(
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS
                    )
            );
    }

    private void registerResourcePatterns(RuntimeHints hints) {
        // Register patterns for configuration files
        hints.resources()
            .registerPattern("management-*.properties")
            .registerPattern("management-*.yml")
            .registerPattern("db/migration/*.sql")
            .registerPattern("graphql/*.graphqls")
            .registerPattern("static/openapi.yaml");
    }

    private void registerSerializationHints(RuntimeHints hints) {
        // Register all domain classes for serialization
        hints.serialization()
            .registerType(TypeReference.of(Customer.class))
            .registerType(TypeReference.of(Address.class))
            .registerType(TypeReference.of(Company.class))
            .registerType(TypeReference.of(Communication.class))
            .registerType(TypeReference.of(CustomerDetailsDTO.class));
    }

    private void registerReactiveProxies(RuntimeHints hints) {
        // Register JDK proxies for reactive repositories if needed
        // This is typically handled automatically by Spring Data R2DBC
        // but can be registered explicitly if issues arise

        // Example:
        // hints.proxies().registerJdkProxy(
        //     ReactiveCrudRepository.class,
        //     CustomerRepository.class
        // );
    }
}
