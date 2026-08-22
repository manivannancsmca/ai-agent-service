package com.enterprise.aiagent.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    /**
     * Circuit breaker for LLM calls.
     * LLM APIs can be slow or unavailable — circuit breaker prevents cascading failures.
     */
    @Bean
    public CircuitBreakerConfigCustomizer llmCircuitBreakerCustomizer() {
        return CircuitBreakerConfigCustomizer.of("llmCall", builder -> builder
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(3)
                .recordExceptions(
                        java.io.IOException.class,
                        java.net.SocketTimeoutException.class,
                        org.springframework.web.client.HttpServerErrorException.class
                )
                .ignoreExceptions(
                        org.springframework.web.client.HttpClientErrorException.BadRequest.class
                )
        );
    }

    /**
     * Circuit breaker for downstream microservice calls.
     */
    @Bean
    public CircuitBreakerConfigCustomizer serviceCircuitBreakerCustomizer() {
        return CircuitBreakerConfigCustomizer.of("serviceCall", builder -> builder
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(20)
                .minimumNumberOfCalls(10)
                .failureRateThreshold(40)
                .waitDurationInOpenState(Duration.ofSeconds(15))
                .permittedNumberOfCallsInHalfOpenState(5)
        );
    }

    /**
     * Retry configuration for transient failures.
     */
    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(1))
                .exponentialBackoff(2, Duration.ofSeconds(10))
                .retryExceptions(
                        java.io.IOException.class,
                        org.springframework.web.client.HttpServerErrorException.class
                )
                .ignoreExceptions(
                        org.springframework.web.client.HttpClientErrorException.class
                )
                .build();

        return RetryRegistry.of(config);
    }
}