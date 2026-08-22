package com.enterprise.inventory.service;

import com.enterprise.inventory.dto.InventoryDto;
import com.enterprise.inventory.dto.ReserveStockRequest;
import com.enterprise.inventory.entity.InventoryEntity;
import com.enterprise.inventory.exception.InsufficientStockException;
import com.enterprise.inventory.exception.InventoryNotFoundException;
import com.enterprise.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    /**
     * Check stock for a product. Returns inventory details including
     * available quantity, reserved quantity, and low-stock flag.
     *
     * Called by the AI Agent's InventoryTool.checkStock()
     */
    @Transactional(readOnly = true)
    public InventoryDto checkInventory(Long productId) {
        log.info("Checking inventory for product {}", productId);

        InventoryEntity entity = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));

        return toDto(entity);
    }

    /**
     * Reserve stock for an order. Uses pessimistic locking to prevent overselling.
     *
     * Flow:
     * 1. Lock the inventory row for this product
     * 2. Check if sufficient stock is available
     * 3. Move quantity from available to reserved
     * 4. Return success/failure
     *
     * Called by the AI Agent's InventoryTool.reserveStock()
     */
    @Transactional
    public boolean reserveStock(Long productId, ReserveStockRequest request) {
        log.info("Reserving {} units of product {} for order '{}'",
                request.quantity(), productId, request.orderId());

        // Pessimistic lock prevents race conditions
        InventoryEntity inventory = inventoryRepository.findByProductIdWithLock(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));

        if (!inventory.canReserve(request.quantity())) {
            log.warn("Insufficient stock for product {}: available={}, requested={}",
                    productId, inventory.getAvailableQuantity(), request.quantity());
            return false;
        }

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - request.quantity());
        inventory.setReservedQuantity(inventory.getReservedQuantity() + request.quantity());
        inventoryRepository.save(inventory);

        log.info("Reserved {} units of product {}. Available: {}, Reserved: {}",
                request.quantity(), productId,
                inventory.getAvailableQuantity(), inventory.getReservedQuantity());

        return true;
    }

    /**
     * Release reserved stock (e.g., when an order is cancelled).
     */
    @Transactional
    public void releaseStock(Long productId, int quantity) {
        log.info("Releasing {} units of reserved stock for product {}", quantity, productId);

        InventoryEntity inventory = inventoryRepository.findByProductIdWithLock(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));

        int releaseAmount = Math.min(quantity, inventory.getReservedQuantity());
        inventory.setReservedQuantity(inventory.getReservedQuantity() - releaseAmount);
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + releaseAmount);
        inventoryRepository.save(inventory);

        log.info("Released {} units for product {}. Available: {}",
                releaseAmount, productId, inventory.getAvailableQuantity());
    }

    /**
     * Confirm stock deduction (e.g., when payment succeeds).
     * Removes from reserved count permanently.
     */
    @Transactional
    public void confirmDeduction(Long productId, int quantity) {
        log.info("Confirming deduction of {} units for product {}", quantity, productId);

        InventoryEntity inventory = inventoryRepository.findByProductIdWithLock(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));

        int deductAmount = Math.min(quantity, inventory.getReservedQuantity());
        inventory.setReservedQuantity(inventory.getReservedQuantity() - deductAmount);
        inventory.setTotalQuantity(inventory.getTotalQuantity() - deductAmount);
        inventoryRepository.save(inventory);
    }

    private InventoryDto toDto(InventoryEntity entity) {
        return new InventoryDto(
                entity.getProductId(),
                entity.getAvailableQuantity(),
                entity.getReservedQuantity(),
                entity.getTotalQuantity(),
                entity.getWarehouseLocation(),
                entity.isLowStock()
        );
    }
}