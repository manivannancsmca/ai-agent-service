package com.enterprise.aiagent.repository;

import com.enterprise.aiagent.model.entity.ConversationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<ConversationEntity, Long> {

    Optional<ConversationEntity> findByConversationId(String conversationId);

    Page<ConversationEntity> findByUserIdOrderByUpdatedAtDesc(String userId, Pageable pageable);

    @Query("SELECT c FROM ConversationEntity c WHERE c.updatedAt < :cutoff")
    Page<ConversationEntity> findStaleConversations(@Param("cutoff") Instant cutoff, Pageable pageable);

    @Query("SELECT COUNT(m) FROM MessageEntity m WHERE m.conversation.conversationId = :conversationId")
    long countMessagesByConversationId(@Param("conversationId") String conversationId);
}