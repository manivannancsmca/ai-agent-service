package com.enterprise.product.dto;

import java.math.BigDecimal;

public record ProductDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String category,
        String brand,
        double rating,
        int reviewCount,
        String imageUrl,
        boolean inStock
) {}