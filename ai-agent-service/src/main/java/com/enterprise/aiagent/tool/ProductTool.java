package com.enterprise.aiagent.tool;

import com.enterprise.aiagent.client.OrderServiceClient;
import com.enterprise.aiagent.client.ProductServiceClient;
import com.enterprise.aiagent.model.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Tool that gives the AI Agent access to the Product Service.
 * The LLM calls these methods when it needs product information
 * to answer user questions.
 */
//@Slf4j
@Component
public class ProductTool {

    private static final Logger log =
            LoggerFactory.getLogger(ProductTool.class);

    private final ProductServiceClient productClient;

    public ProductTool(ProductServiceClient productClient) {
        this.productClient = productClient;
    }

    @Tool(description = """
            Search for products in the catalog using a keyword.
            Returns a list of matching products with their name, price, rating,
            and availability. Use this when the user asks to find, search, or
            discover products. Results are limited to 10 items by default.
            """)
    public List<ProductDto> searchProducts(
            @ToolParam(description = "The search keyword — e.g. 'laptop', 'wireless headphones', 'running shoes'",
                required = true)
            String keyword) {

        log.info("Tool invoked: searchProducts(keyword='{}')", keyword);
        List<ProductDto> results = productClient.searchProducts(keyword, 0, 10);
        log.info("searchProducts returned {} results for '{}'", results.size(), keyword);
        return results;
    }

    @Tool(description = """
            Get detailed information about a specific product by its ID.
            Returns the product's full details including price, description,
            category, rating, and stock status. Use this after identifying
            a specific product the user is interested in.
            """)
    public ProductDto getProductDetails(
            @ToolParam(description = "The unique numeric product identifier",
                required = true) Long productId) {

        log.info("Tool invoked: getProductDetails(productId={})", productId);
        ProductDto product = productClient.getProductById(productId);
        if (product == null) {
            log.warn("Product not found: {}", productId);
        }
        return product;
    }

    @Tool(description = """
            Get products in a specific category, sorted by relevance.
            Use this when the user asks about a product category
            (e.g., 'electronics', 'clothing', 'home & garden').
            Returns up to the specified limit of products.
            """)
    public List<ProductDto> getProductsByCategory(
            @ToolParam(description = "The product category name",
                required = true) String category,
            @ToolParam(description = "Maximum number of products to return (default 10)", required = true) int limit) {

        log.info("Tool invoked: getProductsByCategory(category='{}', limit={})", category, limit);
        return productClient.getProductsByCategory(category, Math.min(limit, 50));
    }

    @Tool(description = """
            Get the top-rated products in a specific category.
            Use this when the user asks for the 'best', 'top-rated',
            or 'most popular' products. Returns products sorted by
            rating and review count.
            """)
    public List<ProductDto> getTopRatedProducts(
            @ToolParam(description = "The product category",
                required = true) String category,
            @ToolParam(description = "Number of top products to return",
                required = true) int limit) {

        log.info("Tool invoked: getTopRatedProducts(category='{}', limit={})", category, limit);
        return productClient.getTopRated(category, Math.min(limit, 20));
    }
}