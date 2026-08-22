package com.enterprise.inventory.dto;

public record InventoryDto(
        Long productId,
        int availableQuantity,
        int reservedQuantity,
        int totalQuantity,
        String warehouseLocation,
        boolean lowStock
) {}