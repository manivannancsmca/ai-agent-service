package com.enterprise.aiagent.model.dto;

import java.time.Instant;
import java.util.List;

public record ChatResponse(
        String conversationId,
        String message,
        String agentUsed,
        List<ToolCallRecord> toolCalls,
        TokenUsage tokenUsage,
        Instant timestamp
) {
    public record ToolCallRecord(
            String toolName,
            String arguments,
            String resultSummary,
            long durationMs
    ) {}

    public record TokenUsage(
            long promptTokens,
            long completionTokens,
            long totalTokens
    ) {}
}