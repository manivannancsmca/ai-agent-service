package com.enterprise.aiagent.agent;

import com.enterprise.aiagent.tool.OrderTool;
import com.enterprise.aiagent.tool.PaymentTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Specialized agent for order lifecycle management.
 *
 * Handles: placing orders, tracking, cancellation, payment, refunds.
 * Uses a lower temperature (0.3) for more precise, deterministic responses
 * since order operations are high-stakes.
 *
 * Agentic loop example:
 * 1. User says "I want to order product #42, quantity 2"
 * 2. Agent confirms details with the user (via conversation context)
 * 3. Agent calls placeOrder(userId, productId, quantity)
 * 4. Agent calls processPayment(orderId, amount, method)
 * 5. Agent returns order confirmation
 */
@Component
public class OrderAgent extends BaseAgent {

    public OrderAgent(@Qualifier("orderChatClient") ChatClient orderChatClient) {
        super(orderChatClient);
    }

    @Override
    public String getAgentName() {
        return "OrderAgent";
    }
}