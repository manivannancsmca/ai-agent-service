package com.enterprise.order.controller;

import com.enterprise.order.dto.CancelOrderRequest;
import com.enterprise.order.dto.CreateOrderRequest;
import com.enterprise.order.dto.OrderDto;
import com.enterprise.order.entity.OrderEntity;
import com.enterprise.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Create a new order.
     * Called by the AI Agent's OrderTool.placeOrder()
     */
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderDto order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * Get order details by order ID.
     * Called by the AI Agent's OrderTool.getOrderStatus()
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    /**
     * Get all orders for a user.
     * Called by the AI Agent's OrderTool.getUserOrders()
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<OrderDto>> getUserOrders(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(orderService.getUserOrders(userId, page, size));
    }

    /**
     * Cancel an order.
     * Called by the AI Agent's OrderTool.cancelOrder()
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(
            @PathVariable String orderId,
            @RequestBody CancelOrderRequest request) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId, request));
    }

    /**
     * Track an order.
     * Called by the AI Agent's OrderTool.trackOrder()
     */
    @GetMapping("/{orderId}/tracking")
    public ResponseEntity<OrderDto> trackOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.trackOrder(orderId));
    }

    /**
     * Update order status (internal API for other services).
     */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderDto> updateStatus(
            @PathVariable String orderId,
            @RequestParam OrderEntity.OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }
}
