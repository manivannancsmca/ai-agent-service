package com.enterprise.inventory.controller;

import com.enterprise.inventory.dto.InventoryDto;
import com.enterprise.inventory.dto.ReserveStockRequest;
import com.enterprise.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Check inventory for a product.
     * Called by the AI Agent's InventoryTool.checkStock()
     */
    @GetMapping("/{productId}")
    public ResponseEntity<InventoryDto> checkInventory(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.checkInventory(productId));
    }

    /**
     * Reserve stock for a product.
     * Called by the AI Agent's InventoryTool.reserveStock()
     */
    @PostMapping("/{productId}/reserve")
    public ResponseEntity<StockReservationResponse> reserveStock(
            @PathVariable Long productId,
            @Valid @RequestBody ReserveStockRequest request) {

        boolean success = inventoryService.reserveStock(productId, request);
        if (success) {
            return ResponseEntity.ok(new StockReservationResponse(true, "Stock reserved successfully"));
        } else {
            return ResponseEntity.ok(new StockReservationResponse(false, "Insufficient stock available"));
        }
    }

    /**
     * Release reserved stock.
     */
    @PostMapping("/{productId}/release")
    public ResponseEntity<Void> releaseStock(
            @PathVariable Long productId,
            @RequestParam int quantity) {
        inventoryService.releaseStock(productId, quantity);
        return ResponseEntity.ok().build();
    }

    /**
     * Confirm stock deduction after successful payment.
     */
    @PostMapping("/{productId}/confirm")
    public ResponseEntity<Void> confirmDeduction(
            @PathVariable Long productId,
            @RequestParam int quantity) {
        inventoryService.confirmDeduction(productId, quantity);
        return ResponseEntity.ok().build();
    }

    public record StockReservationResponse(boolean success, String message) {}
}