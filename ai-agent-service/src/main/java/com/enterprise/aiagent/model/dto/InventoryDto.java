package com.enterprise.aiagent.model.dto;

public record InventoryDto(
        Long productId,
        int availableQuantity,
        int reservedQuantity,
        String warehouseLocation,
        boolean lowStock
) {}