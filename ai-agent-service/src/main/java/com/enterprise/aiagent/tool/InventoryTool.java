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

    @Tool(description = """
            Check the current inventory/stock level for a specific product.
            Returns the available quantity, reserved quantity, warehouse location,
            and whether the product is running low on stock.
            Use this before recommending a product to verify it's available.
            """)
    public InventoryDto checkStock(
            @ToolParam("The product ID to check inventory for") Long productId) {

        log.info("Tool invoked: checkStock(productId={})", productId);
        InventoryDto inventory = inventoryClient.checkInventory(productId);

        if (inventory == null) {
            log.warn("No inventory data found for product {}", productId);
        }
        return inventory;
    }

    @Tool(description = """
            Reserve stock for a product. This is called internally when
            an order is being placed to ensure the item is available.
            Returns true if reservation succeeded, false if insufficient stock.
            """)
    public boolean reserveStock(
            @ToolParam("The product ID") Long productId,
            @ToolParam("The quantity to reserve") int quantity) {

        log.info("Tool invoked: reserveStock(productId={}, qty={})", productId, quantity);
        return inventoryClient.reserveStock(productId, quantity);
    }
}