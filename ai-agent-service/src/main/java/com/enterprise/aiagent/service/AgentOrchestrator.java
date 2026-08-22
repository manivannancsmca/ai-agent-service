package com.enterprise.aiagent.service;

import com.enterprise.aiagent.advisor.TokenBudgetAdvisor;
import com.enterprise.aiagent.agent.SupervisorAgent;
import com.enterprise.aiagent.model.dto.ChatRequest;
import com.enterprise.aiagent.model.dto.ChatResponse;
import com.enterprise.aiagent.model.entity.ConversationEntity;
import com.enterprise.aiagent.repository.ConversationRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * The main orchestrator that ties everything together.
 *
 * Responsibilities:
 * 1. Manage conversation lifecycle (create, retrieve, update)
 * 2. Route requests through the Supervisor Agent
 * 3. Collect metrics and build response DTOs
 * 4. Handle errors gracefully
 */
@Slf4j
@Service
public class AgentOrchestrator {

    private final SupervisorAgent supervisorAgent;
    private final ConversationService conversationService;
    private final TokenBudgetAdvisor tokenBudgetAdvisor;
    private final MeterRegistry meterRegistry;

    public AgentOrchestrator(
            SupervisorAgent supervisorAgent,
            ConversationService conversationService,
            TokenBudgetAdvisor tokenBudgetAdvisor,
            MeterRegistry meterRegistry) {
        this.supervisorAgent = supervisorAgent;
        this.conversationService = conversationService;
        this.tokenBudgetAdvisor = tokenBudgetAdvisor;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Process a chat request through the AI agent system.
     *
     * Flow:
     * 1. Resolve or create conversation
     * 2. Route through Supervisor → specialist agent
     * 3. Build and return response with metadata
     */
    public ChatResponse processChat(ChatRequest request) {
        String conversationId = resolveConversationId(request);
        long startTime = System.nanoTime();

        log.info("Processing chat request: conversationId={}, userId={}, domain={}",
                conversationId, request.userId(), request.agentDomain());

        try {
            // Route through the supervisor (which handles multi-agent orchestration)
            String agentResponse;
            String agentUsed;

            if (!"auto".equals(request.agentDomain())) {
                // Direct routing if user specified a domain
                agentResponse = supervisorAgent.processRequest(conversationId, request.message());
                agentUsed = request.agentDomain();
            } else {
                agentResponse = supervisorAgent.processRequest(conversationId, request.message());
                agentUsed = "SupervisorAgent";
            }

            long durationMs = (System.nanoTime() - startTime) / 1_000_000;

            // Track metrics
            meterRegistry.counter("ai.agent.chat.completed").increment();
            meterRegistry.timer("ai.agent.chat.duration").record(
                    java.time.Duration.ofMillis(durationMs));

            return new ChatResponse(
                    conversationId,
                    agentResponse,
                    agentUsed,
                    List.of(), // Tool call details would be populated by a more detailed implementation
                    new ChatResponse.TokenUsage(
                            tokenBudgetAdvisor.getCurrentUsage(conversationId), 0, 0),
                    Instant.now()
            );

        } catch (Exception e) {
            long durationMs = (System.nanoTime() - startTime) / 1_000_000;
            meterRegistry.counter("ai.agent.chat.errors").increment();

            log.error("Chat processing failed after {}ms for conversation {}: {}",
                    durationMs, conversationId, e.getMessage());

            return new ChatResponse(
                    conversationId,
                    "I apologize, but I encountered an issue processing your request. " +
                    "Please try again, or rephrase your question. If the problem persists, " +
                    "our support team can help.",
                    "error",
                    List.of(),
                    new ChatResponse.TokenUsage(0, 0, 0),
                    Instant.now()
            );
        }
    }

    /**
     * Stream a chat response for real-time UI updates.
     * Uses SSE (Server-Sent Events) for streaming.
     */
    public reactor.core.publisher.Flux<String> streamChat(ChatRequest request) {
        String conversationId = resolveConversationId(request);

        return reactor.core.publisher.Flux.create(sink -> {
            try {
                // For streaming, we use the supervisor's stream capability
                // The response arrives in chunks
                String fullResponse = supervisorAgent.processRequest(
                        conversationId, request.message());

                // Simulate streaming by sending chunks
                String[] words = fullResponse.split(" ");
                for (int i = 0; i < words.length; i++) {
                    String chunk = (i > 0 ? " " : "") + words[i];
                    sink.next(chunk);

                    // Small delay for streaming effect
                    Thread.sleep(30);
                }

                sink.complete();
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    private String resolveConversationId(ChatRequest request) {
        if (request.conversationId() != null && !request.conversationId().isBlank()) {
            return request.conversationId();
        }
        return UUID.randomUUID().toString();
    }
}