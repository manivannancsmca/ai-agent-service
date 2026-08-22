package com.enterprise.aiagent.client;

import com.enterprise.aiagent.model.dto.OrderDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
//import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@Slf4j
@Component
public class OrderServiceClient {

    private static final Logger log =
            LoggerFactory.getLogger(OrderServiceClient.class);

    private final RestClient restClient;

    public OrderServiceClient(RestClient orderRestClient) {
        this.restClient = orderRestClient;
    }

    @CircuitBreaker(name = "serviceCall", fallbackMethod = "createOrderFallback")
    //@Retry(name = "default")
    public OrderDto createOrder(Long userId, List<Map<String, Object>> items) {
        log.info("Creating order for userId={} with {} items", userId, items.size());

        Map<String, Object> body = Map.of(
                "userId", userId,
                "items", items
        );

        return restClient.post()
                .uri("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(OrderDto.class);
    }

    @CircuitBreaker(name = "serviceCall", fallbackMethod = "getOrderFallback")
    //@Retry(name = "default")
    public OrderDto getOrder(String orderId) {
        log.info("Fetching order {}", orderId);

        return restClient.get()
                .uri("/api/v1/orders/{orderId}", orderId)
                .retrieve()
                .body(OrderDto.class);
    }

    @CircuitBreaker(name = "serviceCall")
    public List<OrderDto> getUserOrders(Long userId, int page, int size) {
        log.info("Fetching orders for userId={}, page={}, size={}", userId, page, size);

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/orders/user/{userId}")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build(userId))
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<>() {});
    }

    @CircuitBreaker(name = "serviceCall")
    public OrderDto cancelOrder(String orderId, String reason) {
        log.info("Cancelling order {} with reason: {}", orderId, reason);

        return restClient.post()
                .uri("/api/v1/orders/{orderId}/cancel", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("reason", reason))
                .retrieve()
                .body(OrderDto.class);
    }

    @CircuitBreaker(name = "serviceCall")
    public OrderDto trackOrder(String orderId) {
        return restClient.get()
                .uri("/api/v1/orders/{orderId}/tracking", orderId)
                .retrieve()
                .body(OrderDto.class);
    }

    // ─── Fallback ─────────────────────────────────────

    private OrderDto createOrderFallback(Long userId, List<Map<String, Object>> items, Throwable t) {
        log.error("Order creation failed for userId={}: {}", userId, t.getMessage());
        throw new com.enterprise.aiagent.exception.ToolExecutionException(
                "Order creation is temporarily unavailable. Please try again shortly.", t);
    }

    private OrderDto getOrderFallback(String orderId, Throwable t) {
        log.error("Order fetch failed for orderId={}: {}", orderId, t.getMessage());
        return null;
    }
}