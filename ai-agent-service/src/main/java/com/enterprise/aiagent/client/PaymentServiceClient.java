package com.enterprise.aiagent.client;

import com.enterprise.aiagent.model.dto.PaymentResult;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
public class PaymentServiceClient {

    private final RestClient restClient;

    public PaymentServiceClient(RestClient paymentRestClient) {
        this.restClient = paymentRestClient;
    }

    @CircuitBreaker(name = "serviceCall", fallbackMethod = "processPaymentFallback")
    @Retry(name = "default")
    public PaymentResult processPayment(String orderId, BigDecimal amount, String paymentMethod) {
        log.info("Processing payment for orderId={}, amount={}, method={}", orderId, amount, paymentMethod);

        Map<String, Object> body = Map.of(
                "orderId", orderId,
                "amount", amount,
                "paymentMethod", paymentMethod
        );

        return restClient.post()
                .uri("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(PaymentResult.class);
    }

    @CircuitBreaker(name = "serviceCall")
    public PaymentResult refundPayment(String transactionId) {
        log.info("Processing refund for transactionId={}", transactionId);

        return restClient.post()
                .uri("/api/v1/payments/{transactionId}/refund", transactionId)
                .retrieve()
                .body(PaymentResult.class);
    }

    private PaymentResult processPaymentFallback(String orderId, BigDecimal amount,
                                                  String paymentMethod, Throwable t) {
        log.error("Payment processing failed for orderId={}: {}", orderId, t.getMessage());
        throw new com.enterprise.aiagent.exception.ToolExecutionException(
                "Payment processing is temporarily unavailable. Your order has been saved. " +
                "Please try completing the payment again in a few minutes.", t);
    }
}