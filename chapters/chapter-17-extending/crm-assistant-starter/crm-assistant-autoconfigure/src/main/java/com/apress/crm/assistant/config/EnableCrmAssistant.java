package com.apress.crm.assistant.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Annotation to explicitly enable the CRM Assistant in a Spring Boot application.
 *
 * This annotation provides an alternative to auto-configuration for users who prefer
 * explicit opt-in control over their features.
 *
 * Usage:
 * <pre>
 * &#64;SpringBootApplication
 * &#64;EnableCrmAssistant
 * public class MyApplication {
 *     public static void main(String[] args) {
 *         SpringApplication.run(MyApplication.class, args);
 *     }
 * }
 * </pre>
 *
 * When this annotation is added to a {@code @Configuration} class or {@code @SpringBootApplication},
 * Spring will process the {@link CrmAssistantAutoConfiguration} and all its beans will be registered.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(CrmAssistantAutoConfiguration.class)
public @interface EnableCrmAssistant {
}
