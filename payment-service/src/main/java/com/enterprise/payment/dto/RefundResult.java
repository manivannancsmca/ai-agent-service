package com.enterprise.payment.dto;

import java.math.BigDecimal;

public record RefundResult(
        String refundTransactionId,
        String originalTransactionId,
        BigDecimal amount,
        String status,
        String message
) {}