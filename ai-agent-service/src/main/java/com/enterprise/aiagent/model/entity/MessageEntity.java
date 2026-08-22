package com.enterprise.aiagent.model.entity;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_msg_conv", columnList = "conversation_id"),
        @Index(name = "idx_msg_seq", columnList = "conversation_id, sequenceNumber")
})

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

    public MessageEntity() {

    }

    public MessageEntity(ConversationEntity conversation, int sequenceNumber, String messageType, String content,
            String toolMetadata, Long tokenCount, Instant createdAt) {
        this.conversation = conversation;
        this.sequenceNumber = sequenceNumber;
        this.messageType = messageType;
        this.content = content;
        this.toolMetadata = toolMetadata;
        this.tokenCount = tokenCount;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public ConversationEntity getConversation() {
        return conversation;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getContent() {
        return content;
    }

    public String getToolMetadata() {
        return toolMetadata;
    }

    public Long getTokenCount() {
        return tokenCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    

    public void setId(Long id) {
        this.id = id;
    }

    public void setConversation(ConversationEntity conversation) {
        this.conversation = conversation;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setToolMetadata(String toolMetadata) {
        this.toolMetadata = toolMetadata;
    }

    public void setTokenCount(Long tokenCount) {
        this.tokenCount = tokenCount;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "MessageEntity [id=" + id + ", conversation=" + conversation + ", sequenceNumber=" + sequenceNumber
                + ", messageType=" + messageType + ", content=" + content + ", toolMetadata=" + toolMetadata
                + ", tokenCount=" + tokenCount + ", createdAt=" + createdAt + "]";
    }

    
}