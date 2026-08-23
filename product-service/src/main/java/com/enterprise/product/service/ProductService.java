package com.enterprise.product.service;

import com.enterprise.product.dto.ProductDto;
import com.enterprise.product.entity.ProductEntity;
import com.enterprise.product.exception.ProductNotFoundException;
import com.enterprise.product.mapper.ProductMapper;
import com.enterprise.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public Page<ProductDto> searchProducts(String keyword, int page, int size) {
        log.info("Searching products: keyword='{}', page={}, size={}", keyword, page, size);
        PageRequest pageRequest = PageRequest.of(page, Math.min(size, 50));

        if (keyword == null || keyword.isBlank()) {
            return productRepository.findAll(pageRequest).map(productMapper::toDto);
        }

        return productRepository.searchByKeyword(keyword.trim(), pageRequest)
                .map(productMapper::toDto);
    }

    @Cacheable(value = "productCache", key = "#productId")
    public ProductDto getProductById(Long productId) {
        log.info("Fetching product: id={}", productId);
        ProductEntity entity = productRepository.findById(productId)
                .filter(ProductEntity::getActive)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return productMapper.toDto(entity);
    }

    public List<ProductDto> getProductsByCategory(String category, int limit) {
        log.info("Fetching products by category: '{}', limit={}", category, limit);
        PageRequest pageRequest = PageRequest.of(0, Math.min(limit, 50));
        return productRepository.findByCategoryAndActiveTrueOrderByRatingDesc(category, pageRequest)
                .map(productMapper::toDto)
                .getContent();
    }

    public List<ProductDto> getTopRated(String category, int limit) {
        log.info("Fetching top-rated products: category='{}', limit={}", category, limit);
        PageRequest pageRequest = PageRequest.of(0, Math.min(limit, 20));

        if (category != null && !category.isBlank()) {
            return productRepository
                    .findTopByCategoryAndActiveTrueOrderByRatingDescReviewCountDesc(category, pageRequest)
                    .stream()
                    .map(productMapper::toDto)
                    .toList();
        }

        return productRepository
                .findByActiveTrueOrderByRatingDescReviewCountDesc(pageRequest)
                .stream()
                .map(productMapper::toDto)
                .toList();
    }

    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }
}
