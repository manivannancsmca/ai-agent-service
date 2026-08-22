package com.enterprise.aiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.AdvisedRequest;
import org.springframework.ai.chat.client.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks and limits token usage per conversation to prevent cost overruns.
 * Essential for production deployments where LLM costs can escalate quickly.
 */
@Slf4j
@Component
public class TokenBudgetAdvisor implements CallAroundAdvisor {

    @Value("${ai.agent.token-budget.max-per-conversation:100000}")
    private long maxTokensPerConversation;

    @Value("${ai.agent.token-budget.max-per-request:8000}")
    private long maxTokensPerRequest;

    private final ConcurrentHashMap<String, AtomicLong> conversationTokenUsage = new ConcurrentHashMap<>();

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest request, AdvisorChain chain) {
        String conversationId = request.adviseContext()
                .getOrDefault("conversation_id", "default").toString();

        // Check per-conversation budget
        AtomicLong usage = conversationTokenUsage
                .computeIfAbsent(conversationId, k -> new AtomicLong(0));

        long currentUsage = usage.get();
        if (currentUsage >= maxTokensPerConversation) {
            log.warn("Token budget exceeded for conversation {}: {} >= {}",
                    conversationId, currentUsage, maxTokensPerConversation);

            return new AdvisedResponse(
                    ChatResponse.builder()
                            .withGenerations(List.of(new Generation(
                                    new AssistantMessage(
                                            "This conversation has reached its token limit. " +
                                            "Please start a new conversation to continue. " +
                                            "This helps us manage resources efficiently.")
                            )))
                            .build(),
                    request.adviseContext()
            );
        }

        // Add budget info to context so the agent is aware
        Map<String, Object> context = new java.util.HashMap<>(request.adviseContext());
        context.put("remaining_token_budget", maxTokensPerConversation - currentUsage);

        AdvisedRequest enrichedRequest = AdvisedRequest.from(request)
                .withAdviseContext(context)
                .build();

        // Proceed with the call
        AdvisedResponse response = chain.nextAroundCall(enrichedRequest);

        // Track token usage from response
        ChatResponse chatResponse = response.response();
        if (chatResponse != null && chatResponse.getMetadata() != null) {
            var chatUsage = chatResponse.getMetadata().getUsage();
            if (chatUsage != null) {
                long totalTokens = chatUsage.getPromptTokens() + chatUsage.getGenerationTokens();
                usage.addAndGet(totalTokens);

                log.debug("Token usage for conversation {}: +{} (total: {}/{})",
                        conversationId, totalTokens, usage.get(), maxTokensPerConversation);
            }
        }

        return response;
    }

    /**
     * Reset token usage for a conversation (call when starting a new topic).
     */
    public void resetUsage(String conversationId) {
        conversationTokenUsage.remove(conversationId);
        log.info("Reset token budget for conversation {}", conversationId);
    }

    /**
     * Get current usage for monitoring.
     */
    public long getCurrentUsage(String conversationId) {
        AtomicLong usage = conversationTokenUsage.get(conversationId);
        return usage != null ? usage.get() : 0;
    }

    @Override
    public String getName() {
        return "TokenBudgetAdvisor";
    }

    @Override
    public int getOrder() {
        return 10; // After guardrail, before logging
    }
}