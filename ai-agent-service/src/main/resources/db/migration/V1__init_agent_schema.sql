-- V1__init_agent_schema.sql

CREATE TABLE IF NOT EXISTS conversations (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id VARCHAR(64)  NOT NULL UNIQUE,
    user_id         VARCHAR(128) NOT NULL,
    title           VARCHAR(512),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_conv_user (user_id),
    INDEX idx_conv_updated (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS messages (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT       NOT NULL,
    sequence_number INT          NOT NULL,
    message_type    VARCHAR(20)  NOT NULL,
    content         TEXT         NOT NULL,
    tool_metadata   JSON,
    token_count     BIGINT       NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    INDEX idx_msg_conv (conversation_id),
    INDEX idx_msg_seq (conversation_id, sequence_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Schema for Spring AI's built-in chat memory (if using JdbcChatMemoryRepository)
CREATE TABLE IF NOT EXISTS SPRING_AI_CHAT_MEMORY (
    conversation_id VARCHAR(255) NOT NULL,
    content         TEXT         NOT NULL,
    type            VARCHAR(10)  NOT NULL,
    "timestamp"     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_spring_ai_chat_memory_conversation_id (conversation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;