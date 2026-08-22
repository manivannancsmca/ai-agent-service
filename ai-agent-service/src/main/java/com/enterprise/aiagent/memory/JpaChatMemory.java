package com.enterprise.aiagent.memory;

import com.enterprise.aiagent.model.entity.ConversationEntity;
import com.enterprise.aiagent.model.entity.MessageEntity;
import com.enterprise.aiagent.repository.ConversationRepository;
import com.enterprise.aiagent.repository.MessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Production-grade ChatMemory backed by MySQL via JPA.
 *
 * This stores the full conversation history including tool call results,
 * which is critical for the agentic loop — the LLM needs to remember
 * what tools it already called and what results it received.
 *
 * Implements the sliding window strategy: keeps the system message
 * plus the last N exchanges to stay within token budgets.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JpaChatMemory implements ChatMemory {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    private static final int DEFAULT_WINDOW_SIZE = 40; // messages

    @Override
    @Transactional
    public void add(String conversationId, List<Message> messages) {
        ConversationEntity conversation = getOrCreateConversation(conversationId);
        int currentSequence = conversationRepository.countMessagesByConversationId(conversationId);

        for (Message message : messages) {
            currentSequence++;
            MessageEntity entity = MessageEntity.builder()
                    .conversation(conversation)
                    .sequenceNumber(currentSequence)
                    .messageType(message.getMessageType().name())
                    .content(message.getText())
                    .tokenCount(estimateTokenCount(message.getText()))
                    .toolMetadata(serializeToolMetadata(message))
                    .build();
            messageRepository.save(entity);
        }

        log.debug("Added {} messages to conversation {}", messages.size(), conversationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> get(String conversationId, int lastN) {
        int windowSize = lastN > 0 ? lastN : DEFAULT_WINDOW_SIZE;

        List<MessageEntity> entities = messageRepository
                .findLastNByConversationId(conversationId, windowSize);

        List<Message> messages = new ArrayList<>(entities.size());
        for (MessageEntity entity : entities.reversed()) { // reversed because DESC query
            messages.add(deserializeMessage(entity));
        }

        return messages;
    }

    @Override
    @Transactional
    public void clear(@NonNull String conversationId) {
        conversationRepository.findByConversationId(conversationId)
                .ifPresent(conv -> {
                    conv.getMessages().clear();
                    conversationRepository.save(conv);
                    log.info("Cleared conversation {}", conversationId);
                });
    }

    @Transactional(readOnly = true)
    public int size(String conversationId) {
        return (int) conversationRepository.countMessagesByConversationId(conversationId);
    }

    // ─── Internal Helpers ────────────────────────────────────────

    private ConversationEntity getOrCreateConversation(String conversationId) {
        return conversationRepository.findByConversationId(conversationId)
                .orElseGet(() -> conversationRepository.save(
                        ConversationEntity.builder()
                                .conversationId(conversationId)
                                .userId("system") // Will be updated by service layer
                                .title("Auto-created conversation")
                                .build()
                ));
    }

    private Message deserializeMessage(MessageEntity entity) {
        return switch (MessageType.valueOf(entity.getMessageType())) {
            case SYSTEM -> new SystemMessage(entity.getContent());
            case USER -> new UserMessage(entity.getContent());
            case ASSISTANT -> new AssistantMessage(entity.getContent());
            case TOOL -> new ToolResponseMessage(List.of(
                    new ToolResponseMessage.ToolResponse(
                            entity.getToolMetadata() != null ? entity.getToolMetadata() : "",
                            entity.getContent()
                    )
            ));
        };
    }

    private String serializeToolMetadata(Message message) {
        try {
            if (message instanceof ToolResponseMessage toolResponse) {
                return objectMapper.writeValueAsString(
                        toolResponse.getResponses().stream()
                                .map(r -> r.id())
                                .toList()
                );
            }
        } catch (Exception e) {
            log.warn("Failed to serialize tool metadata", e);
        }
        return null;
    }

    /**
     * Rough token estimation (1 token ≈ 4 chars for English).
     * For production, use a proper tokenizer like tiktoken.
     */
    private long estimateTokenCount(String text) {
        return text == null ? 0 : text.length() / 4;
    }
}