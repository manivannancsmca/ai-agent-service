package com.enterprise.payment.exception;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String transactionId) {
        super("Payment not found: " + transactionId);
    }
}