-- V1__init_order_schema.sql

CREATE TABLE IF NOT EXISTS orders (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id            VARCHAR(32)    NOT NULL UNIQUE,
    user_id             BIGINT         NOT NULL,
    total_amount        DECIMAL(12,2)  NOT NULL,
    status              VARCHAR(30)    NOT NULL DEFAULT 'PENDING',
    shipping_address    TEXT,
    estimated_delivery  TIMESTAMP      NULL,
    cancellation_reason VARCHAR(512)   NULL,
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_order_user (user_id),
    INDEX idx_order_status (status),
    INDEX idx_order_created (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS order_items (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT         NOT NULL,
    product_id      BIGINT         NOT NULL,
    product_name    VARCHAR(255)   NOT NULL,
    quantity        INT            NOT NULL,
    unit_price      DECIMAL(10,2)  NOT NULL,
    subtotal        DECIMAL(12,2)  NOT NULL,

    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_item_order (order_id),
    INDEX idx_item_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;