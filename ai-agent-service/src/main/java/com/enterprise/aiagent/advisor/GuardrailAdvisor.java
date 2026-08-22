package com.enterprise.aiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
public class GuardrailAdvisor implements CallAdvisor {

    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            Pattern.compile("(?i)ignore\\s+(all\\s+)?previous\\s+instructions"),
            Pattern.compile("(?i)you\\s+are\\s+now\\s+(a|an)\\s+"),
            Pattern.compile("(?i)disregard\\s+(all\\s+)?prior\\s+"),
            Pattern.compile("(?i)system\\s*:\\s*you\\s+are"),
            Pattern.compile("(?i)\\[INST\\]"),
            Pattern.compile("(?i)<\\|im_start\\|>"),
            Pattern.compile("(?i)forget\\s+(all\\s+)?instructions"),
            Pattern.compile("(?i)act\\s+as\\s+if\\s+you\\s+have\\s+no\\s+restrictions")
    );

    private static final List<Pattern> SENSITIVE_PATTERNS = List.of(
            Pattern.compile(
                    "\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b"
            ),
            Pattern.compile(
                    "\\b\\d{3}-\\d{2}-\\d{4}\\b"
            )
    );

    @Override
    public ChatClientResponse adviseCall(
            ChatClientRequest request,
            CallAdvisorChain chain) {

        String userText = request.prompt().getContents();

        if (userText == null || userText.isBlank()) {
            return chain.nextCall(request);
        }

        // ---------------------------------------------------------
        // Prompt injection detection
        // ---------------------------------------------------------

        for (Pattern pattern : INJECTION_PATTERNS) {

            if (pattern.matcher(userText).find()) {

                log.warn(
                        "Blocked potential prompt injection attempt: '{}'",
                        sanitizeForLogging(userText)
                );

                throw new IllegalArgumentException(
                        "The request was blocked because it contains potentially unsafe instructions."
                );
            }
        }

        // ---------------------------------------------------------
        // Sensitive data detection
        // ---------------------------------------------------------

        for (Pattern pattern : SENSITIVE_PATTERNS) {

            if (pattern.matcher(userText).find()) {

                log.warn("Potential sensitive data detected in user input");

                throw new IllegalArgumentException(
                        "The request contains potentially sensitive information. " +
                        "Please do not provide credit card numbers, social security " +
                        "numbers, or other sensitive information."
                );
            }
        }

        // ---------------------------------------------------------
        // Request is safe
        // ---------------------------------------------------------

        return chain.nextCall(request);
    }

    private String sanitizeForLogging(String text) {

        if (text == null) {
            return "";
        }

        String sanitized = text
                .replaceAll("[\\r\\n\\t]", " ")
                .trim();

        return sanitized.substring(
                0,
                Math.min(sanitized.length(), 200)
        );
    }

    @Override
    public String getName() {
        return "GuardrailAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}