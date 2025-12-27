package com.apress.crm.customer;

import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Profile( "mtls")
@Configuration
public class ClientConfig {

    @Bean
    public ManagementClient managementClient(RestClient.Builder builder, SslBundles sslBundles) {
        // Apply the 'customer-client' SSL Bundle to the RestClient
        SslBundle bundle = sslBundles.getBundle("customer-client");

        // In a real implementation, you would configure the ClientHttpRequestFactory with bundle.getSslContext()
        
        return HttpServiceProxyFactory.builderFor(
                RestClientAdapter.create(
                        builder.build()
                )
        ).build().createClient(ManagementClient.class);
    }
}

