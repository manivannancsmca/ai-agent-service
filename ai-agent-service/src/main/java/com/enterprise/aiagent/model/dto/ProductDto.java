package com.enterprise.aiagent.model.dto;

import java.math.BigDecimal;

public record ProductDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String category,
        double rating,
        int reviewCount,
        boolean inStock
) {}