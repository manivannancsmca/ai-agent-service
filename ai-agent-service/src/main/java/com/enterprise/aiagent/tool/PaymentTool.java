package com.enterprise.aiagent.tool;

import com.enterprise.aiagent.client.OrderServiceClient;
import com.enterprise.aiagent.client.PaymentServiceClient;
import com.enterprise.aiagent.model.dto.PaymentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

//@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentTool {

    private static final Logger log = LoggerFactory.getLogger(PaymentTool.class);

    private final PaymentServiceClient paymentClient;

    @Tool(description = """
            Process a payment for an order. Requires the order ID,
            payment amount, and payment method (e.g., 'credit_card',
            'paypal', 'apple_pay').
            Returns a transaction result with confirmation details.
            Only use this when the user has explicitly confirmed the
            payment details. NEVER process payments without user consent.
            """)
    public PaymentResult processPayment(
            @ToolParam(description = "The order ID this payment is for", required = true) String orderId,
            @ToolParam(description = "The payment amount", required = true) BigDecimal amount,
            @ToolParam(description = "Payment method: 'credit_card', 'debit_card', 'paypal', 'apple_pay'", required = true) String paymentMethod) {

        log.info("Tool invoked: processPayment(orderId={}, amount={}, method={})",
                orderId, amount, paymentMethod);
        return paymentClient.processPayment(orderId, amount, paymentMethod);
    }

    @Tool(description = """
            Refund a payment by its transaction ID.
            Use this when a user requests a refund for a cancelled order.
            Returns the refund confirmation details.
            """)
    public PaymentResult refundPayment(
            @ToolParam(description = "The original payment transaction ID", required = true) String transactionId) {

        log.info("Tool invoked: refundPayment(transactionId={})", transactionId);
        return paymentClient.refundPayment(transactionId);
    }
}