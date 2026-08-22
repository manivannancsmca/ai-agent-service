package com.enterprise.inventory.exception;

public class InventoryNotFoundException extends RuntimeException {
    public InventoryNotFoundException(Long productId) {
        super("No inventory record found for product: " + productId);
    }
}