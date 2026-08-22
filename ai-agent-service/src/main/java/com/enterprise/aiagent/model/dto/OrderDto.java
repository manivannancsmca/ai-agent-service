package com.enterprise.aiagent.model.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderDto(
        String orderId,
        Long userId,
        List<OrderItem> items,
        BigDecimal totalAmount,
        String status,
        Instant createdAt,
        Instant estimatedDelivery
) {
    public record OrderItem(
            Long productId,
            String productName,
            int quantity,
            BigDecimal unitPrice
    ) {}
}