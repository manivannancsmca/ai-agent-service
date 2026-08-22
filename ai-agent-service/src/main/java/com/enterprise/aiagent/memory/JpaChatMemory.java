package com.enterprise.aiagent.memory;

import com.enterprise.aiagent.model.entity.ConversationEntity;
import com.enterprise.aiagent.model.entity.MessageEntity;
import com.enterprise.aiagent.repository.ConversationRepository;
import com.enterprise.aiagent.repository.MessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Production-grade ChatMemory backed by MySQL/JPA.
 *
 * Compatible with Spring AI 1.0.x ChatMemory API.
 *
 * Responsibilities:
 * - Persist conversation messages
 * - Retrieve conversation history
 * - Maintain a configurable message window
 * - Preserve tool-response information
 * - Clear conversations
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JpaChatMemory implements ChatMemory {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    /**
     * Maximum number of messages returned to the LLM.
     *
     * Important:
     * This is a message window, not a token window.
     */
    private static final int DEFAULT_WINDOW_SIZE = 40;

    // ============================================================
    // ADD
    // ============================================================

    @Override
    @Transactional
    public void add(
            @NonNull String conversationId,
            @NonNull List<Message> messages) {

        if (messages.isEmpty()) {
            return;
        }

        ConversationEntity conversation =
                getOrCreateConversation(conversationId);

        long currentSequence =
                conversationRepository.countMessagesByConversationId(
                        conversationId);

        for (Message message : messages) {

            currentSequence++;

            MessageEntity entity = MessageEntity.builder()
                    .conversation(conversation)
                    .sequenceNumber((int) currentSequence)
                    .messageType(message.getMessageType().name())
                    .content(message.getText())
                    .tokenCount(
                            estimateTokenCount(message.getText()))
                    .toolMetadata(
                            serializeToolMetadata(message))
                    .build();

            messageRepository.save(entity);
        }

        log.debug(
                "Added {} messages to conversation {}",
                messages.size(),
                conversationId
        );
    }

    // ============================================================
    // GET
    // ============================================================

    /**
     * Spring AI 1.0.x ChatMemory contract.
     *
     * The old:
     *
     *     get(String conversationId, int lastN)
     *
     * is no longer the correct API.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Message> get(@NonNull String conversationId) {

        List<MessageEntity> entities =
                messageRepository.findLastNByConversationId(
                        conversationId,
                        DEFAULT_WINDOW_SIZE
                );

        if (entities.isEmpty()) {
            return List.of();
        }

        List<Message> messages =
                new ArrayList<>(entities.size());

        /*
         * Repository query returns DESC order.
         *
         * LLM expects chronological order:
         * oldest -> newest
         */
        for (MessageEntity entity : entities.reversed()) {
            messages.add(deserializeMessage(entity));
        }

        return messages;
    }

    // ============================================================
    // CLEAR
    // ============================================================

    @Override
    @Transactional
    public void clear(@NonNull String conversationId) {

        conversationRepository
                .findByConversationId(conversationId)
                .ifPresent(conversation -> {

                    /*
                     * If cascade + orphanRemoval are correctly configured,
                     * this removes child messages.
                     */
                    conversation.getMessages().clear();

                    conversationRepository.save(conversation);

                    log.info(
                            "Cleared conversation {}",
                            conversationId
                    );
                });
    }

    // ============================================================
    // SIZE
    // ============================================================

    @Transactional(readOnly = true)
    public long size(@NonNull String conversationId) {

        return conversationRepository
                .countMessagesByConversationId(conversationId);
    }

    // ============================================================
    // INTERNAL
    // ============================================================

    private ConversationEntity getOrCreateConversation(
            String conversationId) {

        return conversationRepository
                .findByConversationId(conversationId)
                .orElseGet(() ->
                        conversationRepository.save(
                                ConversationEntity.builder()
                                        .conversationId(conversationId)
                                        .userId("system")
                                        .title("Auto-created conversation")
                                        .build()
                        )
                );
    }

    // ============================================================
    // MESSAGE DESERIALIZATION
    // ============================================================

    private Message deserializeMessage(
            MessageEntity entity) {

        String content = entity.getContent();

        MessageType messageType =
                MessageType.valueOf(entity.getMessageType());

        return switch (messageType) {

            case SYSTEM ->
                    new SystemMessage(content);

            case USER ->
                    new UserMessage(content);

            case ASSISTANT ->
                    new AssistantMessage(content);

            case TOOL ->
                    deserializeToolMessage(entity);

            default ->
                    throw new IllegalStateException(
                            "Unsupported message type: "
                                    + entity.getMessageType()
                    );
        };
    }

    /**
     * Reconstruct Spring AI ToolResponseMessage.
     */
    private Message deserializeToolMessage(
            MessageEntity entity) {

        try {

            String toolMetadata = entity.getToolMetadata();

            /*
             * Our persistence format stores the tool response IDs.
             *
             * If metadata is not available, use safe defaults.
             */
            String id = "persisted-tool-response";
            String name = "unknown-tool";

            if (toolMetadata != null &&
                    !toolMetadata.isBlank()) {

                try {

                    List<String> ids =
                            objectMapper.readValue(
                                    toolMetadata,
                                    objectMapper.getTypeFactory()
                                            .constructCollectionType(
                                                    List.class,
                                                    String.class
                                            )
                            );

                    if (!ids.isEmpty()) {
                        id = ids.get(0);
                    }

                } catch (Exception e) {

                    log.debug(
                            "Unable to deserialize tool metadata",
                            e
                    );
                }
            }

            ToolResponseMessage.ToolResponse response =
                    new ToolResponseMessage.ToolResponse(
                            id,
                            name,
                            entity.getContent()
                    );

            return new ToolResponseMessage(
                    List.of(response)
            );

        } catch (Exception e) {

            log.error(
                    "Failed to deserialize tool message id={}",
                    entity.getId(),
                    e
            );

            /*
             * Do not silently corrupt the conversation.
             * Fail fast because malformed tool history can break
             * the agentic tool-call sequence.
             */
            throw new IllegalStateException(
                    "Unable to deserialize tool message",
                    e
            );
        }
    }

    // ============================================================
    // TOOL METADATA
    // ============================================================

    private String serializeToolMetadata(
            Message message) {

        try {

            if (message instanceof ToolResponseMessage toolResponse) {

                return objectMapper.writeValueAsString(
                        toolResponse.getResponses()
                                .stream()
                                .map(response -> response.id())
                                .toList()
                );
            }

        } catch (Exception e) {

            log.warn(
                    "Failed to serialize tool metadata",
                    e
            );
        }

        return null;
    }

    // ============================================================
    // TOKEN ESTIMATION
    // ============================================================

    /**
     * Rough token estimation.
     *
     * This should not be used for billing.
     * Use a real tokenizer if exact token accounting is required.
     */
    private long estimateTokenCount(String text) {

        if (text == null || text.isBlank()) {
            return 0;
        }

        return Math.max(
                1,
                text.length() / 4
        );
    }

    @Transactional(readOnly = true)
public List<Message> getHistory(String conversationId, int limit) {

    List<MessageEntity> entities =
            messageRepository.findLastNByConversationId(
                    conversationId,
                    limit
            );

    if (entities.isEmpty()) {
        return List.of();
    }

    List<Message> messages =
            new ArrayList<>(entities.size());

    for (MessageEntity entity : entities.reversed()) {
        messages.add(deserializeMessage(entity));
    }

    return messages;
}
}