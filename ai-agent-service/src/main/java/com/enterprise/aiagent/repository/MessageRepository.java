package com.enterprise.aiagent.repository;

import com.enterprise.aiagent.model.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    @Query("""
        SELECT m FROM MessageEntity m
        JOIN FETCH m.conversation c
        WHERE c.conversationId = :conversationId
        ORDER BY m.sequenceNumber ASC
    """)
    List<MessageEntity> findByConversationId(@Param("conversationId") String conversationId);

    @Query("""
        SELECT m FROM MessageEntity m
        JOIN FETCH m.conversation c
        WHERE c.conversationId = :conversationId
        ORDER BY m.sequenceNumber DESC
        LIMIT :limit
    """)
    List<MessageEntity> findLastNByConversationId(
            @Param("conversationId") String conversationId,
            @Param("limit") int limit);
}