package com.enterprise.aiagent.model.dto;

import java.math.BigDecimal;

public record PaymentResult(
        String transactionId,
        String orderId,
        BigDecimal amount,
        String status,
        String paymentMethod
) {}