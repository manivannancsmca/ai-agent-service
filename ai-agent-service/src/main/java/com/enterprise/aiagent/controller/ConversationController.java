package com.enterprise.aiagent.controller;

import com.enterprise.aiagent.advisor.TokenBudgetAdvisor;
import com.enterprise.aiagent.memory.JpaChatMemory;
import com.enterprise.aiagent.model.entity.ConversationEntity;
import com.enterprise.aiagent.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final JpaChatMemory chatMemory;
    private final TokenBudgetAdvisor tokenBudgetAdvisor;

    /**
     * Get all conversations for a user.
     */
    @GetMapping
    public ResponseEntity<Page<ConversationEntity>> getUserConversations(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                conversationService.getUserConversations(userId, PageRequest.of(page, size)));
    }

    /**
     * Get conversation history (messages).
     */
    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<?> getConversationMessages(
            @PathVariable String conversationId) {
        var messages = chatMemory.get(conversationId, 100);
        return ResponseEntity.ok(messages);
    }

    /**
     * Delete a conversation and its history.
     */
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(@PathVariable String conversationId) {
        chatMemory.clear(conversationId);
        tokenBudgetAdvisor.resetUsage(conversationId);
        return ResponseEntity.noContent().build();
    }
}