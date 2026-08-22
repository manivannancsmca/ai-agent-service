-- V1__init_payment_schema.sql

CREATE TABLE IF NOT EXISTS payments (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id   VARCHAR(36)    NOT NULL UNIQUE,
    order_id         VARCHAR(32)    NOT NULL,
    amount           DECIMAL(12,2)  NOT NULL,
    currency         VARCHAR(3)     NOT NULL DEFAULT 'USD',
    payment_method   VARCHAR(30)    NOT NULL,
    status           VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    failure_reason   VARCHAR(512)   NULL,
    refund_transaction_id VARCHAR(36) NULL,
    processed_at     TIMESTAMP      NULL,
    created_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_payment_order (order_id),
    INDEX idx_payment_status (status),
    INDEX idx_payment_transaction (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;