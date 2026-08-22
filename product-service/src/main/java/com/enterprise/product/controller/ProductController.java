package com.enterprise.product.controller;

import com.enterprise.product.dto.ProductDto;
import com.enterprise.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Search products by keyword. Searches across name, description,
     * brand, and tags. Returns paginated results sorted by relevance.
     *
     * Called by the AI Agent's ProductTool.searchProducts()
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductDto>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.searchProducts(keyword, page, size));
    }

    /**
     * Get full details for a single product.
     *
     * Called by the AI Agent's ProductTool.getProductDetails()
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * Get products filtered by category.
     *
     * Called by the AI Agent's ProductTool.getProductsByCategory()
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductDto>> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(productService.getProductsByCategory(category, limit));
    }

    /**
     * Get top-rated products, optionally filtered by category.
     *
     * Called by the AI Agent's ProductTool.getTopRatedProducts()
     */
    @GetMapping("/top-rated")
    public ResponseEntity<List<ProductDto>> getTopRated(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(productService.getTopRated(category, limit));
    }

    /**
     * Get all available product categories.
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }
}