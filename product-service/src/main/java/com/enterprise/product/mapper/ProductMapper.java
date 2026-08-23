package com.enterprise.product.mapper;

import com.enterprise.product.dto.ProductDto;
import com.enterprise.product.entity.ProductEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductDto toDto(ProductEntity entity) {
        return new ProductDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getCategory(),
                entity.getBrand(),
                entity.getRating(),
                entity.getReviewCount(),
                entity.getImageUrl(),
                entity.getActive()
        );
    }
}
