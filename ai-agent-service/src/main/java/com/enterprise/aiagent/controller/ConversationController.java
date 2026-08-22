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
// @RequiredArgsConstructor
public class ConversationController {

        private final ConversationService conversationService;
        private final JpaChatMemory chatMemory;
        private final TokenBudgetAdvisor tokenBudgetAdvisor;

        public ConversationController(ConversationService conversationService,
                        JpaChatMemory chatMemory, TokenBudgetAdvisor tokenBudgetAdvisor) {
                this.conversationService = conversationService;
                this.chatMemory = chatMemory;
                this.tokenBudgetAdvisor = tokenBudgetAdvisor;
        }

        @GetMapping
        public ResponseEntity<Page<ConversationEntity>> getUserConversations(
                        @RequestParam String userId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {

                return ResponseEntity.ok(
                                conversationService.getUserConversations(
                                                userId,
                                                PageRequest.of(page, size)));
        }

        @GetMapping("/{conversationId}/messages")
        public ResponseEntity<?> getConversationMessages(
                        @PathVariable String conversationId,
                        @RequestParam(defaultValue = "100") int limit) {

                return ResponseEntity.ok(
                                chatMemory.getHistory(
                                                conversationId,
                                                limit));
        }

        @DeleteMapping("/{conversationId}")
        public ResponseEntity<Void> deleteConversation(
                        @PathVariable String conversationId) {

                chatMemory.clear(conversationId);

                tokenBudgetAdvisor.resetUsage(conversationId);

                return ResponseEntity.noContent().build();
        }
}