package com.apress.crm.management.aot;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Advanced programmatic bean registration for Management application.
 * Demonstrates AOT-friendly conditional bean registration based on:
 * - Cloud platform detection
 * - Active profiles
 * - Environment properties
 */
public class ManagementBeanRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                        BeanDefinitionRegistry registry) {

        // Detect cloud platform and register appropriate beans
        if (isRunningOnKubernetes()) {
            registerKubernetesBeans(registry);
        } else if (isRunningOnCloudFoundry()) {
            registerCloudFoundryBeans(registry);
        } else {
            registerLocalDevelopmentBeans(registry);
        }

        // Register observability-specific beans
        registerObservabilityBeans(registry);

        // Register reactive-specific beans
        registerReactiveBeans(registry);
    }

    private boolean isRunningOnKubernetes() {
        return System.getenv("KUBERNETES_SERVICE_HOST") != null;
    }

    private boolean isRunningOnCloudFoundry() {
        return System.getenv("VCAP_APPLICATION") != null;
    }

    private void registerKubernetesBeans(BeanDefinitionRegistry registry) {
        // Register K8s-specific health probes
        if (!registry.containsBeanDefinition("k8sHealthProbe")) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(K8sHealthProbe.class);
            registry.registerBeanDefinition("k8sHealthProbe",
                builder.getBeanDefinition());
        }
    }

    private void registerCloudFoundryBeans(BeanDefinitionRegistry registry) {
        // Register Cloud Foundry specific beans
        // Example: CF service bindings, credentials
    }

    private void registerLocalDevelopmentBeans(BeanDefinitionRegistry registry) {
        // Register development-only beans
        // Example: mock external services, debug endpoints
    }

    private void registerObservabilityBeans(BeanDefinitionRegistry registry) {
        // Register custom observation filters or handlers
        // These work with OpenTelemetry in native mode
    }

    private void registerReactiveBeans(BeanDefinitionRegistry registry) {
        // Register reactive-specific WebClient configurations
        if (!registry.containsBeanDefinition("managementWebClient")) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(WebClient.class)
                .setFactoryMethod("builder");
            registry.registerBeanDefinition("managementWebClient",
                builder.getBeanDefinition());
        }
    }

    /**
     * Example K8s health probe bean.
     * In a real application, this would implement custom health checks.
     */
    public static class K8sHealthProbe {
        public boolean isHealthy() {
            return true;
        }
    }
}
