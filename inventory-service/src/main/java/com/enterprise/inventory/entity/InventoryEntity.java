package com.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "inventory")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;

    @Column(name = "available_quantity", nullable = false)
    @Builder.Default
    private Integer availableQuantity = 0;

    @Column(name = "reserved_quantity", nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Column(name = "total_quantity", nullable = false)
    @Builder.Default
    private Integer totalQuantity = 0;

    @Column(name = "warehouse_location", length = 100)
    private String warehouseLocation;

    @Column(name = "low_stock_threshold", nullable = false)
    @Builder.Default
    private Integer lowStockThreshold = 10;

    @Column(name = "last_restocked_at")
    private Instant lastRestockedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    public boolean isLowStock() {
        return availableQuantity <= lowStockThreshold;
    }

    public boolean canReserve(int quantity) {
        return availableQuantity >= quantity;
    }

    @PrePersist
    protected void onCreate() { this.createdAt = this.updatedAt = Instant.now(); }

    @PreUpdate
    protected void onUpdate() { this.updatedAt = Instant.now(); }
}