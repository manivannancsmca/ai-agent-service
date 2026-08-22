package com.enterprise.aiagent.client;

import com.enterprise.aiagent.advisor.GuardrailAdvisor;
import com.enterprise.aiagent.model.dto.InventoryDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

//@Slf4j
@Component
public class InventoryServiceClient {

    private static final Logger log =
            LoggerFactory.getLogger(InventoryServiceClient.class);

    private final RestClient restClient;

    public InventoryServiceClient(RestClient inventoryRestClient) {
        this.restClient = inventoryRestClient;
    }

    @CircuitBreaker(name = "serviceCall")
    public InventoryDto checkInventory(Long productId) {
        log.info("Checking inventory for productId={}", productId);

        return restClient.get()
                .uri("/api/v1/inventory/{productId}", productId)
                .retrieve()
                .body(InventoryDto.class);
    }

    @CircuitBreaker(name = "serviceCall")
    public boolean reserveStock(Long productId, int quantity) {
        log.info("Reserving {} units for productId={}", quantity, productId);

        try {
            restClient.post()
                    .uri("/api/v1/inventory/{productId}/reserve", productId)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(java.util.Map.of("quantity", quantity))
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.warn("Failed to reserve stock for productId={}: {}", productId, e.getMessage());
            return false;
        }
    }
}