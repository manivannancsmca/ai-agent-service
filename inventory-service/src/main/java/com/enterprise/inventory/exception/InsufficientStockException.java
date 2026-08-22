package com.enterprise.inventory.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(Long productId, int available, int requested) {
        super(String.format("Insufficient stock for product %d: available=%d, requested=%d",
                productId, available, requested));
    }
}