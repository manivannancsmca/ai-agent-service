package com.enterprise.aiagent.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatResponse;

import java.util.List;
import java.util.Map;

/**
 * Base class for all specialized agents.
 * Provides common functionality for chat interactions.
 */
@Slf4j
public abstract class BaseAgent {

    protected final ChatClient chatClient;

    protected BaseAgent(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Process a user message and return a text response.
     * The ChatClient handles the full agentic loop: prompt → LLM → tool calls → LLM → response.
     */
    public String chat(String conversationId, String userMessage) {
        log.info("Agent [{}] processing message for conversation {}", getAgentName(), conversationId);

        try {
            String response = chatClient.prompt()
                    .user(userMessage)
                    .advisors(advisor -> advisor
                            .param("conversation_id", conversationId))
                    .call()
                    .content();

            log.info("Agent [{}] completed for conversation {}", getAgentName(), conversationId);
            return response;

        } catch (Exception e) {
            log.error("Agent [{}] failed for conversation {}: {}",
                    getAgentName(), conversationId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Process a user message and return the full ChatResponse with metadata.
     */
    public ChatResponse chatWithMetadata(String conversationId, String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .advisors(advisor -> advisor
                        .param("conversation_id", conversationId))
                .call()
                .chatResponse();
    }

    /**
     * Stream a response for real-time UI updates.
     */
    public org.springframework.ai.chat.model.Flux<org.springframework.ai.chat.model.Generation> stream(
            String conversationId, String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .advisors(advisor -> advisor
                        .param("conversation_id", conversationId))
                .stream()
                .content();
    }

    public abstract String getAgentName();
}