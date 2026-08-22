package com.enterprise.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PaymentRequest(
        @NotBlank String orderId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank String paymentMethod,
        String currency
) {
    public PaymentRequest {
        if (currency == null) currency = "USD";
    }
}