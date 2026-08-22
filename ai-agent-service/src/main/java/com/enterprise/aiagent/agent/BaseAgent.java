package com.enterprise.aiagent.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * Base class for all specialized agents.
 *
 * Provides common functionality for:
 * - synchronous chat
 * - chat with metadata
 * - reactive streaming
 */
@Slf4j
public abstract class BaseAgent {

        protected final ChatClient chatClient;

        protected BaseAgent(ChatClient chatClient) {
                this.chatClient = chatClient;
        }

        /**
         * Process a user message and return a text response.
         */
        public String chat(
                        String conversationId,
                        String userMessage) {

                log.info(
                                "Agent [{}] processing message for conversation {}",
                                getAgentName(),
                                conversationId);

                try {

                        String response = chatClient.prompt()
                                        .user(userMessage)
                                        .advisors(advisor -> advisor
                                                        .param("conversation_id", conversationId))
                                        .call()
                                        .content();

                        log.info(
                                        "Agent [{}] completed for conversation {}",
                                        getAgentName(),
                                        conversationId);

                        return response;

                } catch (Exception e) {

                        log.error(
                                        "Agent [{}] failed for conversation {}: {}",
                                        getAgentName(),
                                        conversationId,
                                        e.getMessage(),
                                        e);

                        throw e;
                }
        }

        /**
         * Process a user message and return the complete ChatResponse.
         *
         * Useful when you need:
         * - token usage
         * - model metadata
         * - finish reason
         * - tool-call information
         */
        public ChatResponse chatWithMetadata(
                        String conversationId,
                        String userMessage) {

                return chatClient.prompt()
                                .user(userMessage)
                                .advisors(advisor -> advisor
                                                .param("conversation_id", conversationId))
                                .call()
                                .chatResponse();
        }

        /**
         * Stream the generated response as it is produced.
         *
         * Returns Reactor Flux<String>.
         */
        public Flux<String> stream(
                        String conversationId,
                        String userMessage) {

                log.info(
                                "Agent [{}] starting streaming response for conversation {}",
                                getAgentName(),
                                conversationId);

                return chatClient.prompt()
                                .user(userMessage)
                                .advisors(advisor -> advisor
                                                .param("conversation_id", conversationId))
                                .stream()
                                .content();
        }

        /**
         * Name of the specialized agent.
         */
        public abstract String getAgentName();
}