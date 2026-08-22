package com.enterprise.aiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.AdvisedRequest;
import org.springframework.ai.chat.client.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Prevents prompt injection attacks and sanitizes user input.
 * Runs early in the advisor chain to reject malicious input
 * before it reaches the LLM.
 */
@Slf4j
@Component
public class GuardrailAdvisor implements CallAroundAdvisor {

    // Patterns that commonly indicate prompt injection attempts
    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            Pattern.compile("(?i)ignore\\s+(all\\s+)?previous\\s+instructions"),
            Pattern.compile("(?i)you\\s+are\\s+now\\s+(a|an)\\s+"),
            Pattern.compile("(?i)disregard\\s+(all\\s+)?prior\\s+"),
            Pattern.compile("(?i)system\\s*:\\s*you\\s+are"),
            Pattern.compile("(?i)\\[INST\\]"),
            Pattern.compile("(?i)\\<\\|im_start\\|\\>"),
            Pattern.compile("(?i)forget\\s+(all\\s+)?instructions"),
            Pattern.compile("(?i)act\\s+as\\s+if\\s+you\\s+have\\s+no\\s+restrictions")
    );

    // Sensitive data patterns that should not be in user input
    private static final List<Pattern> SENSITIVE_PATTERNS = List.of(
            Pattern.compile("\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b"), // Credit card
            Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b") // SSN
    );

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest request, AdvisorChain chain) {
        String userText = request.userText();

        // Check for prompt injection
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(userText).find()) {
                log.warn("Blocked potential prompt injection attempt: '{}'",
                        userText.substring(0, Math.min(userText.length(), 200)));

                // Return a safe response without calling the LLM
                return new AdvisedResponse(
                        org.springframework.ai.chat.model.ChatResponse.builder()
                                .withGenerations(List.of(
                                        new org.springframework.ai.chat.model.Generation(
                                                new org.springframework.ai.chat.messages.AssistantMessage(
                                                        "I'm sorry, but I can only help you with product " +
                                                        "and order-related questions. How can I assist you today?")
                                        )
                                ))
                                .build(),
                        request.adviseContext()
                );
            }
        }

        // Check for accidentally leaked sensitive data
        for (Pattern pattern : SENSITIVE_PATTERNS) {
            if (pattern.matcher(userText).find()) {
                log.warn("Detected potentially sensitive data in user input");
                return new AdvisedResponse(
                        org.springframework.ai.chat.model.ChatResponse.builder()
                                .withGenerations(List.of(
                                        new org.springframework.ai.chat.model.Generation(
                                                new org.springframework.ai.chat.messages.AssistantMessage(
                                                        "I noticed your message may contain sensitive information. " +
                                                        "Please do not share credit card numbers, social security " +
                                                        "numbers, or other sensitive data. How can I help you?")
                                        )
                                ))
                                .build(),
                        request.adviseContext()
                );
            }
        }

        // Input is safe — proceed
        return chain.nextAroundCall(request);
    }

    @Override
    public String getName() {
        return "GuardrailAdvisor";
    }

    @Override
    public int getOrder() {
        return 0; // Run first (innermost)
    }
}