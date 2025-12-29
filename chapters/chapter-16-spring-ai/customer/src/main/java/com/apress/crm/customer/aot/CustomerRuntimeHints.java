package com.apress.crm.customer.aot;

import com.apress.crm.customer.Customer;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

/**
 * Runtime hints for native image compilation.
 * This class demonstrates how to provide hints to GraalVM for reflection,
 * resources, and serialization that cannot be automatically detected.
 */
public class CustomerRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Register reflection hints for Customer class
        // This ensures Customer can be serialized/deserialized in native mode
        hints.reflection()
            .registerType(
                TypeReference.of(Customer.class),
                hint -> hint
                    .withMembers(
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS,
                        MemberCategory.INVOKE_PUBLIC_METHODS
                    )
            );

        // Register resource patterns for configuration files
        // This makes sure resources are included in the native image
        hints.resources()
            .registerPattern("customer-*.properties")
            .registerPattern("customer-*.yml")
            .registerPattern("db/migration/*.sql");

        // Register serialization hint for Customer class
        // Required if Customer is used in serialization contexts
        hints.serialization()
            .registerType(TypeReference.of(Customer.class));

        // Example: Register proxy hint if needed for interfaces
        // hints.proxies().registerJdkProxy(SomeInterface.class);
    }
}
