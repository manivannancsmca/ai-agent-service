package com.enterprise.order.exception;

public class OrderCannotBeCancelledException extends RuntimeException {
    public OrderCannotBeCancelledException(String orderId, String currentStatus) {
        super("Order " + orderId + " cannot be cancelled. Current status: " + currentStatus);
    }
}
