package com.enterprise.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderDto(
        String orderId,
        Long userId,
        List<OrderItemDto> items,
        BigDecimal totalAmount,
        String status,
        String shippingAddress,
        Instant createdAt,
        Instant estimatedDelivery,
        String cancellationReason
) {
    public record OrderItemDto(
            Long productId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {}
}
