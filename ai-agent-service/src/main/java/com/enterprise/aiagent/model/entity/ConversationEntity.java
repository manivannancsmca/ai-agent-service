package com.enterprise.aiagent.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversations", indexes = {
        @Index(name = "idx_conv_user", columnList = "userId"),
        @Index(name = "idx_conv_updated", columnList = "updatedAt")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConversationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String conversationId;

    @Column(nullable = false, length = 128)
    private String userId;

    @Column(length = 512)
    private String title;

    @Column(nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceNumber ASC")
    @Builder.Default
    private List<MessageEntity> messages = new ArrayList<>();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}