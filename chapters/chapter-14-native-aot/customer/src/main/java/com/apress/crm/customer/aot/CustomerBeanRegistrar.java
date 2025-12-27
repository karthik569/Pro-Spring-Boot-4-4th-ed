package com.apress.crm.customer.aot;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.concurrent.Executors;

/**
 * Demonstrates programmatic bean registration for AOT processing.
 * This approach allows conditional bean registration that works in native mode.
 *
 * Traditional @Conditional annotations work in native mode, but this demonstrates
 * an alternative approach using ImportBeanDefinitionRegistrar.
 */
public class CustomerBeanRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                        BeanDefinitionRegistry registry) {
        // Register a custom executor for async operations
        // This demonstrates programmatic bean registration for AOT
        if (!registry.containsBeanDefinition("customExecutor")) {
            BeanDefinitionBuilder executorBuilder =
                BeanDefinitionBuilder.rootBeanDefinition(Executors.class);
            executorBuilder.setFactoryMethod("newVirtualThreadPerTaskExecutor");

            registry.registerBeanDefinition("customExecutor",
                executorBuilder.getBeanDefinition());
        }

        // Example: Register different beans based on environment
        // This demonstrates AOT-friendly conditional logic
        if (isKubernetesEnvironment()) {
            registerKubernetesSpecificBeans(registry);
        } else {
            registerLocalDevelopmentBeans(registry);
        }
    }

    private boolean isKubernetesEnvironment() {
        // Check for Kubernetes environment variables
        return System.getenv("KUBERNETES_SERVICE_HOST") != null;
    }

    private void registerKubernetesSpecificBeans(BeanDefinitionRegistry registry) {
        // Register K8s-specific beans
        // Example: health probes, configuration
    }

    private void registerLocalDevelopmentBeans(BeanDefinitionRegistry registry) {
        // Register local development beans
        // Example: mock services, debug tools
    }
}
