package com.enterprise.aiagent.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "conversations",
    indexes = {
        @Index(name = "idx_conv_user", columnList = "user_id"),
        @Index(name = "idx_conv_updated", columnList = "updated_at")
    }
)
public class ConversationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", nullable = false, unique = true, length = 64)
    private String conversationId;

    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    @Column(length = 512)
    private String title;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceNumber ASC")

    private List<MessageEntity> messages = new ArrayList<>();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<MessageEntity> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageEntity> messages) {
        this.messages = messages;
    }

    public ConversationEntity(){}

    public ConversationEntity(String conversationId, String userId, String title, Instant createdAt, Instant updatedAt,
            List<MessageEntity> messages) {
        this.conversationId = conversationId;
        this.userId = userId;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.messages = messages;
    }

    @Override
    public String toString() {
        return "ConversationEntity [id=" + id + ", conversationId=" + conversationId + ", userId=" + userId + ", title="
                + title + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + ", messages=" + messages + "]";
    }
    
}