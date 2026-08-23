package com.enterprise.order.service;

import com.enterprise.order.dto.CancelOrderRequest;
import com.enterprise.order.dto.CreateOrderRequest;
import com.enterprise.order.dto.OrderDto;
import com.enterprise.order.entity.OrderEntity;
import com.enterprise.order.entity.OrderItemEntity;
import com.enterprise.order.exception.OrderCannotBeCancelledException;
import com.enterprise.order.exception.OrderNotFoundException;
import com.enterprise.order.mapper.OrderMapper;
import com.enterprise.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {
        String orderId = generateOrderId();
        log.info("Creating order {} for user {}", orderId, request.userId());

        OrderEntity order = OrderEntity.builder()
                .orderId(orderId)
                .userId(request.userId())
                .totalAmount(BigDecimal.ZERO)
                .status(OrderEntity.OrderStatus.PENDING)
                .shippingAddress(request.shippingAddress())
                .estimatedDelivery(Instant.now().plus(
                        ThreadLocalRandom.current().nextLong(3, 8), ChronoUnit.DAYS))
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (CreateOrderRequest.OrderItemRequest itemReq : request.items()) {
            // In production, the product name and price would come from Product Service.
            // Here we use placeholder values since the AI Agent already has this data.
            BigDecimal unitPrice = BigDecimal.valueOf(0); // Will be populated by agent context
            String productName = "Product #" + itemReq.productId();

            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.quantity()));

            OrderItemEntity item = OrderItemEntity.builder()
                    .productId(itemReq.productId())
                    .productName(productName)
                    .quantity(itemReq.quantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();

            order.addItem(item);
            total = total.add(subtotal);
        }

        order.setTotalAmount(total);
        OrderEntity saved = orderRepository.save(order);

        log.info("Order {} created successfully. Total: {}", orderId, total);
        return orderMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrder(String orderId) {
        log.info("Fetching order {}", orderId);
        OrderEntity order = findByOrderId(orderId);
        return orderMapper.toDto(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getUserOrders(Long userId, int page, int size) {
        log.info("Fetching orders for user {}, page={}, size={}", userId, page, size);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(orderMapper::toDto);
    }

    @Transactional
    public OrderDto cancelOrder(String orderId, CancelOrderRequest request) {
        log.info("Cancelling order {}: reason='{}'", orderId, request.reason());

        OrderEntity order = findByOrderId(orderId);

        // Business rule: only PENDING or CONFIRMED orders can be cancelled
        if (order.getStatus() != OrderEntity.OrderStatus.PENDING
                && order.getStatus() != OrderEntity.OrderStatus.CONFIRMED) {
            throw new OrderCannotBeCancelledException(orderId, order.getStatus().name());
        }

        order.setStatus(OrderEntity.OrderStatus.CANCELLED);
        order.setCancellationReason(request.reason());

        OrderEntity saved = orderRepository.save(order);
        log.info("Order {} cancelled successfully", orderId);

        return orderMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public OrderDto trackOrder(String orderId) {
        log.info("Tracking order {}", orderId);
        // In production, this would integrate with a shipping provider API.
        return orderMapper.toDto(findByOrderId(orderId));
    }

    @Transactional
    public OrderDto updateOrderStatus(String orderId, OrderEntity.OrderStatus newStatus) {
        OrderEntity order = findByOrderId(orderId);
        order.setStatus(newStatus);
        return orderMapper.toDto(orderRepository.save(order));
    }

    private OrderEntity findByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private String generateOrderId() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 13).toUpperCase();
    }
}
