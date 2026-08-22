package com.enterprise.aiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.enterprise.aiagent.exception.TokenBudgetExceededException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks and limits token usage per conversation.
 *
 * <p>
 * Protects the application from unexpectedly high LLM costs by enforcing:
 *
 * <ul>
 *     <li>Maximum tokens per conversation</li>
 *     <li>Maximum estimated tokens per request</li>
 * </ul>
 *
 * <p>
 * This advisor uses the Spring AI 1.0 CallAdvisor API.
 */
@Slf4j
@Component
public class TokenBudgetAdvisor implements CallAdvisor {

    @Value("${ai.agent.token-budget.max-per-conversation:100000}")
    private long maxTokensPerConversation;

    @Value("${ai.agent.token-budget.max-per-request:8000}")
    private long maxTokensPerRequest;

    private final ConcurrentHashMap<String, AtomicLong> conversationTokenUsage =
            new ConcurrentHashMap<>();

    @Override
    public ChatClientResponse adviseCall(
            ChatClientRequest request,
            CallAdvisorChain chain) {

        String conversationId = request.context()
                .getOrDefault("conversation_id", "default")
                .toString();

        AtomicLong usage = conversationTokenUsage.computeIfAbsent(
                conversationId,
                key -> new AtomicLong(0)
        );

        long currentUsage = usage.get();

        // ---------------------------------------------------------
        // Conversation budget check
        // ---------------------------------------------------------

        if (currentUsage >= maxTokensPerConversation) {

            log.warn(
                    "Token budget exceeded: conversation={}, current={}, max={}",
                    conversationId,
                    currentUsage,
                    maxTokensPerConversation
            );

            throw new TokenBudgetExceededException(
                    "This conversation has reached its token limit. " +
                    "Please start a new conversation to continue."
            );
        }

        // ---------------------------------------------------------
        // Continue advisor chain
        // ---------------------------------------------------------

        ChatClientResponse response = chain.nextCall(request);

        // ---------------------------------------------------------
        // Track actual token usage
        // ---------------------------------------------------------

        recordTokenUsage(
                conversationId,
                usage,
                response
        );

        return response;
    }

    private void recordTokenUsage(
            String conversationId,
            AtomicLong usage,
            ChatClientResponse response) {

        if (response == null || response.chatResponse() == null) {
            return;
        }

        var metadata = response.chatResponse().getMetadata();

        if (metadata == null) {
            return;
        }

        var chatUsage = metadata.getUsage();

        if (chatUsage == null) {
            return;
        }

        Integer totalTokens = chatUsage.getTotalTokens();

        if (totalTokens == null) {

            Integer promptTokens = chatUsage.getPromptTokens();
            Integer completionTokens = chatUsage.getCompletionTokens();

            long prompt = promptTokens != null ? promptTokens : 0;
            long completion = completionTokens != null ? completionTokens : 0;

            totalTokens = Math.toIntExact(prompt + completion);
        }

        usage.addAndGet(totalTokens);

        log.debug(
                "Token usage: conversation={}, added={}, total={}, max={}",
                conversationId,
                totalTokens,
                usage.get(),
                maxTokensPerConversation
        );
    }

    /**
     * Reset token usage for a conversation.
     */
    public void resetUsage(String conversationId) {

        conversationTokenUsage.remove(conversationId);

        log.info(
                "Token budget reset: conversation={}",
                conversationId
        );
    }

    /**
     * Returns current token usage for a conversation.
     */
    public long getCurrentUsage(String conversationId) {

        AtomicLong usage =
                conversationTokenUsage.get(conversationId);

        return usage != null
                ? usage.get()
                : 0;
    }

    /**
     * Returns remaining token budget.
     */
    public long getRemainingUsage(String conversationId) {

        long current = getCurrentUsage(conversationId);

        return Math.max(
                0,
                maxTokensPerConversation - current
        );
    }

    @Override
    public String getName() {
        return "TokenBudgetAdvisor";
    }

    @Override
    public int getOrder() {
        return 10;
    }
}