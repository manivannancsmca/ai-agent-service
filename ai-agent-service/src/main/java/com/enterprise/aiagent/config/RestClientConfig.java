package com.enterprise.aiagent.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${services.product-service.url:http://product-service:8081}")
    private String productServiceUrl;

    @Value("${services.order-service.url:http://order-service:8082}")
    private String orderServiceUrl;

    @Value("${services.inventory-service.url:http://inventory-service:8083}")
    private String inventoryServiceUrl;

    @Value("${services.payment-service.url:http://payment-service:8084}")
    private String paymentServiceUrl;

    /**
     * Customizes the default RestClient with connection pool settings and timeouts.
     * This applies globally to all RestClient beans unless overridden.
     */
    @Bean
    RestClientCustomizer restClientCustomizer() {
        return builder -> builder
                .requestFactory(
                        new BufferingClientHttpRequestFactory(
                                ClientHttpRequestFactories.get(
                                        ClientHttpRequestFactorySettings.DEFAULTS
                                                .withConnectTimeout(Duration.ofSeconds(5))
                                                .withReadTimeout(Duration.ofSeconds(30))
                                )
                        )
                );
    }

    @Bean(name = "productRestClient")
    RestClient productRestClient(RestClient.Builder builder, ObservationRegistry observationRegistry) {
        return builder
                .baseUrl(productServiceUrl)
                .observationRegistry(observationRegistry)
                .build();
    }

    @Bean(name = "orderRestClient")
    RestClient orderRestClient(RestClient.Builder builder, ObservationRegistry observationRegistry) {
        return builder
                .baseUrl(orderServiceUrl)
                .observationRegistry(observationRegistry)
                .build();
    }

    @Bean(name = "inventoryRestClient")
    RestClient inventoryRestClient(RestClient.Builder builder, ObservationRegistry observationRegistry) {
        return builder
                .baseUrl(inventoryServiceUrl)
                .observationRegistry(observationRegistry)
                .build();
    }

    @Bean(name = "paymentRestClient")
    RestClient paymentRestClient(RestClient.Builder builder, ObservationRegistry observationRegistry) {
        return builder
                .baseUrl(paymentServiceUrl)
                .observationRegistry(observationRegistry)
                .build();
    }
}