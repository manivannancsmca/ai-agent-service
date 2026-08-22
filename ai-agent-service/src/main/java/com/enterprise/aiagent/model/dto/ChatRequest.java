package com.enterprise.aiagent.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @NotBlank(message = "Message cannot be blank")
        @Size(max = 10000, message = "Message exceeds maximum length")
        String message,

        String conversationId,

        @Size(max = 128)
        String userId,

        /**
         * Optional: restrict the agent to a specific domain.
         * Values: "auto" (supervisor decides), "shopping", "orders"
         */
        String agentDomain
) {
    public ChatRequest {
        if (agentDomain == null) agentDomain = "auto";
    }
}