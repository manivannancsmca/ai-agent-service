package com.enterprise.aiagent.model.entity;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_msg_conv", columnList = "conversation_id"),
        @Index(name = "idx_msg_seq", columnList = "conversation_id, sequenceNumber")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ConversationEntity conversation;

    @Column(nullable = false)
    private int sequenceNumber;

    /**
     * Message type: SYSTEM, USER, ASSISTANT, TOOL
     */
    @Column(nullable = false, length = 20)
    private String messageType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Optional: tool call metadata as JSON for tool messages
     */
    @Column(columnDefinition = "JSON")
    private String toolMetadata;

    @Column(nullable = false)
    @Builder.Default
    private Long tokenCount = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}