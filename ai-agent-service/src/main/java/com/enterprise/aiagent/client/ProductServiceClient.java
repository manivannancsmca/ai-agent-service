package com.enterprise.aiagent.client;

import com.enterprise.aiagent.model.dto.ProductDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Component
public class ProductServiceClient {

    private final RestClient restClient;

    public ProductServiceClient(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("${services.product-service.url}").build();
    }

    /**
     * Constructor injection with the named RestClient bean.
     */
    public ProductServiceClient(RestClient productRestClient) {
        this.restClient = productRestClient;
    }

    @CircuitBreaker(name = "serviceCall", fallbackMethod = "searchProductsFallback")
    @Retry(name = "default")
    public List<ProductDto> searchProducts(String keyword, int page, int size) {
        log.info("Searching products with keyword='{}', page={}, size={}", keyword, page, size);

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/products/search")
                        .queryParam("keyword", keyword)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    @CircuitBreaker(name = "serviceCall", fallbackMethod = "getProductByIdFallback")
    @Retry(name = "default")
    public ProductDto getProductById(Long productId) {
        log.info("Fetching product details for id={}", productId);

        return restClient.get()
                .uri("/api/v1/products/{id}", productId)
                .retrieve()
                .body(ProductDto.class);
    }

    @CircuitBreaker(name = "serviceCall", fallbackMethod = "getProductsByCategoryFallback")
    public List<ProductDto> getProductsByCategory(String category, int limit) {
        log.info("Fetching products by category='{}', limit={}", category, limit);

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/products/category/{category}")
                        .queryParam("limit", limit)
                        .build(category))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    @CircuitBreaker(name = "serviceCall")
    public List<ProductDto> getTopRated(String category, int limit) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/products/top-rated")
                        .queryParam("category", category)
                        .queryParam("limit", limit)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    // ─── Fallback Methods ─────────────────────────────────────

    private List<ProductDto> searchProductsFallback(String keyword, int page, int size, Throwable t) {
        log.error("Product search failed for keyword='{}': {}", keyword, t.getMessage());
        return List.of(); // Return empty list; the agent will inform the user
    }

    private ProductDto getProductByIdFallback(Long productId, Throwable t) {
        log.error("Product fetch failed for id={}: {}", productId, t.getMessage());
        return null;
    }

    private List<ProductDto> getProductsByCategoryFallback(String category, int limit, Throwable t) {
        log.error("Product category fetch failed for category='{}': {}", category, t.getMessage());
        return List.of();
    }
}