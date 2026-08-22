package com.enterprise.aiagent.tool;

import com.enterprise.aiagent.client.OrderServiceClient;
import com.enterprise.aiagent.model.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTool {

    private final OrderServiceClient orderClient;

    @Tool(description = """
            Place a new order for a product on behalf of a user.
            Requires the user's ID, the product ID, and the quantity.
            Returns the created order with its ID, status, and total amount.
            Use this ONLY when the user explicitly confirms they want to
            place an order. Never place orders without explicit user consent.
            """)
    public OrderDto placeOrder(
            @ToolParam("The user's unique identifier") Long userId,
            @ToolParam("The product ID to order") Long productId,
            @ToolParam("The quantity to order (must be >= 1)") int quantity) {

        log.info("Tool invoked: placeOrder(userId={}, productId={}, qty={})", userId, productId, quantity);

        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        List<Map<String, Object>> items = List.of(
                Map.of("productId", productId, "quantity", quantity)
        );

        return orderClient.createOrder(userId, items);
    }

    @Tool(description = """
            Look up an existing order by its order ID.
            Returns the order details including items, total amount,
            status, and estimated delivery date.
            Use this when the user asks about a specific order.
            """)
    public OrderDto getOrderStatus(
            @ToolParam("The order ID (e.g., 'ORD-20250822-7841')") String orderId) {

        log.info("Tool invoked: getOrderStatus(orderId={})", orderId);
        return orderClient.getOrder(orderId);
    }

    @Tool(description = """
            Get all orders for a specific user, ordered by most recent first.
            Returns the user's order history with order IDs, dates,
            amounts, and statuses.
            """)
    public List<OrderDto> getUserOrders(
            @ToolParam("The user's unique identifier") Long userId) {

        log.info("Tool invoked: getUserOrders(userId={})", userId);
        return orderClient.getUserOrders(userId, 0, 20);
    }

    @Tool(description = """
            Cancel an existing order. The user must provide the order ID
            and optionally a reason for cancellation.
            Returns the updated order with cancelled status.
            Only use this when the user explicitly requests cancellation.
            """)
    public OrderDto cancelOrder(
            @ToolParam("The order ID to cancel") String orderId,
            @ToolParam("The reason for cancellation") String reason) {

        log.info("Tool invoked: cancelOrder(orderId={}, reason='{}')", orderId, reason);
        return orderClient.cancelOrder(orderId, reason);
    }

    @Tool(description = """
            Track the delivery status of an order.
            Returns tracking information including current location
            and estimated delivery date.
            """)
    public OrderDto trackOrder(
            @ToolParam("The order ID to track") String orderId) {

        log.info("Tool invoked: trackOrder(orderId={})", orderId);
        return orderClient.trackOrder(orderId);
    }
}