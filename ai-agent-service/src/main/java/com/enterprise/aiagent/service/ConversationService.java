package com.enterprise.aiagent.service;

import com.enterprise.aiagent.client.OrderServiceClient;
import com.enterprise.aiagent.model.entity.ConversationEntity;
import com.enterprise.aiagent.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

//@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {

    private static final Logger log =
            LoggerFactory.getLogger(ConversationService.class);

    private final ConversationRepository conversationRepository;

    @Transactional
    public ConversationEntity getOrCreate(String conversationId, String userId) {
        return conversationRepository.findByConversationId(conversationId)
                .orElseGet(() -> createConversation(conversationId, userId));
    }

    @Transactional
    public ConversationEntity createConversation(String conversationId, String userId) {
        ConversationEntity entity = ConversationEntity.builder()
                .conversationId(conversationId != null ? conversationId : UUID.randomUUID().toString())
                .userId(userId != null ? userId : "anonymous")
                .title("New Conversation")
                .build();

        return conversationRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public Page<ConversationEntity> getUserConversations(String userId, Pageable pageable) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageable);
    }

    /**
     * Cleanup old conversations to prevent database bloat.
     * Runs daily at 2 AM. Conversations older than 90 days are archived.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupOldConversations() {
        Instant cutoff = Instant.now().minus(90, ChronoUnit.DAYS);
        log.info("Cleaning up conversations older than {}", cutoff);

        Page<ConversationEntity> stale = conversationRepository
                .findStaleConversations(cutoff, Pageable.ofSize(100));

        for (ConversationEntity conv : stale.getContent()) {
            conv.getMessages().clear();
            conversationRepository.delete(conv);
        }

        log.info("Cleaned up {} stale conversations", stale.getContent().size());
    }
}