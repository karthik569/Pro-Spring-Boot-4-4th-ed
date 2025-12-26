package com.apress.crm.customer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Uses BCrypt by default, but supports multiple formats for migration
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
