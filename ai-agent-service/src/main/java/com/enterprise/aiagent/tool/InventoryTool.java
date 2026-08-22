package com.enterprise.aiagent.tool;

import com.enterprise.aiagent.client.InventoryServiceClient;
import com.enterprise.aiagent.model.dto.InventoryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryTool {

    private final InventoryServiceClient inventoryClient;

    @Tool(
        description = """
            Check the current inventory/stock level for a specific product.
            Returns the available quantity, reserved quantity, warehouse location,
            and whether the product is running low on stock.
            Use this before recommending a product to verify it's available.
            """
    )
    public InventoryDto checkStock(
            @ToolParam(
                description = "The unique product ID to check inventory for",
                required = true
            )
            Long productId) {

        log.info("Tool invoked: checkStock(productId={})", productId);

        InventoryDto inventory = inventoryClient.checkInventory(productId);

        if (inventory == null) {
            log.warn("No inventory data found for product {}", productId);
        }

        return inventory;
    }

    @Tool(
        description = """
            Reserve stock for a product.
            Use this when an order is being placed to ensure the requested
            quantity is available.
            Returns true if the reservation succeeds, or false if there is
            insufficient stock.
            """
    )
    public boolean reserveStock(
            @ToolParam(
                description = "The unique product ID for which stock should be reserved",
                required = true
            )
            Long productId,

            @ToolParam(
                description = "The quantity of units to reserve. Must be greater than zero.",
                required = true
            )
            int quantity) {

        log.info(
            "Tool invoked: reserveStock(productId={}, qty={})",
            productId,
            quantity
        );

        if (quantity <= 0) {
            log.warn(
                "Invalid reservation quantity: productId={}, qty={}",
                productId,
                quantity
            );
            throw new IllegalArgumentException(
                "Reservation quantity must be greater than zero"
            );
        }

        return inventoryClient.reserveStock(productId, quantity);
    }
}