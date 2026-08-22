-- V1__init_inventory_schema.sql

CREATE TABLE IF NOT EXISTS inventory (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id           BIGINT       NOT NULL UNIQUE,
    available_quantity   INT          NOT NULL DEFAULT 0,
    reserved_quantity    INT          NOT NULL DEFAULT 0,
    total_quantity       INT          NOT NULL DEFAULT 0,
    warehouse_location   VARCHAR(100),
    low_stock_threshold  INT          NOT NULL DEFAULT 10,
    last_restocked_at    TIMESTAMP    NULL,
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_inventory_product (product_id),
    INDEX idx_inventory_low_stock (available_quantity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS stock_reservations (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id      BIGINT         NOT NULL,
    order_id        VARCHAR(32)    NOT NULL,
    quantity        INT            NOT NULL,
    status          VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    expires_at      TIMESTAMP      NOT NULL,
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_reservation_product (product_id),
    INDEX idx_reservation_order (order_id),
    INDEX idx_reservation_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed inventory for the products
INSERT INTO inventory (product_id, available_quantity, reserved_quantity, total_quantity, warehouse_location) VALUES
(1,  12, 0, 12, 'WH-WEST-A1'),
(2,  23, 0, 23, 'WH-EAST-B3'),
(3,   5, 0,  5, 'WH-EAST-B3'),
(4,   0, 0,  0, 'WH-WEST-A1'),
(5,  45, 0, 45, 'WH-WEST-C2'),
(6,  38, 0, 38, 'WH-WEST-C2'),
(7,   3, 0,  3, 'WH-EAST-D1'),
(8,  20, 0, 20, 'WH-EAST-A5'),
(9,  15, 0, 15, 'WH-WEST-B1'),
(10, 30, 0, 30, 'WH-WEST-B1'),
(11,  8, 0,  8, 'WH-EAST-C4'),
(12, 50, 0, 50, 'WH-WEST-A2'),
(13, 100,0, 100,'WH-WEST-A2'),
(14, 18, 0, 18, 'WH-EAST-B3'),
(15, 10, 0, 10, 'WH-EAST-B3');