package com.enterprise.aiagent.advisor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class LoggingAdvisor implements CallAdvisor {

    private final Timer responseTimer;
    private final Counter requestCounter;
    private final Counter errorCounter;

    public LoggingAdvisor(MeterRegistry meterRegistry) {

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
    public ChatClientResponse adviseCall(
            ChatClientRequest request,
            CallAdvisorChain chain) {

        long start = System.nanoTime();

        requestCounter.increment();

        log.info(
                "AI request: {}",
                truncate(request.prompt().getContents(), 500)
        );

        try {

            ChatClientResponse response = chain.nextCall(request);

            long durationMs = TimeUnit.NANOSECONDS.toMillis(
                    System.nanoTime() - start
            );

            responseTimer.record(
                    durationMs,
                    TimeUnit.MILLISECONDS
            );

            log.info(
                    "AI request completed: duration={}ms",
                    durationMs
            );

            logResponse(response);

            return response;

        }
        catch (Exception e) {

            errorCounter.increment();

            long durationMs = TimeUnit.NANOSECONDS.toMillis(
                    System.nanoTime() - start
            );

            log.error(
                    "AI request failed: duration={}ms, error={}",
                    durationMs,
                    e.getMessage(),
                    e
            );

            throw e;
        }
    }

    private void logResponse(ChatClientResponse response) {

        if (response == null || response.chatResponse() == null) {
            return;
        }

        var chatResponse = response.chatResponse();

        if (chatResponse.getResult() == null
                || chatResponse.getResult().getOutput() == null) {
            return;
        }

        String text =
                chatResponse.getResult()
                        .getOutput()
                        .getText();

        log.debug(
                "AI response: '{}'",
                truncate(text, 500)
        );
    }

    @Override
    public String getName() {
        return "LoggingAdvisor";
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    private String truncate(String text, int maxLength) {

        if (text == null) {
            return "null";
        }

        return text.length() > maxLength
                ? text.substring(0, maxLength) + "..."
                : text;
    }
}