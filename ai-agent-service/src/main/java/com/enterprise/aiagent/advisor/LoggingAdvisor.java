package com.enterprise.aiagent.advisor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.client.AdvisedRequest;
import org.springframework.ai.chat.client.AdvisedResponse;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Logs every AI interaction for observability and debugging.
 * Tracks latency, token usage, and error rates via Micrometer.
 */
@Slf4j
@Component
public class LoggingAdvisor implements CallAroundAdvisor {

    private final MeterRegistry meterRegistry;
    private final Timer responseTimer;
    private final Counter requestCounter;
    private final Counter errorCounter;

    public LoggingAdvisor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.responseTimer = Timer.builder("ai.agent.request.duration")
                .description("AI agent request latency")
                .register(meterRegistry);
        this.requestCounter = Counter.builder("ai.agent.requests.total")
                .description("Total AI agent requests")
                .register(meterRegistry);
        this.errorCounter = Counter.builder("ai.agent.errors.total")
                .description("Total AI agent errors")
                .register(meterRegistry);
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest request, AdvisorChain chain) {
        long startTime = System.nanoTime();
        requestCounter.increment();

        String conversationId = request.adviseContext()
                .getOrDefault("conversation_id", "unknown").toString();

        log.info("AI Request [conv={}]: system='{}', user='{}'",
                conversationId,
                truncate(request.system(), 100),
                truncate(request.userText(), 200));

        try {
            AdvisedResponse response = chain.nextAroundCall(request);

            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            responseTimer.record(durationMs, TimeUnit.MILLISECONDS);

            ChatResponse chatResponse = response.response();
            if (chatResponse != null && chatResponse.getMetadata() != null) {
                var usage = chatResponse.getMetadata().getUsage();
                if (usage != null) {
                    log.info("AI Response [conv={}]: tokens={}/{}, duration={}ms",
                            conversationId,
                            usage.getPromptTokens(),
                            usage.getGenerationTokens(),
                            durationMs);

                    meterRegistry.counter("ai.agent.tokens.prompt")
                            .increment(usage.getPromptTokens());
                    meterRegistry.counter("ai.agent.tokens.completion")
                            .increment(usage.getGenerationTokens());
                }
            }

            log.debug("AI Response [conv={}]: content='{}'",
                    conversationId,
                    truncate(chatResponse.getResult().getOutput().getText(), 500));

            return response;

        } catch (Exception e) {
            errorCounter.increment();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            log.error("AI Request failed [conv={}, duration={}ms]: {}",
                    conversationId, durationMs, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public String getName() {
        return "LoggingAdvisor";
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE; // Run last (outermost)
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "null";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}